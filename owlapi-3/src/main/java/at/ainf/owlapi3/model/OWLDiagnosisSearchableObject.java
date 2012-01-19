package at.ainf.owlapi3.model;

import at.ainf.theory.Searchable;
import at.ainf.theory.model.AbstractSearchableObject;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 14.07.11
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
public class OWLDiagnosisSearchableObject extends AbstractSearchableObject<OWLLogicalAxiom>
        implements Searchable<OWLLogicalAxiom> {

    private static Logger logger = Logger.getLogger(OWLDiagnosisSearchableObject.class.getName());

    OWLTheory theory;

    public OWLDiagnosisSearchableObject(OWLTheory theory) {
        this.theory = theory;
    }

    @Override
    protected boolean verifyConsistency() {
        OWLOntology ontology = theory.getOntology();
        theory.addAxioms(theory.getActiveFormulas(), ontology);
        theory.removeAxioms(getFormulaStack(), ontology);
        Set<OWLLogicalAxiom> bf = theory.getBackgroundFormulas();
        theory.addAxioms(bf, ontology);

        boolean consistent = !theory.doConsistencyTest(theory.getSolver());

        theory.removeAxioms(bf, ontology);
        if (logger.isTraceEnabled())
            logger.trace(ontology.getOntologyID() + " is consistent: " + consistent);
        return consistent;
    }

    public void addBackgroundFormulas(Set<OWLLogicalAxiom> formulas) throws InconsistentTheoryException, SolverException {
        theory.addBackgroundFormulas(formulas);
    }

    public void removeBackgroundFormulas(Set<OWLLogicalAxiom> formulas) throws InconsistentTheoryException, SolverException {
        theory.removeBackgroundFormulas(formulas);
    }
}
