package at.ainf.asp.interactive.input;

import at.ainf.asp.antlr.IntASPOutputBaseListener;
import at.ainf.asp.antlr.IntASPOutputListener;
import at.ainf.asp.antlr.IntASPOutputParser;

import java.util.*;

/**
 * Listener that extracts diagnoses from the answer sets
 */
public class IntASPDiagnosisListener extends IntASPOutputBaseListener implements IntASPOutputListener, ASPListener {

    private List<Set<String>> diagnoses = new LinkedList<Set<String>>();

    private Set<String> diagnosis;

    @Override
    public void enterDiagnosis(IntASPOutputParser.DiagnosisContext ctx) {
        this.diagnosis = new HashSet<String>();
    }

    @Override
    public void enterDiagatom(IntASPOutputParser.DiagatomContext ctx) {
        this.diagnosis.add(ctx.getText());
    }

    @Override
    public void exitDiagnosis(IntASPOutputParser.DiagnosisContext ctx) {
        getDiagnoses().add(this.diagnosis);
    }

    public List<Set<String>> getDiagnoses() {
        return diagnoses;
    }

    @Override
    public boolean hasResult() {
        return !getDiagnoses().isEmpty();
    }
}
