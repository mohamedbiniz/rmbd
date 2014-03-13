package at.ainf.asp.interactive.input;

import at.ainf.asp.antlr.IntASPOutputBaseListener;
import at.ainf.asp.antlr.IntASPOutputListener;
import at.ainf.asp.antlr.IntASPOutputParser;

import java.util.LinkedList;
import java.util.List;

/**
 * Reading interpretations
 */
public class IntASPInterpretationListener extends IntASPOutputBaseListener implements IntASPOutputListener {

    private List<List<String>> interpretations = new LinkedList<List<String>>();

    private List<String> interpretation;

    @Override
    public void enterInterpretation(IntASPOutputParser.InterpretationContext ctx) {
        this.interpretation = new LinkedList<String>();
    }

    @Override
    public void exitInterpretation(IntASPOutputParser.InterpretationContext ctx) {
        getInterpretations().add(this.interpretation);
    }

    private List<List<String>> getInterpretations() {
        return this.interpretations;
    }

    @Override
    public void enterIntliteral(IntASPOutputParser.IntliteralContext ctx) {
        this.interpretation.add(ctx.getText());
    }
}
