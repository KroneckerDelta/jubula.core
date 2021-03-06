/*******************************************************************************
 * Copyright (c) 2004, 2012 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.analyze.impl.standard.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * i18n string internationalization
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.jubula.client.analyze.impl.standard.i18n.messages"; //$NON-NLS-1$

    public static String MonitorCanceled;
    public static String Running;
    public static String General;
    public static String AnalysingProjects;
    public static String ChainBeginning;
    public static String PossibleChildren;
    public static String ConstructChains;
    
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
