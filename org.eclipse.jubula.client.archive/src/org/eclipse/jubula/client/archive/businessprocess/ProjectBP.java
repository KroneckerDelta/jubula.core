/*******************************************************************************
 * Copyright (c) 2014, BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.archive.businessprocess;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jubula.client.archive.XmlStorage;
import org.eclipse.jubula.client.archive.i18n.Messages;
import org.eclipse.jubula.client.archive.output.NullImportOutput;
import org.eclipse.jubula.client.core.businessprocess.ComponentNamesDecorator;
import org.eclipse.jubula.client.core.businessprocess.INameMapper;
import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameCache;
import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameMapper;
import org.eclipse.jubula.client.core.businessprocess.ParamNameBP;
import org.eclipse.jubula.client.core.businessprocess.ParamNameBPDecorator;
import org.eclipse.jubula.client.core.businessprocess.ProjectComponentNameMapper;
import org.eclipse.jubula.client.core.events.DataEventDispatcher;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.model.ProjectVersion;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.core.persistence.NodePM;
import org.eclipse.jubula.client.core.persistence.PMException;
import org.eclipse.jubula.client.core.persistence.PMReadException;
import org.eclipse.jubula.client.core.persistence.PMSaveException;
import org.eclipse.jubula.client.core.persistence.ProjectPM;
import org.eclipse.jubula.tools.internal.exception.JBVersionException;
import org.eclipse.jubula.tools.internal.exception.ProjectDeletedException;
import org.eclipse.jubula.tools.internal.jarutils.IVersion;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Business processes for the project
 *
 * @author BREDEX GmbH
 * @created Jan 23, 2014
 */
public class ProjectBP {
    
    /** standard logging */
    private static Logger log = LoggerFactory
            .getLogger(ProjectBP.class);
    
    /**
     * Operation for Create New Version action.
     * 
     * @author BREDEX GmbH
     * @created Dec 3, 2007
     */
    public static class NewVersionOperation implements IRunnableWithProgress {

        /** the total work for the operation */
        private static final int TOTAL_WORK = 100;

        /** the work for gathering project data from the database*/
        private static final int WORK_GET_PROJECT_FROM_DB = 5;
        
        /** the work for creating the domain objects for the project */
        private static final int WORK_PROJECT_CREATION = 10;
        
        /** the work for saving the project to the database */
        private static final int WORK_PROJECT_SAVE = 
            TOTAL_WORK - WORK_PROJECT_CREATION - WORK_GET_PROJECT_FROM_DB;
        
        /** The project from which to create a new version */
        private IProjectPO m_project;
        
        /** The new version */
        private ProjectVersion m_projectVersion;

        /**
         * Constructor
         * 
         * @param project The project from which to create a new version
         * @param newVersionNumbers the new version numbers and qualifier
         */
        public NewVersionOperation(IProjectPO project,
                ProjectVersion newVersionNumbers) {
            m_project = project;
            m_projectVersion = newVersionNumbers;
        }
        
        /** {@inheritDoc} */
        public void run(IProgressMonitor monitor) throws InterruptedException, 
            InvocationTargetException {
            String pName = m_project.getName();
            String pVersion = m_project.getVersionString();
            final SubMonitor subMonitor = SubMonitor.convert(
                    monitor, NLS.bind(Messages.
                        CreateNewProjectVersionOperationCreatingNewVersion,
                                    new Object[] { 
                                        m_projectVersion, pName, pVersion }),
                            TOTAL_WORK);
            final ParamNameBPDecorator paramNameMapper = 
                new ParamNameBPDecorator(ParamNameBP.getInstance());
            final IWritableComponentNameCache compNameCache = 
                new ComponentNamesDecorator(null);
            try {
                NodePM.getInstance().setUseCache(true);
                GeneralStorage.getInstance().validateProjectExists(m_project);
                InputStream xmlProjectInputStream = XmlStorage.save(m_project,
                    null, false, subMonitor.newChild(WORK_GET_PROJECT_FROM_DB),
                    false, null);
                if (monitor.isCanceled()) {
                    throw new InterruptedException();
                }
                final IProjectPO duplicatedProject = XmlStorage.load(
                        xmlProjectInputStream, false,
                        m_projectVersion.getMajorNumber(),
                        m_projectVersion.getMinorNumber(),
                        m_projectVersion.getMicroNumber(),
                        m_projectVersion.getVersionQualifier(),
                        paramNameMapper, compNameCache,
                        subMonitor.newChild(WORK_PROJECT_CREATION),
                        new NullImportOutput(), true);
                if (monitor.isCanceled()) {
                    throw new InterruptedException();
                }
                IWritableComponentNameMapper compNameMapper =
                    new ProjectComponentNameMapper(
                            compNameCache, duplicatedProject);
                
                duplicatedProject.setClientMetaDataVersion(
                    IVersion.JB_CLIENT_METADATA_VERSION);
                attachProjectWithProgress(
                        subMonitor.newChild(WORK_PROJECT_SAVE), 
                        paramNameMapper, compNameMapper, duplicatedProject);
            } catch (final PMSaveException e) {
                log.error(Messages.ErrorWhileCreatingNewProjectVersion, e);
                throw new InvocationTargetException(e);
            } catch (final PMReadException e) {
                log.error(Messages.ErrorWhileCreatingNewProjectVersion, e);
            } catch (PMException e) {
                log.error(Messages.ErrorWhileCreatingNewProjectVersion, e);
                throw new InvocationTargetException(e);
            } catch (ProjectDeletedException e) {
                throw new InvocationTargetException(e);
            } catch (JBVersionException e) {
                log.error(Messages.TKVersionCreatingNewProjectVersion);
            } finally {
                NodePM.getInstance().setUseCache(false);
                monitor.done();
            }
        }

        /**
         * Attaches the given project to the Master Session and database using
         * the given parameter name mapper. Reports progress during the
         * operation.
         * 
         * @param monitor
         *            The progress monitor for the operation.
         * @param paramNameMapper
         *            The parameter name mapper to use when adding the project
         *            to the database.
         * @param compNameMapper
         *            The component name mapper to use when adding the project
         *            to the database.
         * @param project
         *            The project to add to the database
         * @throws PMException
         *             in case of any db error
         * @throws ProjectDeletedException
         *             if project is already deleted
         * @throws InterruptedException
         *             if the operation was canceled.
         */
        private void attachProjectWithProgress(IProgressMonitor monitor,
                final ParamNameBPDecorator paramNameMapper,
                final IWritableComponentNameMapper compNameMapper,
                final IProjectPO project) throws PMException,
                ProjectDeletedException, InterruptedException {

            // We need to clear the current project data so 
            // we are in a known state if the operation is 
            // canceled.
            IProjectPO clearedProject = m_project;
            if (clearedProject != null) {
                DataEventDispatcher.getInstance().fireDataChangedListener(
                        clearedProject, DataState.Deleted, UpdateState.all);
            }
            List<INameMapper> mapperList = new ArrayList<INameMapper>();
            List<IWritableComponentNameMapper> compNameCacheList = 
                new ArrayList<IWritableComponentNameMapper>();
            mapperList.add(paramNameMapper);
            compNameCacheList.add(compNameMapper);
            ProjectPM.attachProjectToROSession(project, project.getName(), 
                    mapperList, compNameCacheList, monitor);
        }
    }
}
