package org.epic.perleditor.editors.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.epic.core.util.ScriptExecutor;
import org.epic.core.util.StatusFactory;
import org.epic.perleditor.preferences.PerlCriticPreferencePage;
import org.epic.perleditor.preferences.PreferenceConstants;


/**
 * Runs metrics against perl code using <code>Perl::Critic</code>
 *
 * @see http://search.cpan.org/dist/Perl-Critic/
 */
public class SourceCritic extends ScriptExecutor
{
    //~ Static fields/initializers

    private static Violation[] EMPTY_ARRAY = new Violation[0];

    private IResource resource;

    //~ Constructors

    protected SourceCritic(ILog log)
    {
        super(log);
    }

    //~ Methods   

    public static Violation[] critique(IResource resource, ILog log)
    {
        IFile file = (IFile) resource;
        /*
         * it seems that Perl::Critic does not like receiving the editor input when invoked via the
         * perl executor (although it works fine from the command line outside of java land).
         *
         * this work around is ok for now b/c metrics are only run against a single file, but this
         * won't work for entire directories at a time - perhaps a background job that processes
         * each one?
         */
        ArrayList args = new ArrayList(1);
        args.add(file.getRawLocation().toOSString());

        try
        {
            SourceCritic critic = new SourceCritic(log);
            // meh - not sure if i'm happy w/ this, but it's needed in getCommandLineOpts
            critic.resource = resource;

            String output = critic.run(args).stdout;
            return critic.parseViolations(output);
        }
        catch (CoreException e)
        {
            log.log(e.getStatus());
            // nothing more we can do
            return EMPTY_ARRAY;
        }
    }

    /*
     * @see org.epic.core.util.ScriptExecutor#getCommandLineOpts(java.util.List)
     */
    protected List getCommandLineOpts(List additionalOptions)
    {
        if (additionalOptions == null || additionalOptions.isEmpty())
        {
            additionalOptions = new ArrayList(2);
        }

        // project specific critic config files
        IFile rc = resource.getProject().getFile(".perlcriticrc");
        try
        {
            rc.refreshLocal(IResource.DEPTH_ZERO, null);
        }
        catch (CoreException e)
        {
            log(e.getStatus());
        }
        if (rc.exists())
        {
            additionalOptions.add("-profile");
            additionalOptions.add(rc.getRawLocation().toOSString());
        }
        
        String severity = PerlCriticPreferencePage.getSeverity();
        if(!severity.equals("default")) 
        {
        	additionalOptions.add("--" + severity);
        }

        additionalOptions.add("-verbose");
        additionalOptions.add("%f~|~%s~|~%l~|~%c~|~%m~|~%e" + getSystemLineSeparator());
        
        String otherOptions = PerlCriticPreferencePage.getOtherOptions();
        if(otherOptions.length() > 0)
        {
        	additionalOptions.add(otherOptions);
        }
        
        return additionalOptions;
    }

    /*
     * @see org.epic.core.util.ScriptExecutor#getExecutable()
     */
    protected String getExecutable()
    {
        return PerlCriticPreferencePage.getPerlCritic();
    }

    /*
     * @see org.epic.core.util.ScriptExecutor#getScriptDir()
     */
    protected String getScriptDir()
    {
        return "";
    }

    private final Violation parseLine(String toParse)
    {
        String[] tmp = toParse.split("~\\|~");

        // handle cases where a line returned from critic doesn't have all 6 expected fields
        if (tmp.length != 6)
        {
            return null;
        }

        Violation violation = new Violation();

        violation.file = tmp[0];
        violation.severity = parseInt(tmp[1]);
        violation.lineNumber = parseInt(tmp[2]);
        violation.column = parseInt(tmp[3]);
        violation.message = tmp[4];
        violation.pbp = tmp[5];

        return violation;
    }

    private final Violation[] parseViolations(String toParse)
    {
        String separator = getLineSeparator(toParse);

        if ((toParse == null) || "".equals(toParse) || toParse.endsWith("OK" + separator))
        {
            return EMPTY_ARRAY;
        }

        String[] lines = toParse.split(separator);
        ArrayList violations = new ArrayList();;
        for (int i = 0; i < lines.length; i++)
        {
            System.out.println("critic: " + lines[i]);

            Violation v = parseLine(lines[i]);
            if (v != null)
            {
                violations.add(v);
            }
        }

        if (violations.size() == 0)
        {
            log(StatusFactory.createWarning(getPluginId(),
                    "Perl::Critic violations.length == 0, output change?"));
        }

        return (Violation[]) violations.toArray(new Violation[violations.size()]);
    }

    public static class Violation
    {
        public int column;
        public String file;
        public int lineNumber;
        public String message;
        public String pbp;
        public int severity;
    }

}