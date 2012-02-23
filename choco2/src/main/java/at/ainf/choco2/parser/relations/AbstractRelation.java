/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser.relations;

import at.ainf.choco2.parser.ChocoParserException;
import at.ainf.choco2.parser.ConstraintParser;

import static at.ainf.choco2.parser.ConstraintParser.*;

import at.ainf.choco2.parser.IValue;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetVariable;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public abstract class AbstractRelation implements Relation {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(AbstractRelation.class);

    protected IValue left;

    protected IValue right;

    private Map<String, List<String>> strings;

    public void setStrings(Map<String, List<String>> strings) {
        this.strings = strings;
    }

    public Constraint getConstraint(IntStream st) throws RecognitionException {
        Constraint res;
        if (!left.is(IDENT)) {
            if (!right.is(IDENT)) {
                throw new ChocoParserException(st, "A constraint should contain at least one variable!");
            }

            IValue save = left;
            left = right;
            right = save;
        }

        switch (right.getType()) {
            case IDENT:
                if (left.is(INTEGER))
                    res = createConstraint(left.getIntVar(), right.getIntVar());
                else if (left.is(FLOAT))
                    res = createConstraint(left.getRealVar(), right.getRealVar());
                else if (left.is(IDENT)) {
                    if (left.isInteger())
                        res = createConstraint(left.getIntVar(), right.getIntVar());
                    else
                        res = createConstraint(left.getRealVar(), right.getRealVar());
                } else
                    throw new ChocoParserException(st, "Unknown definition! Only integers, floats and variable identifiers are supported.");
                break;

            case STRING:
                if (!isEnumAcceptable()) {
                    String message = "Enumerations are not acceptable with " + getRelationString() + " relation";
                    logger.error(message);
                    throw new ChocoParserException(st, message);
                }

                int index = getStringIndex(st, left, right.getString());
                res = createConstraint(left.getIntVar(), index);
                break;

            case ConstraintParser.BOOLEAN:
                if (!isEnumAcceptable()) {
                    String message = "Enumerations are not acceptable with " + getRelationString() + " relation";
                    logger.error(message);
                    throw new ChocoParserException(st, message);
                }
                index = (right.getBoolean()) ? 1 : 0;
                res = createConstraint(left.getIntVar(), index);
                break;

            case INTEGER:
                res = createConstraint(left.getIntVar(), right.getInteger());
                break;
            case FLOAT:
                res = createConstraint(left.getRealVar(), right.getReal());
                break;

            default:
                throw new ChocoParserException(st, "Unknown definition! Only integers, floats and variable identifiers are supported.");
        }
        if (logger.isTraceEnabled())
            logger.trace("Created constraint " + res.pretty());
        return res;
    }

    private int getStringIndex(IntStream is, IValue pv, String st) throws RecognitionException {
        List<String> list = strings.get(pv.getName());
        if (list == null) {
            String message = "Declaration of the string variable " + pv.getName() + " was not found.";
            logger.error(message);
            throw new ChocoParserException(is, message);
        }
        int index = list.indexOf(st);
        if (index < 0) {
            String message = "Value " + st + " of the string variable " + pv.getName() + " was not declared. ";
            logger.error(message);
            throw new ChocoParserException(is, message);
        }
        return index;
    }

    public abstract boolean isEnumAcceptable();

    public abstract String getRelationString();

    public abstract Constraint createConstraint(IntegerVariable variable, int number);

    public abstract Constraint createConstraint(RealVariable variable, double number);

    public abstract Constraint createConstraint(IntegerVariable variable1, IntegerVariable variable2);

    public abstract Constraint createConstraint(RealVariable variable1, RealVariable variable2);

    public abstract Constraint createConstraint(SetVariable variable1, SetVariable variable2);

    public void setLeft(IValue left) {
        this.left = left;
    }

    public void setRight(IValue right) {
        this.right = right;
    }

}
