package at.ainf.asp.interactive.solver;


import at.ainf.diagnosis.model.BaseSearchableObject;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;

import java.util.Set;

/**
 *  *
 */
public class ASPTheory extends BaseSearchableObject<String> {

	@Override
	public ASPSolver getReasoner() {
		return (ASPSolver) super.getReasoner();
	}
	
	@Override
	public boolean verifyConsistency() throws SolverException {
        boolean consistent = getReasoner().isConsistent();
        return consistent && checkTestsConsistency();
    }

    private boolean checkTestsConsistency() {
        //OWLReasoner solver = getSolver();
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

    public Set<String> getEntailments(Set<String> hittingSet) throws SolverException {
        throw new RuntimeException("This theory does not support computation of entailments!");
    }

    public boolean isEntailed(Set<String> n) {
        throw new RuntimeException("This theory does not support verification of entailments!");
    }

    public void reset() {
        throw new RuntimeException("Unimplemented method");
    }

    public boolean diagnosisEntails(FormulaSet<String> hs, Set<String> ent) {
        throw new RuntimeException("Unimplemented method");
    }

    public boolean diagnosisConsistent(FormulaSet<String> hs, Set<String> ent) {
        throw new RuntimeException("Unimplemented method");
    }

    public void doBayesUpdate(Set<? extends FormulaSet<String>> hittingSets) {
        throw new RuntimeException("Unimplemented method");
    }

	
	
}
