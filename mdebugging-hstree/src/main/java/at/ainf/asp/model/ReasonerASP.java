package at.ainf.asp.model;

import java.util.Set;
import at.ainf.diagnosis.model.AbstractReasoner;
import at.ainf.diagnosis.model.IReasoner;

/**
 * @author Melanie Frühstück
 *
 */
public class ReasonerASP extends AbstractReasoner<IProgramElement> {

	ASPModel model;
	
	public ReasonerASP() {
		model = ASPModel.getASPModelInstance();
		setReasonerFormulas(model.getProgramElements());
	}

	/* (non-Javadoc)
	 * @see at.ainf.diagnosis.model.IReasoner#isConsistent()
	 */
	@Override
	public boolean isConsistent() {
		ASPSolver solver = new ASPSolver();
		return solver.solve(getFormulasCache());
	}

	/* (non-Javadoc)
	 * @see at.ainf.diagnosis.model.IReasoner#newInstance()
	 */
	@Override
	public IReasoner<IProgramElement> newInstance() {
		return new ReasonerASP();
	}

	/* (non-Javadoc)
	 * @see at.ainf.diagnosis.model.AbstractReasoner#updateReasonerModel(java.util.Set, java.util.Set)
	 */
	@Override
	protected void updateReasonerModel(Set<IProgramElement> axiomsToAdd,
			Set<IProgramElement> axiomsToRemove) {
		for (IProgramElement rule : axiomsToAdd) {
			model.addProgramElement(rule);
		}
		for (IProgramElement rule : axiomsToRemove) {
			model.removeProgramElement(rule);
		}
		
	}

}
