 
/*******************************************************************************
 * Copyright (c) 2006, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.app.testexec.core;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jubula.client.cmd.AbstractCmdlineClient;
import org.eclipse.jubula.client.cmd.JobConfiguration;
import org.eclipse.jubula.client.cmd.constants.ClientStrings;
import org.eclipse.jubula.client.cmd.i18n.Messages;
import org.eclipse.jubula.client.core.businessprocess.ClientTestStrings;
import org.eclipse.jubula.client.core.constants.Constants;
import org.eclipse.jubula.client.core.constants.TestExecutionConstants;
import org.eclipse.jubula.tools.internal.constants.StringConstants;
import org.eclipse.jubula.tools.internal.exception.CommunicationException;
import org.eclipse.jubula.tools.internal.exception.JBFatalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @created Mar 21, 2006
 */
public class TestexecClient extends AbstractCmdlineClient {
    /** log facility */
    private static Logger log = LoggerFactory.getLogger(TestexecClient.class);

    /** the instance */
    private static AbstractCmdlineClient instance = null;

    /**
     * private constructor
     */
    private TestexecClient() {
        //no public constructor for this class
    }

    /**
     * Method to get the single instance of this class.
     * 
     * @return the instance of this Singleton
     */
    public static AbstractCmdlineClient getInstance() {
        if (instance == null) {
            instance = new TestexecClient();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    protected void preRun() {
        ExecutionController.getInstance().setJob(getJob());
    }
    /**
     * initializes the client
     * @return int
     *      Exit Code
     */
    public int doRun() {
        int exitCode = EXIT_CODE_OK;
        try {            
            // initializing execution controller
            ExecutionController controller = ExecutionController.getInstance();

            // start job
            if (!controller.executeJob()) {
                exitCode = EXIT_CODE_ERROR;
            }
        } catch (CommunicationException e) {
            log.error(e.getLocalizedMessage(), e);
            printlnConsoleError(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error(e.getLocalizedMessage(), e);
            printlnConsoleError(e.getMessage());
        } catch (JBFatalException e) {
            log.error(e.getLocalizedMessage(), e);
            printlnConsoleError(e.getMessage());
        } catch (Throwable t) {
            log.error(ClientStrings.ERR_UNEXPECTED, t);
            printlnConsoleError(t.getMessage());
        }
        shutdown();
        return exitCode;
    }
    
    
    /**
     * {@inheritDoc}
     */
    protected void extendOptions(Options options, boolean req) {
        options.addOption(createOption(ClientTestStrings.SERVER, true, 
                ClientTestStrings.HOSTNAME, 
                Messages.ClientServerOpt, false));
        options.addOption(createOption(ClientTestStrings.PORT, true, 
                ClientTestStrings.PORT_NUMBER, 
                Messages.ClientPortOpt, false)); 
        options.addOption(createOption(ClientTestStrings.PROJECT, true, 
                ClientTestStrings.PROJECT_NAME, 
                Messages.ClientProjectOpt, req));
        options.addOption(createOption(ClientTestStrings.PROJECT_VERSION, true, 
                ClientTestStrings.PROJECT_VERSION_EX, 
                Messages.ClientProjectVersionOpt, req));
        options.addOption(createOption(ClientTestStrings.LANGUAGE, true, 
                ClientTestStrings.LANGUAGE, 
                Messages.ClientLanguageOpt, req));
        options.addOption(createOption(ClientTestStrings.RESULTDIR, true, 
                ClientTestStrings.RESULTDIR, 
                Messages.ClientResultdirOpt, false));
        options.addOption(createOption(ClientStrings.RESULT_NAME, true, 
                ClientStrings.RESULT_NAME, 
                Messages.ClientResultnameOpt, false));
        // AUT option group (AUT Configuration / AUT ID)
        OptionGroup autOptionGroup = new OptionGroup();
        autOptionGroup.setRequired(false);
        autOptionGroup.addOption(createOption(
                ClientTestStrings.AUT_CONFIG, true, 
                ClientTestStrings.AUT_CONFIG, 
                Messages.ClientAutconfigOpt, req));
        autOptionGroup.addOption(createOption(ClientTestStrings.AUT_ID, true, 
                ClientTestStrings.AUT_ID, 
                Messages.ClientAutIdOpt, req));
        options.addOptionGroup(autOptionGroup);
        
        // Test execution type option group (Test Suite / Test Job)
        OptionGroup testExecutionGroup = new OptionGroup();
        testExecutionGroup.setRequired(req);
        testExecutionGroup.addOption(createOption(
                ClientTestStrings.TESTJOB, true, 
                ClientTestStrings.TESTJOB, 
                Messages.ClientTestJobOpt, req));         
        testExecutionGroup.addOption(createOption(
                ClientTestStrings.TESTSUITE, true, 
                ClientTestStrings.TESTSUITE, 
                Messages.ClientTestSuiteOpt, req));      
        options.addOptionGroup(testExecutionGroup);
        
        options.addOption(createOption(ClientTestStrings.DATA_DIR, true, 
            ClientTestStrings.DATA_DIR_EX, 
            Messages.ClientDataFile, false));
        Option noRunOption = createOption(ClientStrings.NORUN, true,
                ClientStrings.NORUN_MODE,
                Messages.ClientNoRunOpt, false);
        noRunOption.setOptionalArg(true);
        options.addOption(noRunOption);
        options.addOption(createOption(ClientTestStrings.AUTO_SCREENSHOT,
                false, StringConstants.EMPTY, Messages.ClientAutoScreenshot, 
                    false));
        options.addOption(createOption(ClientTestStrings.NO_XML_SCREENSHOT,
                false, StringConstants.EMPTY, Messages.ClientNoXmlScreenshot, 
                    false));
        options.addOption(createOption(ClientTestStrings.TIMEOUT, true,
                ClientTestStrings.TIMEOUT, Messages.ClientTimeout, 
                    false));  
        // server option for the CLC extension
        options.addOption(createOption(ClientTestStrings.STARTSERVER, true, 
                ClientTestStrings.PORT_NUMBER, Messages.ClientStartServerOpt, 
                    false));
    }
    
    /**
     * {@inheritDoc}
     */
    protected void extendValidate(JobConfiguration job, 
            StringBuilder errorMsgs, StringBuilder errorInvalidArgsMsg) {
        if (job.getProjectName() == null) {
            appendError(errorMsgs, ClientTestStrings.PROJECT, 
                    ClientTestStrings.PROJECT_NAME);
        }
        if (job.getProjectVersion() == null) {
            appendError(errorMsgs, ClientTestStrings.PROJECT_VERSION,
                    ClientTestStrings.PROJECT_VERSION_EX);
        }
        if (job.getProjectVersion() != null
                && job.getProjectVersion().getMajorNumber() == null
                && job.getProjectVersion().getVersionQualifier() == null) {
            appendValidationError(errorInvalidArgsMsg,
                    ClientTestStrings.PROJECT_VERSION,
                    ClientTestStrings.PROJECT_VERSION_EX);
        }
        if (job.getPort() == Constants.INVALID_VALUE) {
            appendError(errorMsgs, ClientTestStrings.PORT,
                    ClientTestStrings.PORT_NUMBER);
        }
        if ((job.getServer() != null) && (job.getPort() == 0)) {
            appendError(errorMsgs, ClientTestStrings.PORT, 
                    ClientTestStrings.PORT_NUMBER);
        }   
        if (job.getAutConfigName() == null && job.getAutId() == null
                && job.getTestJobName() == null) {
            appendError(errorMsgs, ClientTestStrings.AUT_CONFIG, 
                    ClientTestStrings.AUT_CONFIG);
            appendError(errorMsgs, ClientTestStrings.AUT_ID, 
                    ClientTestStrings.AUT_ID);
        }
        if (job.getLanguage() == null) {
            appendError(errorMsgs, ClientTestStrings.LANGUAGE, 
                    ClientTestStrings.LANGUAGE);
        }
        if (job.getTestSuiteNames().isEmpty() && job.getTestJobName() == null
                && job.getServerPort() == null) {
            appendError(errorMsgs, ClientTestStrings.TESTSUITE, 
                    ClientTestStrings.TESTSUITE);
            appendError(errorMsgs, ClientTestStrings.TESTJOB, 
                    ClientTestStrings.TESTJOB);
        }
        if (job.getTimeout() < 0) {
            appendError(errorMsgs, ClientTestStrings.TIMEOUT,
                    ClientTestStrings.TIMEOUT);
        }
        if ((!StringUtils.isEmpty(job.getNoRunOptMode()))
                && (job.getNoRunOptMode().equals(
                        TestExecutionConstants.EXIT_INVALID_ARGUMENT))) {
            appendValidationError(errorInvalidArgsMsg, ClientStrings.NORUN,
                    ClientStrings.NORUN_MODE);
        }
        if ((!StringUtils.isEmpty(job.getFileName()))
                && (job.getFileName()
                        .equals(TestExecutionConstants.
                                EXIT_INVALID_ARGUMENT))) {
            appendValidationError(errorInvalidArgsMsg,
                    ClientStrings.RESULT_NAME, ClientStrings.RESULT_NAME);
        }

    }
    
    /** {@inheritDoc} */
    public String getCmdlineClientExecName() {
        return org.eclipse.jubula.app.testexec.i18n.Messages.ClientNameShort;
    }
}