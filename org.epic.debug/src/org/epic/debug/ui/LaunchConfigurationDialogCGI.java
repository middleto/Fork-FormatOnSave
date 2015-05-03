package org.epic.debug.ui;

import org.eclipse.debug.ui.*;

/**
 * @author ruehl
 */
public class LaunchConfigurationDialogCGI extends
    AbstractLaunchConfigurationTabGroup
{
    public void createTabs(ILaunchConfigurationDialog dialog, String mode)
    {
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[]
        {
            new LaunchConfigurationMainTab(false),
            new LaunchConfigurationCGIWebServerTab(),            
            new LaunchConfigurationCGIBrowserTab(),
            new LaunchConfigurationArgumentsTab(true),
            new EnvironmentTab(),
            new org.eclipse.debug.ui.CommonTab()
        };
        setTabs(tabs);
    }
}
