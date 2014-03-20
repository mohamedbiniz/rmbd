package at.ainf.asp.interactive.solver;


import at.ainf.diagnosis.model.BaseSearchableObject;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.Rounding;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * *
 */
public class ASPTheory extends BaseSearchableObject<String> {

    public ASPTheory(ASPSolver solver, ASPKnowledgeBase kb) {
        setKnowledgeBase(kb);
        setReasoner(solver);
    }

    @Override
    public ASPSolver getReasoner() {
        return (ASPSolver) super.getReasoner();
    }

    @Override
    public boolean verifyConsistency() throws SolverException {
        boolean consistent = getReasoner().isConsistent();
        return consistent && !violatesTestCases();
    }

    private boolean violatesTestCases() {
        for (Set<String> test : getKnowledgeBase().getNegativeTests()) {
            if (!getReasoner().isEntailed(test)) {
                return true;
            }
        }

        for (Set<String> test : getKnowledgeBase().getNonentailedTests()) {
            if (test != null && getReasoner().isEntailed(test)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getEntailments(Set<String> hittingSet) throws SolverException {
        final Set<String> program = getReasoner().generateDiagnosisProgram(hittingSet, getASPKnowledgeBase());
        getReasoner().clearFormulasCache();
        getReasoner().addFormulasToCache(program);
        return getReasoner().getEntailments();
    }

    @Override
    public boolean isEntailed(Set<String> formulas) {
        final Set<String> program = getReasoner().generateDebuggingProgram(getASPKnowledgeBase());
        getReasoner().clearFormulasCache();
        getReasoner().addFormulasToCache(program);
        return getReasoner().getEntailments().containsAll(formulas);
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean diagnosisEntails(FormulaSet<String> hs, Set<String> ent) {
        try {
            return getEntailments(hs).containsAll(ent);
        } catch (SolverException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifyTestCasesForDiagnosisCandidate(Set<String> hs) {
        final Set<String> program = getReasoner().generateDiagnosisProgram(hs, getASPKnowledgeBase());
        getReasoner().clearFormulasCache();
        getReasoner().addFormulasToCache(program);
        try {
            return verifyConsistency();
        } catch (SolverException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean diagnosisConsistent(FormulaSet<String> hs, Set<String> ent) {
        final Set<String> program = getReasoner().generateDiagnosisProgram(hs, getASPKnowledgeBase());
        getReasoner().clearFormulasCache();
        getReasoner().addFormulasToCache(program);
        getReasoner().addFormulasToCache(ent);
        try {
            return verifyConsistency();
        } catch (SolverException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doBayesUpdate(Set<? extends FormulaSet<String>> hittingSets) {
        for (FormulaSet<String> hs : hittingSets) {
            Set<String> positive = new LinkedHashSet<String>();

            for (int i = 0; i < getKnowledgeBase().getTestsSize(); i++) {
                Set<String> testcase = getKnowledgeBase().getTest(i);

                if (i - 1 > -1) {
                    Set<String> olderTC = getKnowledgeBase().getTest(i - 1);
                    if (getKnowledgeBase().getTypeOfTest(olderTC))
                        positive.addAll(olderTC);
                }
                BigDecimal value = hs.getMeasure().divide(BigDecimal.valueOf(2));

                if (getKnowledgeBase().getTypeOfTest(testcase)) {
                    if (!diagnosisEntails(hs, testcase, positive)) {
                        hs.setMeasure(value);
                    }
                } else {
                    if (diagnosisConsistent(hs, testcase, positive)) {
                        hs.setMeasure(value);
                    }
                }
            }
        }

        // normalize diagnoses probabilities

        BigDecimal sum = new BigDecimal("0");

        for (FormulaSet<String> hittingSet : hittingSets) {
            sum = sum.add(hittingSet.getMeasure());
        }

        if (sum.compareTo(BigDecimal.ZERO) == 0 && hittingSets.size() != 0)
            throw new IllegalStateException("Sum of probabilities of all diagnoses is 0!");

        for (FormulaSet<String> hittingSet : hittingSets) {
            // the decimal expansion is inf we need round
            hittingSet.setMeasure(hittingSet.getMeasure().divide(sum, Rounding.PRECISION, Rounding.ROUNDING_MODE));
        }

    }

    protected boolean diagnosisConsistent(FormulaSet<String> hs, Set<String> testcase, Set<String> positive) {
        final Set<String> program = getReasoner().generateDiagnosisProgram(hs, getASPKnowledgeBase());
        getReasoner().clearFormulasCache();
        getReasoner().addFormulasToCache(program);
        getReasoner().addFormulasToCache(testcase);
        getReasoner().addFormulasToCache(positive);
        try {
            return verifyConsistency();
        } catch (SolverException e) {
            throw new RuntimeException(e);
        }
    }


    protected boolean diagnosisEntails(FormulaSet<String> hs, Set<String> testcase, Set<String> positive) {
        final Set<String> program = getReasoner().generateDiagnosisProgram(hs, getASPKnowledgeBase());
        getReasoner().clearFormulasCache();
        getReasoner().addFormulasToCache(program);
        getReasoner().addFormulasToCache(positive);
        return getReasoner().getEntailments().containsAll(testcase);
    }

    public ASPKnowledgeBase getASPKnowledgeBase() {
        return (ASPKnowledgeBase) getKnowledgeBase();
    }
}
