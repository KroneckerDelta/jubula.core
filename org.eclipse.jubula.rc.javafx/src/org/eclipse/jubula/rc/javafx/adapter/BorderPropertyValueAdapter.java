/*******************************************************************************
 * Copyright (c) 2015 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.jubula.rc.javafx.adapter;

import java.util.Iterator;

import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;

import org.eclipse.jubula.rc.common.adaptable.IPropertyValue;

/**
 * @author BREDEX GmbH
 */
public class BorderPropertyValueAdapter 
    implements IPropertyValue<Border> {
    
    /** {@inheritDoc} */
    public String getStringRepresentation(Border b) {
        StringBuilder sb = new StringBuilder();
        Iterator<BorderStroke> strokeIterator = b.getStrokes().iterator();
        while (strokeIterator.hasNext()) {
            BorderStroke stroke = strokeIterator.next();
            sb.append("LeftStroke: "
                    + "Color=" + stroke.getLeftStroke().toString()
                    + ","
                    + "Style=" + stroke.getLeftStyle().toString()
            );
            sb.append("; ");
            sb.append("RightStroke: "
                    + "Color=" + stroke.getRightStroke().toString()
                    + ","
                    + "Style=" + stroke.getRightStyle().toString()
            );
            sb.append("; ");
            sb.append("TopStroke: "
                    + "Color=" + stroke.getTopStroke().toString()
                    + ","
                    + "Style=" + stroke.getTopStyle().toString()
            );
            sb.append("; ");
            sb.append("BottomStroke: "
                    + "Color=" + stroke.getBottomStroke().toString()
                    + ","
                    + "Style=" + stroke.getBottomStyle().toString()
            );
            if (strokeIterator.hasNext()) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }
}