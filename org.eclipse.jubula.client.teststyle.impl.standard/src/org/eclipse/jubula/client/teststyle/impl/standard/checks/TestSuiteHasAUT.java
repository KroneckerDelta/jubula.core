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
package org.eclipse.jubula.client.teststyle.impl.standard.checks;

import java.util.Locale;

import org.eclipse.jubula.client.core.model.ITestSuitePO;
import org.eclipse.jubula.client.teststyle.checks.BaseCheck;
import org.eclipse.jubula.client.teststyle.impl.standard.i18n.Messages;
import org.eclipse.jubula.client.ui.rcp.businessprocess.WorkingLanguageBP;

/**
 * Check whether each Testsuite has an AUT defined
 */
public class TestSuiteHasAUT extends BaseCheck {

    @Override
    public String getDescription() {
        return Messages.TestSuiteHasAUTProblemDescription;
    }

    @Override
    public boolean hasError(Object obj) {
        if (obj instanceof ITestSuitePO) {
            ITestSuitePO ts = (ITestSuitePO) obj;
            WorkingLanguageBP wl = WorkingLanguageBP.getInstance();
            Locale workLang = wl.getWorkingLanguage();
            if (ts.getAut() != null && wl.isTestSuiteLanguage(workLang, ts)) {
                return false;
            }
        }
        return true;
    }

}
