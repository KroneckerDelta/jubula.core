/*******************************************************************************
 * Copyright (c) 2012 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation 
 *******************************************************************************/
package org.eclipse.jubula.rc.common.tester;

import org.eclipse.jubula.rc.common.exception.StepExecutionException;
import org.eclipse.jubula.rc.common.tester.adapter.interfaces.ITextComponent;
import org.eclipse.jubula.rc.common.util.Verifier;

/**
 * This class represents the general implementation for components
 * which have readable text.
 * 
 * @author BREDEX GmbH
 */
public abstract class AbstractTextVerifiableTester extends WidgetTester {

    /**
     * Action to read the value of the current component 
     * to store it in a variable in the Client.<br>
     * If it is a complex component, it is always the selected object.
     * @param variable the name of the variable
     * @return the text value.
     */
    public String rcReadValue(String variable) {
        return ((ITextComponent)getComponent()).getText();
    }

    /**
     * Verifies the rendered text inside the currently component.<br>
     * If it is a complex component, it is always the selected object.
     * @param text The text to verify.
     * @param operator The operation used to verify
     * @throws StepExecutionException If the rendered text cannot be extracted.
     */
    public void rcVerifyText(String text, String operator)
        throws StepExecutionException {
        Verifier.match(((ITextComponent)getComponent()).getText(), text,
                                    operator);
    }

}
