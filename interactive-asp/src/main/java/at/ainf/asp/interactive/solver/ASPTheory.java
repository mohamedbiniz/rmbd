package at.ainf.asp.interactive.solver;

import at.ainf.asp.test.Application;
import at.ainf.diagnosis.model.BaseSearchableObject;
import at.ainf.diagnosis.model.SolverException;

import java.util.LinkedHashSet;
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
		LinkedHashSet<IProgramElement> formulasToAdd =
                new LinkedHashSet<IProgramElement>(getKnowledgeBase().getBackgroundFormulas());
		
		if (Application.enableInfo) {
			System.out.println("\nFormulas to add:");
			for (IProgramElement pe : formulasToAdd) {
				System.out.println(pe.getString());
			}
		}
		
		Set<IProgramElement> cache = getReasoner().getFormulasCache();
		
		if (Application.enableInfo) {
			System.out.println("\nFormulas cached:");
			for (IProgramElement pe : cache) {
				System.out.println(pe.getString());
			}
		}
		
		formulasToAdd.removeAll(getReasoner().getFormulasCache());
		getReasoner().addFormulasToCache(formulasToAdd);
		boolean isConsistent = getReasoner().isConsistent();
		ASPOutput output = ASPOutput.getASPOutputInstance();
		if (output.isUnknown()) throw new SolverException("The solver returned UNKNOWN. Probably there are some syntax errors in the ASP file.");
		getReasoner().removeFormulasFromCache(formulasToAdd);
		return isConsistent;
	}

	
	
}
