/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser.relations;

import static choco.Choco.eq;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetVariable;

public class EqualsRelation extends AbstractRelation {

    @Override
    public Constraint createConstraint(IntegerVariable variable, int number) {
        return eq(variable, number);
    }

    @Override
    public Constraint createConstraint(RealVariable variable, double number) {
        return eq(variable, number);
    }

    @Override
    public Constraint createConstraint(IntegerVariable variable1, IntegerVariable variable2) {
        return eq(variable1, variable2);
    }

    @Override
    public Constraint createConstraint(RealVariable variable1, RealVariable variable2) {
        return eq(variable1, variable2);
    }

    @Override
    public Constraint createConstraint(SetVariable variable1, SetVariable variable2) {
        return eq(variable1, variable2);
    }

    @Override
    public String getRelationString() {
        return "EQUAL";
    }

    @Override
    public boolean isEnumAcceptable() {
        return true;
    }
}
