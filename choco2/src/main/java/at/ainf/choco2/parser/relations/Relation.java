/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser.relations;

import at.ainf.choco2.parser.ChocoParserException;
import at.ainf.choco2.parser.IValue;
import choco.kernel.model.constraints.Constraint;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;

import java.util.List;
import java.util.Map;

public interface Relation {
    public void setLeft(IValue left);

    public void setRight(IValue right);

    public void setStrings(Map<String, List<String>> strings);

    public Constraint getConstraint(IntStream st) throws RecognitionException;

}
