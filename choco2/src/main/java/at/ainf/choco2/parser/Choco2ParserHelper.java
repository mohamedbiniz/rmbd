/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser;

import static at.ainf.choco2.parser.ConstraintParser.*;

import at.ainf.choco2.parser.relations.*;
import choco.Choco;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: Jul 31, 2009
 * Time: 12:49:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Choco2ParserHelper implements IParserHelper {

    private Map<String, Variable> vars = new HashMap<String, Variable>();
    private Map<String, List<String>> strings = new HashMap<String, List<String>>();
    private List<Constraint> changable = new LinkedList<Constraint>();
    private List<Constraint> background = new LinkedList<Constraint>();
    private final Model model;
    private static final Logger logger = LoggerFactory.getLogger(Choco2ParserHelper.class);
    private Map<Constraint, String> nameMap = new HashMap<Constraint, String>();


    public String getName(Constraint con) {
        return this.nameMap.get(con);
    }

    public Choco2ParserHelper(Model md) {
        this.model = md;
    }

    public Map<String, Variable> getVariables() {
        return this.vars;
    }

    public Collection<Constraint> getChangableConstraints() {
        return this.changable;
    }

    public Map<String, List<String>> getStringValuesMap() {
        return this.strings;
    }

    public boolean removeBGConstraint(int index) {
        return this.background.remove(index) != null;
    }

    public boolean removeCHConstraint(int index) {
        return this.changable.remove(index) != null;
    }


    public Collection<Constraint> getBackgroundConstraints() {
        return this.background;
    }

    public Model getModel() {
        return this.model;
    }

    public Constraint createFixConstraint(String value) {
        if (Boolean.valueOf(value))
            return Choco.TRUE;
        else
            return Choco.FALSE;
    }

    private  List<RecognitionException> errors = new LinkedList<RecognitionException>();

    public void addError(RecognitionException e) {
        this.errors.add(e);
    }

    public List<RecognitionException> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    private Double getDouble(Token t) {
        return Double.valueOf(t.getText());
    }

    private Integer getInteger(Token t) {
        return Integer.valueOf(t.getText());
    }


    private Boolean getBoolean(Token t) {
        return Boolean.valueOf(t.getText());
    }

    private int[] toArray(List<Token> list) {
        int[] res = new int[list.size()];
        int i = 0;
        for (Token in : list) {
            res[i] = getInteger(in);
            i++;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Array: " + Arrays.toString(res));
        }
        return res;
    }

    private int[] toStringArray(List<String> list) {
        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = i;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("StringArray: " + Arrays.toString(res));
        }
        return res;
    }

    private Constraint[] toConstraintArray(List<Constraint> list) {
        Constraint[] res = new Constraint[list.size()];
        list.toArray(res);
        if (logger.isTraceEnabled()) {
            logger.trace("ConstraintArray: " + Arrays.toString(res));
        }
        return res;
    }


    public Constraint createConstraint(IntStream st, int type, IValue left, IValue right) throws RecognitionException {
        Relation rel;
        switch (type) {
            case EQUALS:
                rel = new EqualsRelation();
                break;
            case NOTEQUALS:
                rel = new NotEqualsRelation();
                break;
            case LT:
                rel = new LTRelation();
                break;
            case GT:
                rel = new GTRelation();
                break;
            case LTEQ:
                rel = new LTEQRelation();
                break;
            case GTEQ:
                rel = new GTEQRelation();
                break;
            default:
                String message = "The given relation type " + type + " is unknown!";
                logger.error(message);
                throw new ChocoParserException(st, message);
        }
        rel.setLeft(left);
        rel.setRight(right);
        rel.setStrings(this.strings);
        return rel.getConstraint(st);
    }

    public IValue createIntValue(Token i) {
        return new ParserValue(INTEGER, getInteger(i));
    }

    public IValue createRealValue(Token i) {
        return new ParserValue(FLOAT, getDouble(i));
    }

    public IValue createBooleanValue(Token i) {
        return new ParserValue(BOOLEAN, getBoolean(i));
    }

    public IValue createStringValue(Token i) {
        return new ParserValue(STRING, i.getText().replaceAll("'", ""));
    }

    public IValue createIdentValue(Token i) {
        return new ParserValue(IDENT, vars.get(i.getText()), i.getText());
    }

    /**
     * Creates a cp:enum integer variable by default ands this to the chache of vars.
     * If the variable exists a runtime excpetion is thrown.
     *
     * @param name of the variable to
     * @param up   upper bound
     * @param low  lower bound
     */
    public void addIntVar(String name, Token low, Token up) {
        IntegerVariable var = Choco.makeIntVar(name, getInteger(low), getInteger(up));
        checkVariable(name, var);
        if (logger.isInfoEnabled())
            logger.info("Adding integer variable " + name + " [" + getInteger(low) + "," + getInteger(up) + "]");
        getModel().addVariable(var);
    }

    private Variable checkVariable(String name, Variable var) {
        if (vars.get(name) != null) {
            if (logger.isInfoEnabled())
                logger.info("Redeclaration of a variable " + name + " is ignored");
            return var;
        }
        vars.put(name, var);
        return var;
    }

    public void addIntVar(String name, List<Token> list) {
        int[] ints = toArray(list);
        IntegerVariable var = Choco.makeIntVar(name, ints);
        checkVariable(name, var);
        if (logger.isInfoEnabled())
            logger.info("Adding integer enum variable " + name + " [" + Arrays.toString(ints) + "]");
        getModel().addVariable(var);
    }

    public void addReadVar(String name, Token low, Token up) {
        RealVariable var = Choco.makeRealVar(name, getDouble(low), getDouble(up));
        checkVariable(name, var);
        if (logger.isInfoEnabled())
            logger.info("Adding real variable " + name + " [" + getDouble(low) + "," + getDouble(up) + "]");
        getModel().addVariable(var);
    }

    public void addBoolVar(String name) {
        IntegerVariable var = Choco.makeBooleanVar(name);
        checkVariable(name, var);
        if (logger.isInfoEnabled())
            logger.info("Adding boolean variable " + name);
        getModel().addVariable(var);
    }

    public void addStringIntVar(String name, List<String> list) {
        IntegerVariable var = Choco.makeIntVar(name, 0, list.size() - 1, "cp:enum");
        checkVariable(name, var);
        this.strings.put(name, list);
        if (logger.isInfoEnabled())
            logger.info("Adding string set variable " + name + " " + list);
        getModel().addVariable(var);
    }

    public void addConstraint(String name, char type, Constraint con) {

        boolean res;
        if (logger.isInfoEnabled())
            logger.info("Adding constraint " + name + " : " + con.pretty());
        switch (type) {
            case UNCHACNGABLE:
                res = background.add(con);
                break;
            case CHACNGABLE:
                res = changable.add(con);
                break;
            default:
                throw new IllegalArgumentException("Unknown constraint type: " + type + "!");
        }
        if (!res)
            throw new RuntimeException("Duplicate constraints detected!");

        nameMap.put(con, name);


    }

    public Constraint implies(Constraint body, Constraint head) {
        if (logger.isTraceEnabled())
            logger.trace("Creating implication: " + head.pretty() + " -> " + body.pretty());
        return Choco.implies(body, head);
    }

    public Constraint or(List<Constraint> list) {
        if (logger.isTraceEnabled()) {
            logger.trace("Creating disjunction: " + convertList(list));
        }
        return Choco.or(toConstraintArray(list));
    }

    private String convertList(List<Constraint> list) {
        StringBuffer buf = new StringBuffer(100);
        for (Constraint con : list)
            buf.append(con.pretty());
        return buf.toString();
    }

    public Constraint and(List<Constraint> list) {
        if (logger.isTraceEnabled())
            logger.trace("Creating conjunction: " + convertList(list));
        return Choco.and(toConstraintArray(list));
    }

    public Constraint negate(Constraint con) {
        if (logger.isTraceEnabled())
            logger.trace("Creating negation: " + con.pretty());
        return Choco.not(con);
    }

}
