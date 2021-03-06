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
package org.eclipse.jubula.tools.internal.xml.businessmodell;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This class represents an element of a set of predefined values. Predefined
 * value sets are used for parameters.
 * {@inheritDoc}
 * @author BREDEX GmbH
 * @created 15.08.2005
 */
public class ValueSetElement {
    /** The name (I18N key) of the element. */
    private String m_name;
    
    /** The value of the element. */
    private String m_value;

    /** Constructor. Used by deserialization only. */
    public ValueSetElement() {
        // That's ok, used by (de)serializers.
    }
    
    /**
     * The constructor.
     * @param name The name (I18N key) of the element.
     * @param value The value of the element.
     */
    public ValueSetElement(String name, String value) {
        super();
        m_name = name;
        m_value = value;
    }

    /** @return Returns the value. */
    public String getValue() {
        return m_value;
    }

    /** @return Returns the name. */
    public String getName() {
        return m_name;
    }
    
    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueSetElement)) {
            return false;
        }
        ValueSetElement rhs = (ValueSetElement)obj;
        return new EqualsBuilder().append(m_name, rhs.m_name).append(m_value,
            rhs.m_value).isEquals();
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        return new HashCodeBuilder().append(m_name).append(m_value)
            .toHashCode();
    }
}