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
package org.eclipse.jubula.client.analyze.impl.standard.context;

import org.eclipse.jubula.client.analyze.definition.IContext;
import org.eclipse.jubula.client.core.model.ISpecTestCasePO;

/**
 * 
 * @author volker
 *
 */
public class SpecTestCaseCX implements IContext {

    /**
     * {@inheritDoc}
     */
    public boolean isActive(Object obj) {
        // checks if the given Object is a userspecified TestCase
        if (obj instanceof ISpecTestCasePO) {
            return true;
        }
        return false;
    }

}
