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

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: Aug 4, 2009
 * Time: 5:44:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IValue {
    public boolean is(int ident);

    public int getType();

    public IntegerVariable getIntVar();

    public RealVariable getRealVar();

    public String getString();

    public String getName();

    public boolean getBoolean();

    public int getInteger();

    public boolean isInteger();

    public double getReal();
}
