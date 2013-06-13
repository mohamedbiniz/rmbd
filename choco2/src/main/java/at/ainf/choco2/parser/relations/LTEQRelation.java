/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser.relations;

import static choco.Choco.leq;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetVariable;


public class LTEQRelation extends AbstractRelation {

    @Override
    public Constraint createConstraint(IntegerVariable variable, int number) {
        return leq(variable, number);
    }

    @Override
    public Constraint createConstraint(RealVariable variable, double number) {
        return leq(variable, number);
    }

    @Override
    public Constraint createConstraint(IntegerVariable variable1, IntegerVariable variable2) {
        return leq(variable1, variable2);
    }

    @Override
    public Constraint createConstraint(RealVariable variable1, RealVariable variable2) {
        return leq(variable1, variable2);
    }

    @Override
    public Constraint createConstraint(SetVariable variable1, SetVariable variable2) {
        throw new RuntimeException("Choco does not support LESS-EQUAL relationship for set variables.");
    }

    @Override
    public String getRelationString() {
        return "LESS-EQUAL";
    }

    @Override
    public boolean isEnumAcceptable() {
        return false;
    }

}
