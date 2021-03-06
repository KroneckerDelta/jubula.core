/*******************************************************************************
 * Copyright (c) 2013 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.rc.javafx.listener;

import java.util.Map;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import org.eclipse.jubula.communication.internal.message.ObjectMappedMessage;
import org.eclipse.jubula.rc.common.AUTServer;
import org.eclipse.jubula.rc.common.exception.NoIdentifierForComponentException;
import org.eclipse.jubula.rc.common.logger.AutServerLogger;
import org.eclipse.jubula.rc.common.util.PropertyUtil;
import org.eclipse.jubula.rc.javafx.util.NodeBounds;
import org.eclipse.jubula.tools.internal.exception.CommunicationException;
import org.eclipse.jubula.tools.internal.objects.IComponentIdentifier;

/**
 * Realizes Object-Mapping.
 *
 * @author BREDEX GmbH
 * @created 10.10.2013
 */
public class MappingListener extends AbstractFXAUTEventHandler {

    /** the logger */
    private static AutServerLogger log = new AutServerLogger(
            MappingListener.class);

    /** The mouse move threshold in ms **/
    private static final long THRESHOLD = 100;

    /** The delta x for mouse move **/
    private static final double DX = 0;

    /** The delta x for mouse move **/
    private static final double DY = 0;
    /** The mouse move handler **/
    private MouseMoveDone m_mHandler = new MouseMoveDone(THRESHOLD, DX, DY);

    /** The highlight handler **/
    private HighlightHandler m_hHandler = new HighlightHandler();

    /** The key input handler **/
    private InputMappingHandler m_iHandler = new InputMappingHandler();

    @Override
    public void addHandler(Stage s) {
        s.addEventFilter(MouseEvent.MOUSE_CLICKED, m_iHandler);
        s.addEventFilter(MouseEvent.MOUSE_MOVED, m_mHandler);
        s.addEventFilter(KeyEvent.KEY_PRESSED, m_iHandler);

        m_mHandler.addMoveDoneHandler(m_hHandler);
    }

    @Override
    public void removeHandler(Stage s) {
        s.removeEventFilter(MouseEvent.MOUSE_MOVED, m_mHandler);
        s.removeEventFilter(MouseEvent.MOUSE_CLICKED, m_iHandler);
        s.removeEventFilter(KeyEvent.KEY_PRESSED, m_iHandler);

        m_mHandler.removeMoveDoneHandler(m_hHandler);
    }

    /**
     * Private class for handling "mouse-move-done-events"
     */
    private class HighlightHandler implements EventHandler<WorkerStateEvent> {

        @Override
        public void handle(WorkerStateEvent workerEvent) {
            MouseEvent event = (MouseEvent) workerEvent.getSource().getValue();
            Point2D pos = new Point2D(event.getScreenX(), event.getScreenY());
            Node currN = getCurrentNode();
            if (currN != null) {
                if (!(NodeBounds.checkIfContains(pos, currN))) {
                    lowlightCurrentNode();
                    setCurrentNode(null);
                }
            }

            Node n = ComponentHandler.getComponentByPos(pos);

            if (n != null && n != currN) {

                setCurrentNode(n);
                highlightCurrentNode();
            }
        }

    }

    /**
     * Private class for handling keyboard events
     */
    private class InputMappingHandler implements EventHandler<InputEvent> {

        @Override
        public void handle(InputEvent event) {
            Node currNode = getCurrentNode();
            if (currNode != null
                    && KeyAcceptor.accept(event) == KeyAcceptor.
                    MAPPING_KEY_COMB) {
                IComponentIdentifier id;
                try {
                    id = ComponentHandler.getIdentifier(currNode);
                    if (log.isInfoEnabled()) {
                        log.info("send a message with identifier for the component '" //$NON-NLS-1$
                                + id + "'"); //$NON-NLS-1$
                    }
                    
                    Map<String, String> componentProperties = PropertyUtil
                            .getMapOfComponentProperties(currNode);
                    id.setComponentPropertiesMap(componentProperties);
                    // send a message with the identifier of the selected
                    // component
                    ObjectMappedMessage message = new ObjectMappedMessage();
                    message.setComponentIdentifier(id);
                    AUTServer.getInstance().getCommunicator().send(message);
                } catch (NoIdentifierForComponentException nifce) {
                    // no identifier for the component, log this as an error
                    log.error("no identifier for '" + currNode); //$NON-NLS-1$
                } catch (CommunicationException ce) {
                    log.error(ce);
                    // do nothing here: a closed connection is handled by the
                    // AUTServer
                }
            }
        }

    }
}
