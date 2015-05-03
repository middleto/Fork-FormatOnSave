/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.epic.perleditor.templates;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


//import net.sourceforge.phpdt.internal.corext.template.php.HTMLContextType;
import org.epic.perleditor.templates.perl.PerlContextType;
//import net.sourceforge.phpdt.internal.corext.template.php.PHPDocContextType;



/**
 * A singleton to keep track of all known context types.
 */
public class ContextTypeRegistry {

	/** the singleton */
	private static ContextTypeRegistry fInstance;
	
	/** all known context types */
	private final Map<String, ContextType> fContextTypes= new HashMap<String, ContextType>();
	
	/**
	 * Returns the single instance of this class.
	 */
	public static ContextTypeRegistry getInstance() {
		if (fInstance == null)
			fInstance= new ContextTypeRegistry();
			
		return fInstance;	
	}

	/**
	 * Adds a context type to the registry.
	 */	
	public void add(ContextType contextType) {
		fContextTypes.put(contextType.getName(), contextType);
	}
	
	/**
	 * Removes a context type from the registry.
	 */
	public void remove(ContextType contextType) {
		fContextTypes.remove(contextType.getName());
	}

	/**
	 * Returns the context type if the name is valid, <code>null</code> otherwise.
	 */
	public ContextType getContextType(String name) {
		return fContextTypes.get(name);
	}
	
	/**
	 * Returns an iterator over the registered context type names.
	 */
	public Iterator<String> iterator() {
		return fContextTypes.keySet().iterator();	
	}

	// XXX bootstrap with java and javadoc context types
	private ContextTypeRegistry() {
		add(new PerlContextType());
    //add(new PHPDocContextType());
		//add(new HTMLContextType());
	}

}
