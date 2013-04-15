/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser;

import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: Aug 4, 2009
 * Time: 3:02:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChocoParserException extends RecognitionException {
    private String message;

    public ChocoParserException(IntStream input, String message)
    {
        super(input);
        this.message = message;
    }
}
