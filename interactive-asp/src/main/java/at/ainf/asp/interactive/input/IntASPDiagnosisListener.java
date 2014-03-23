package at.ainf.asp.interactive.input;

import at.ainf.asp.antlr.IntASPOutputBaseListener;
import at.ainf.asp.antlr.IntASPOutputListener;
import at.ainf.asp.antlr.IntASPOutputParser;

import java.util.*;

/**
 * Listener that extracts diagnosis candidates from the answer sets
 */
public class IntASPDiagnosisListener extends IntASPOutputBaseListener implements IntASPOutputListener, ASPListener {

    private Set<Set<String>> diagnosisCandidates = new HashSet<Set<String>>();

    private Set<String> diagnosisCandidate;

    @Override
    public void enterDiagnosis(IntASPOutputParser.DiagnosisContext ctx) {
        this.diagnosisCandidate = new HashSet<String>();
    }

    @Override
    public void enterDiagatom(IntASPOutputParser.DiagatomContext ctx) {
        this.diagnosisCandidate.add(ctx.getText());
    }

    @Override
    public void exitDiagnosis(IntASPOutputParser.DiagnosisContext ctx) {
        getDiagnosisCandidates().add(this.diagnosisCandidate);
    }


    @Override
    public boolean hasResult() {
        return !getDiagnosisCandidates().isEmpty();
    }

    public Set<Set<String>> getDiagnosisCandidates() {
        return diagnosisCandidates;
    }
}
