package org.epic.perleditor.editors.util;

import org.epic.perleditor.PerlEditorPlugin;
import org.epic.perleditor.preferences.PreferenceConstants;

public class PreferenceUtil
{
    /**
     * @param column
     * @return
     */
    public static String getTab(int column)
    {
        boolean useSpaces =
            PerlEditorPlugin.getDefault().getPreferenceStore()
            .getBoolean(PreferenceConstants.SPACES_INSTEAD_OF_TABS);

        String tabString = null;

        if (useSpaces)
        {
            int numSpaces = PerlEditorPlugin.getDefault().getPreferenceStore()
                .getInt(PreferenceConstants.INSERT_TABS_ON_INDENT);

            if (numSpaces > 0)
            {
                char[] indentChars = new char[numSpaces - (column % numSpaces)];
                for (int i = 0; i < indentChars.length; i++)
                {
                    indentChars[i] = ' ';
                }
                tabString = String.valueOf(indentChars);
            }
            else tabString = "";
        }
        else
        {
            tabString = "\t";
        }

        return tabString;
    }
}