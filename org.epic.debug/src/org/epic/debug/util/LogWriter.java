/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.epic.debug.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
/**
 * A log writer that writes log entries.
 * See PlatformLogReader for reading logs back into memory.
 */
public class LogWriter implements ILogListener {
	protected File logFile = null;
	protected Writer log = null;
	protected boolean newSession = true;
	protected int mLogLevel;

	protected static final String SESSION = "!SESSION";//$NON-NLS-1$
	protected static final String ENTRY = "!ENTRY";//$NON-NLS-1$
	protected static final String SUBENTRY = "!SUBENTRY";//$NON-NLS-1$
	protected static final String MESSAGE = "!MESSAGE";//$NON-NLS-1$
	protected static final String STACK = "!STACK";//$NON-NLS-1$

	protected static final String LINE_SEPARATOR;
	protected static final String TAB_STRING = "\t";//$NON-NLS-1$

	static {
		String s = System.getProperty("line.separator");//$NON-NLS-1$
		LINE_SEPARATOR = s == null ? "\n" : s;//$NON-NLS-1$
	}

public LogWriter(File file,int fLogLevel) {
	this.logFile = file;
	this.mLogLevel = fLogLevel;
}
/**
 * This constructor should only be used to pass System.out .
 */
public LogWriter(OutputStream out,int fLogLevel) {
	this.log = logForStream(out);
	this.mLogLevel = fLogLevel;
}
protected void closeLogFile() throws IOException {
	try {
		if (log != null) {
			log.flush();
			log.close();
		}
	} finally {
		log = null;
	}
}
/**
 * @see ILogListener#logging.
 */
public synchronized void logging(IStatus status, String plugin) {
	
	if( status != null )
	 if( status.getSeverity() < mLogLevel)
	  return;
	  
	// thread safety: (Concurrency003)
	if (logFile != null)
		openLogFile();
	if (log == null)
		log = logForStream(System.err);
	try {
		try {
			write(status, 0);
		} finally {
			if (logFile != null)
				closeLogFile();
			else
				log.flush();
		}
	} catch (Exception e) {
		System.err.println("An exception occurred while writing to the EPIC log:");//$NON-NLS-1$
		e.printStackTrace(System.err);
		System.err.println("Logging to the console instead.");//$NON-NLS-1$
		//we failed to write, so dump log entry to console instead
	try {
			log = logForStream(System.err);
			write(status, 0);
			log.flush();
		} catch (Exception e2) {
			System.err.println("An exception occurred while logging to the console:");//$NON-NLS-1$
			e2.printStackTrace(System.err);
		}
	} finally {
			log = null;
	}
}
protected void openLogFile() {
	try {
		log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile.getAbsolutePath(), true), "UTF-8"));//$NON-NLS-1$
		if (newSession) {
			writeHeader();
			newSession = false;
		}
	} catch (IOException e) {
		// there was a problem opening the log file so log to the console
		log = logForStream(System.err);
	}
}
protected void writeHeader() throws IOException {
	write(SESSION);
	writeSpace();
	String date = getDate();
	write(date);
	writeSpace();
	for (int i=SESSION.length()+date.length(); i<78; i++) {
		write("-");//$NON-NLS-1$
	}
	writeln();

	// Write out certain values found in System.getProperties()
	try {
		String key = "java.fullversion";//$NON-NLS-1$
		String value = System.getProperty(key);
		if (value == null) {
			key = "java.version";//$NON-NLS-1$
			value = System.getProperty(key);
			writeln(key + "=" + value);//$NON-NLS-1$
			key = "java.vendor";//$NON-NLS-1$
			value = System.getProperty(key);
			writeln(key + "=" + value);//$NON-NLS-1$
		} else {
			writeln(key + "=" + value);//$NON-NLS-1$
		}
	} catch (Exception e) {
		// If we're not allowed to get the values of these properties
		// then just skip over them.
	}

	// The Bootloader has some information that we might be interested in.
	write("BootLoader constants: OS=" + Platform.getOS());//$NON-NLS-1$
	write(", ARCH=" + Platform.getOSArch());//$NON-NLS-1$
	write(", WS=" + Platform.getWS());//$NON-NLS-1$
	writeln(", NL=" + Platform.getNL());//$NON-NLS-1$

	// Add the command-line arguments used to envoke the EPIC.
	String[] args = Platform.getCommandLineArgs();
	if (args != null && args.length > 0) {
		write("Command-line arguments:");//$NON-NLS-1$
		for (int i=0; i<args.length; i++) {
			//mask out the password argument for security
//			if (i > 0 && InternalPlatform.PASSWORD.equals(args[i-1]))
//				write(" (omitted)");//$NON-NLS-1$
//			else
				write(" " + args[i]);//$NON-NLS-1$
		}
		writeln();
	}

}
protected String getDate() {
	try {
		DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SS"); //$NON-NLS-1$
		return formatter.format(new Date());
	} catch (Exception e) {
		// If there were problems writing out the date, ignore and
		// continue since that shouldn't stop us from losing the rest
		// of the information
	}
	return Long.toString(System.currentTimeMillis());
}
protected Writer logForStream(OutputStream output) {
	try {
		return new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));//$NON-NLS-1$
	} catch (UnsupportedEncodingException e) {
		return new BufferedWriter(new OutputStreamWriter(output));
	}
}
/**
 * Writes the given string to the log, followed by the line terminator string.
 */
protected void writeln(String s) throws IOException {
	write(s);
	writeln();
}
/**
 * Shuts down the EPIC log.
 */
public synchronized void shutdown() {
	try {
		if (logFile != null) {
			closeLogFile();
			logFile = null;
		} else {
			if (log != null) {
				Writer old = log;
				log = null;
				old.flush();
				old.close();
			}
		}
	} catch (Exception e) {
		//we've shutdown the log, so not much else we can do!
		e.printStackTrace();
	}
}

protected void write(Throwable throwable) throws IOException {
	if (throwable == null)
		return;
	write(STACK);
	writeSpace();
	boolean isCoreException = throwable instanceof CoreException;
	if (isCoreException)
		writeln("1");//$NON-NLS-1$
	else
		writeln("0");//$NON-NLS-1$
	throwable.printStackTrace(new PrintWriter(log));
	if (isCoreException) {
	 CoreException e = (CoreException) throwable;
	 write(e.getStatus(), 0);
	}
}


protected void write(IStatus status, int depth) throws IOException {
	if (depth == 0) {
		write(ENTRY);
	} else {
		write(SUBENTRY);
		writeSpace();
		write(Integer.toString(depth));
	}
	writeSpace();
	write(status.getPlugin());
	writeSpace();
	switch (status.getSeverity())
	{
		case IStatus.OK: write("**OK**"); break;
		case IStatus.INFO: write("**Info**"); break;
		case IStatus.WARNING: write("**Warning**"); break;
		case IStatus.ERROR: write("**Error**"); break;
		default: write("**?"+status.getSeverity()+"?**");
	}
	writeSpace();
	write(Integer.toString(status.getCode()));
	writeSpace();
	write(getDate());
	writeln();

	write(MESSAGE);
	writeSpace();
	writeln(status.getMessage());

	write(status.getException());

	if (status.isMultiStatus()) {
		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++) {
			write(children[i], depth+1);
		}
	}
}

protected void writeln() throws IOException {
	write(LINE_SEPARATOR);
}
protected void write(String message) throws IOException {
	if (message != null)
		log.write(message);
}
protected void writeSpace() throws IOException {
	write(" ");//$NON-NLS-1$
}
}