package at.ainf.asp.interactive.input;

import at.ainf.asp.antlr.IntASPOutputBaseListener;
import at.ainf.asp.antlr.IntASPOutputListener;
import at.ainf.asp.antlr.IntASPOutputParser;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Reading interpretations
 */
public class IntASPInterpretationListener extends IntASPOutputBaseListener
        implements IntASPOutputListener, ASPListener {

    private List<Set<String>> interpretations = new LinkedList<Set<String>>();

    private Set<String> interpretation;

    @Override
    public void enterInterpretation(IntASPOutputParser.InterpretationContext ctx) {
        this.interpretation = new HashSet<String>();
    }

    @Override
    public void exitInterpretation(IntASPOutputParser.InterpretationContext ctx) {
        getInterpretations().add(this.interpretation);
    }

    public List<Set<String>> getInterpretations() {
        return this.interpretations;
    }

    @Override
    public void enterIntliteral(IntASPOutputParser.IntliteralContext ctx) {
        this.interpretation.add(ctx.getText());
    }

    @Override
    public boolean hasResult() {
        return !getInterpretations().isEmpty();
    }
}
