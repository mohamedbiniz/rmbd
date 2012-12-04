/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser;

import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: Jul 31, 2009
 * Time: 12:48:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IParserHelper {

    /**
     * Constraint types
     */

    public final char CHACNGABLE = 'c';
    public final char UNCHACNGABLE = 'u';

    public String getName(Constraint con);

    public void addIntVar(String name, Token up, Token low);

    public void addIntVar(String name, List<Token> list);

    public void addReadVar(String name, Token up, Token low);

    public void addBoolVar(String name);

    public void addStringIntVar(String name, List<String> list);

    public void addConstraint(String constraintName, char constraintType, Constraint con);

    public Constraint implies(Constraint con, Constraint con1);

    public Constraint or(List<Constraint> list);

    public Constraint and(List<Constraint> list);

    public Constraint negate(Constraint con);

    public Constraint createConstraint(IntStream st, int type, IValue obj, IValue obj1) throws RecognitionException;

    public IValue createIntValue(Token i);

    public IValue createRealValue(Token i);

    public IValue createBooleanValue(Token i);

    public IValue createStringValue(Token i);

    public IValue createIdentValue(Token i);

    public Collection<Constraint> getBackgroundConstraints();

    public Collection<Constraint> getChangableConstraints();

    public Map<String, Variable> getVariables();

    public Map<String, List<String>> getStringValuesMap();

    public Model getModel();

    public Constraint createFixConstraint(String value);

    public void addError(RecognitionException e);

    public List<RecognitionException> getErrors();

    public boolean hasErrors();
}
