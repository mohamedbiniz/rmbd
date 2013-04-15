package at.ainf.choco2.parser;

import org.antlr.runtime.RecognitionException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 04.06.2010
 * Time: 19:32:08
 * To change this template use File | Settings | File Templates.
 */
public class ConstraintReaderException extends Exception{

    private final List<RecognitionException> errors = new LinkedList<RecognitionException>();

    public ConstraintReaderException(List<RecognitionException> e) {
        this.errors.addAll(e);
    }

    public List<RecognitionException> getErrors() {
        return errors;
    }
}
