package schemely.formatter.codeStyle;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.*;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;
import schemely.SchemeLanguage;

import javax.swing.*;


public class SchemeLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    public static final String ABSOLUTE = "Absolute";
    public static final String RELATIVE = "Indent statements after label";
    public static final String RELATIVE_REVERSED = "Indent labels";

    @NotNull
    @Override
    public Language getLanguage() {
        return SchemeLanguage.INSTANCE;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions("SPACE_AROUND_ASSIGNMENT_OPERATORS");
            consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Separator");
        } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
        }
    }

    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(SchemeLanguage.INSTANCE);
        defaultSettings.initIndentOptions();
        CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.getIndentOptions();
        if (null != indentOptions) {
            indentOptions.INDENT_SIZE = 2;
            indentOptions.TAB_SIZE = 2;
            indentOptions.CONTINUATION_INDENT_SIZE = 4;
        }
        return defaultSettings;
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return "(f f)\n" +
                "\n" +
                "((lambda (f) x) (lambda (f) x))\n" +
                "\n" +
                "(define Y (lambda (f) (f f)))\n" +
                "\n" +
                "((lambda (f) (lambda (x) ((f f) (g x))))\n" +
                " (lambda (f) (lambda (x) ((f f) (g x)))))\n" +
                " \n" +
                "((lambda (g)\n" +
                " ((lambda (f) (lambda (x) ((f f) (g x))))\n" +
                "  (lambda (f) (lambda (x) ((f f) (g x))))))\n" +
                "cdr)\n" +
                "\n" +
                "((lambda (g)\n" +
                " ((lambda (f) (f f))\n" +
                "  (lambda (f) (lambda (x) ((f f) (g x))))))\n" +
                "g)";
    }

    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new SmartIndentOptionsEditor() {
            private JTextField myLabelIndent;
            private JLabel myLabelIndentLabel;

            private JComboBox myLabelIndentStyle;
            private JBLabel myStyleLabel;

            @Override
            public boolean isModified(final CodeStyleSettings settings, final CommonCodeStyleSettings.IndentOptions options) {
                boolean isModified = super.isModified(settings, options);

                isModified |= isFieldModified(myLabelIndent, options.LABEL_INDENT_SIZE);
                isModified |= isLabelStyleModified(options.LABEL_INDENT_ABSOLUTE, settings.getCustomSettings(SchemeCodeStyleSettings.class).INDENT_LABEL_BLOCKS);
                return isModified;
            }

            private boolean isLabelStyleModified(boolean absolute, boolean relative) {
                if (absolute) {
                    return !ABSOLUTE.equals(myLabelIndentStyle.getSelectedItem());
                }
                else if (relative) {
                    return !RELATIVE.equals(myLabelIndentStyle.getSelectedItem());
                }
                else {
                    return !RELATIVE_REVERSED.equals(myLabelIndentStyle.getSelectedItem());
                }
            }

            @Override
            public void apply(final CodeStyleSettings settings, final CommonCodeStyleSettings.IndentOptions options) {
                super.apply(settings, options);
                options.LABEL_INDENT_SIZE = getFieldValue(myLabelIndent, Integer.MIN_VALUE, options.LABEL_INDENT_SIZE);
                options.LABEL_INDENT_ABSOLUTE = ABSOLUTE.equals(myLabelIndentStyle.getSelectedItem());
                settings.getCustomSettings(SchemeCodeStyleSettings.class).INDENT_LABEL_BLOCKS = RELATIVE.equals(myLabelIndentStyle.getSelectedItem());
            }

            @Override
            public void reset(@NotNull final CodeStyleSettings settings, @NotNull final CommonCodeStyleSettings.IndentOptions options) {
                super.reset(settings, options);
                myLabelIndent.setText(Integer.toString(options.LABEL_INDENT_SIZE));
                if (options.LABEL_INDENT_ABSOLUTE) {
                    myLabelIndentStyle.setSelectedItem(ABSOLUTE);
                }
                else if(settings.getCustomSettings(SchemeCodeStyleSettings.class).INDENT_LABEL_BLOCKS) {
                    myLabelIndentStyle.setSelectedItem(RELATIVE);
                }
                else {
                    myLabelIndentStyle.setSelectedItem(RELATIVE_REVERSED);
                }
            }

            @Override
            public void setEnabled(final boolean enabled) {
                super.setEnabled(enabled);
                myLabelIndent.setEnabled(enabled);
                myLabelIndentLabel.setEnabled(enabled);
                myStyleLabel.setEnabled(enabled);
                myLabelIndentStyle.setEnabled(enabled);
            }
        };
    }
}
