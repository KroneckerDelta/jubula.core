/*******************************************************************************
 * Copyright (c) 2004, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.launch.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author BREDEX GmbH
 * @created 13.07.2011
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.launch.i18n.messages"; //$NON-NLS-1$
    
    public static String LaunchAutError_NoAgentConnection;
    public static String GetAutIdFromLaunchConfigError;
    public static String GetToolkitFromLaunchConfigError;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    /**
     * Constructor
     */
    private Messages() {
        // hide
    }
}
