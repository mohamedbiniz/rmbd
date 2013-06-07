package at.ainf.asp.model;

import java.util.LinkedHashSet;
import java.util.Set;

import at.ainf.diagnosis.model.AbstractReasoner;
import at.ainf.diagnosis.model.BaseSearchableObject;
import at.ainf.diagnosis.model.IKnowledgeBase;
import at.ainf.diagnosis.model.IReasoner;
import at.ainf.diagnosis.model.SolverException;

/**
 * @author Melanie Fruehstueck
 *
 */
public class ASPTheory extends BaseSearchableObject<IProgramElement> {

	
	
	@Override
	public ReasonerASP getReasoner() {
		return (ReasonerASP) super.getReasoner();
	}

	@Override
	public boolean verifyConsistency() throws SolverException {
		LinkedHashSet<IProgramElement> formulasToAdd = new LinkedHashSet<IProgramElement>(getKnowledgeBase().getBackgroundFormulas());
		System.out.println("\nFormulas to add:");
		for (IProgramElement pe : formulasToAdd) {
			System.out.println(pe.getString());
		}
		Set<IProgramElement> cache = getReasoner().getFormulasCache();
		System.out.println("\nFormulas cached:");
		for (IProgramElement pe : cache) {
			System.out.println(pe.getString());
		}
		formulasToAdd.removeAll(getReasoner().getFormulasCache());
		getReasoner().addFormulasToCache(formulasToAdd);
		boolean isConsistent = getReasoner().isConsistent();
		getReasoner().removeFormulasFromCache(formulasToAdd);
		return isConsistent;
	}

	
	
}
