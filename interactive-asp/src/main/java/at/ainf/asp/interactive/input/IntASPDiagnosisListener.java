package at.ainf.asp.interactive.input;

import at.ainf.asp.antlr.IntASPOutputBaseListener;
import at.ainf.asp.antlr.IntASPOutputListener;
import at.ainf.asp.antlr.IntASPOutputParser;

import java.util.LinkedList;
import java.util.List;

/**
 * Listener that extracts diagnoses from the answer sets
 */
public class IntASPDiagnosisListener extends IntASPOutputBaseListener implements IntASPOutputListener {

    private List<List<String>> diagnoses = new LinkedList<List<String>>();

    private List<String> diagnosis;

    @Override
    public void enterDiagnosis(IntASPOutputParser.DiagnosisContext ctx) {
        this.diagnosis = new LinkedList<String>();
    }

    @Override
    public void enterDiagatom(IntASPOutputParser.DiagatomContext ctx) {
        this.diagnosis.add(ctx.getText());
    }

    @Override
    public void exitDiagnosis(IntASPOutputParser.DiagnosisContext ctx) {
        getDiagnoses().add(this.diagnosis);
    }

    public List<List<String>> getDiagnoses() {
        return diagnoses;
    }
}
