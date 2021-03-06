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
package org.eclipse.jubula.rc.javafx.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;

import org.eclipse.jubula.rc.common.components.AUTComponent;

/**
 * Wrapper for concrete JavaFX components.
 *
 * @author BREDEX GmbH
 * @created 10.10.2013
 */
public class JavaFXComponent extends AUTComponent<EventTarget> {
    /**
     * create an instance from a JavaFX component. This constructor is used when
     * working with real instances.
     *
     * @param component
     *            the JavaFX component
     */
    public JavaFXComponent(EventTarget component) {
        super(component);
        addChangeListener(component);
    }

    /**
     * Add a change Listener
     * 
     * @param component
     *            the component
     */
    public void addChangeListener(EventTarget component) {
        Object children = ChildrenGetter.getAsRealType(component);
        if (children instanceof ReadOnlyObjectProperty) {
            ChildrenListenerHelper.addListener(
                (ReadOnlyObjectProperty<? extends EventTarget>) children);
        } else if (children instanceof ObservableList) {
            ChildrenListenerHelper.addListener(
                (ObservableList<? extends EventTarget>) children);
        }
    }

    /**
     * Remove a change Listener
     */
    public void removeChangeListener() {
        Object children = ChildrenGetter.getAsRealType(getComponent());
        if (children instanceof ObjectProperty) {
            ChildrenListenerHelper.removeListener(
                (ObjectProperty<? extends EventTarget>) children);
        } else if (children instanceof ObservableList) {
            ChildrenListenerHelper.removeListener(
                (ObservableList<? extends EventTarget>) children);
        }
    }
}