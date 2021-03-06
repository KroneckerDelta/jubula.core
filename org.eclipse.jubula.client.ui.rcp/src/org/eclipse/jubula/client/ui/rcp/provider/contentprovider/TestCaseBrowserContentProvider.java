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
package org.eclipse.jubula.client.ui.rcp.provider.contentprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jubula.client.core.model.IExecTestCasePO;
import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.model.IReusedProjectPO;
import org.eclipse.jubula.client.core.model.ISpecObjContPO;
import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.core.persistence.ProjectPM;
import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
import org.eclipse.jubula.tools.internal.exception.JBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BREDEX GmbH
 * @created 08.10.2004
 */
public class TestCaseBrowserContentProvider 
    extends AbstractTreeViewContentProvider {

    /** the logger */
    private static final Logger LOG = 
            LoggerFactory.getLogger(TestCaseBrowserContentProvider.class);
    
    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ISpecObjContPO[]) {
            return new Object[] { ((ISpecObjContPO[])parentElement)[0] };
        }

        if (parentElement instanceof ISpecObjContPO) {
            ISpecObjContPO specObjects = (ISpecObjContPO)parentElement;
            List<Object> elements = new ArrayList<Object>();
            elements.addAll(specObjects.getSpecObjList());
            IProjectPO activeProject = 
                    GeneralStorage.getInstance().getProject();
            if (activeProject != null) {
                elements.addAll(activeProject.getUsedProjects());
            } else {
                LOG.error(Messages.TestCaseBrowser_NoActiveProject);
            }
            return elements.toArray();
        }
        
        if (parentElement instanceof IExecTestCasePO) {
            ISpecTestCasePO referencedTestCase = 
                ((IExecTestCasePO)parentElement).getSpecTestCase();
            if (referencedTestCase != null) {
                return ArrayUtils.addAll(
                        Collections.unmodifiableCollection(
                                referencedTestCase.getAllEventEventExecTC())
                                .toArray(), referencedTestCase
                                .getUnmodifiableNodeList().toArray());
            }
            
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }

        if (parentElement instanceof INodePO) {
            INodePO parentNode = ((INodePO) parentElement);
            Object[] children = parentNode.getUnmodifiableNodeList().toArray();
            if (parentElement instanceof ISpecTestCasePO) {
                children = ArrayUtils.addAll(
                        Collections.unmodifiableCollection(
                                ((ISpecTestCasePO) parentElement)
                                        .getAllEventEventExecTC()).toArray(),
                        children);
            }
            return children;
        }
        
        if (parentElement instanceof IReusedProjectPO) {
            try {
                IProjectPO reusedProject = 
                    ProjectPM.loadReusedProjectInMasterSession(
                            (IReusedProjectPO)parentElement);

                if (reusedProject != null) {
                    return reusedProject.getSpecObjCont()
                        .getSpecObjList().toArray();
                }

                return ArrayUtils.EMPTY_OBJECT_ARRAY;
            } catch (JBException e) {
                ErrorHandlingUtil.createMessageDialog(e, null, null);
                return ArrayUtils.EMPTY_OBJECT_ARRAY;
            }
        }
        
        return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }  
}