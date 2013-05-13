/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser;

import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserValue implements IValue {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(ParserValue.class);

    private int type;
    private Object value;
    private String name;


    public ParserValue(int type, Object value) {
        this(type, value, null);
    }

    public ParserValue(int type, Object value, String name) {
        this.type = type;
        this.value = value;
        this.name = name;
    }

    public String getName() {
        if (!isIdentifier())
            throw new RuntimeException("Only values that correspond to variables have names!");
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    public boolean is(int type) {
        return getType() == type;
    }

    public boolean isIdentifier() {
        return getType() == ConstraintParser.IDENT;
    }

    public int getInteger() {
        return (Integer) getValue();
    }

    public double getReal() {
        return (Double) getValue();
    }

    public IntegerVariable getIntVar() {
        return (IntegerVariable) getValue();
    }

    public SetVariable getSetVar() {
        return (SetVariable) getValue();
    }

    public RealVariable getRealVar() {
        return (RealVariable) getValue();
    }

    public Object getValue() {
        if (logger.isTraceEnabled()) {
            logger.trace("getValue() - Getting a value: " + String.valueOf(this.value));
        }
        return this.value;
    }

    public boolean isInteger() {
        return getValue() instanceof IntegerVariable;
    }

    public String getString() {
        return (String) getValue();
    }

    public boolean getBoolean() {
        return (Boolean) getValue();
    }
}
