/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser;


import at.ainf.choco2.model.ConstraintTheory;
import choco.kernel.model.Model;
import choco.kernel.solver.Solver;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import java.io.File;
import java.io.IOException;

public class ConstraintReader {
    public ConstraintTheory getConstraints(String input, Solver solver, Model model) throws ConstraintReaderException {
        ConstraintLexer lexer = new ConstraintLexer(new ANTLRStringStream(input));
        ConstraintParser parser = new ConstraintParser(new CommonTokenStream(lexer));
        return parse(parser, solver, model);
    }

    public ConstraintTheory getConstraints(File input, Solver solver, Model model) throws IOException, ConstraintReaderException {
        ANTLRFileStream fileStream = new ANTLRFileStream(input.getPath());
        ConstraintLexer lexer = new ConstraintLexer(fileStream);
        ConstraintParser parser = new ConstraintParser(new CommonTokenStream(lexer));
        return parse(parser, solver, model);
    }

    private ConstraintTheory parse(ConstraintParser parser, Solver solver, Model model) throws ConstraintReaderException {
        IParserHelper chocoParser = new Choco2ParserHelper(model);
        parser.setParserHelper(chocoParser);
        try{
            parser.expression();
        }
        catch (RecognitionException e) {
            throw new RuntimeException("All errors should be registered in Parser helper", e);
        }
        if (chocoParser.hasErrors())
            throw new ConstraintReaderException(chocoParser.getErrors());
        ConstraintTheory ct = new ConstraintTheory(solver, model);
        ct.addConstraints(chocoParser.getChangableConstraints());
        return ct;
    }
}
