/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser.relations;

import static choco.Choco.neq;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetVariable;

public class NotEqualsRelation extends AbstractRelation {

    public Constraint createConstraint(SetVariable variable1, SetVariable variable2) {
        return neq(variable1, variable2);
    }

    @Override
    public Constraint createConstraint(IntegerVariable variable, int number) {
        return neq(variable, number);
    }

    @Override
    public Constraint createConstraint(RealVariable variable, double number) {
        throw new RuntimeException("Choco does not support NOT-EQUAL relationship for real variables");
    }

    @Override
    public Constraint createConstraint(IntegerVariable variable1, IntegerVariable variable2) {
        return neq(variable1, variable2);
    }

    @Override
    public Constraint createConstraint(RealVariable variable1, RealVariable variable2) {
        throw new RuntimeException("Choco does not support NOT-EQUAL relationship for real variables");
    }

    @Override
    public String getRelationString() {
        return "NOT-EQUAL";
    }

    @Override
    public boolean isEnumAcceptable() {
        return true;
    }

}
