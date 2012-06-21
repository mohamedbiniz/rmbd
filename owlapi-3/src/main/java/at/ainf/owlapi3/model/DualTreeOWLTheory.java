package at.ainf.owlapi3.model;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.01.12
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */
public class DualTreeOWLTheory extends OWLTheory {

    private static Logger logger = Logger.getLogger(DualTreeOWLTheory.class.getName());

    public DualTreeOWLTheory(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Set<OWLLogicalAxiom> backgroundAxioms) throws InconsistentTheoryException, SolverException {
        super(reasonerFactory, ontology, backgroundAxioms);
    }

    public boolean verifyRequirements() {
        OWLOntology ontology = getOntology();
        Set<OWLLogicalAxiom> axiomSet = new LinkedHashSet<OWLLogicalAxiom> (getActiveFormulas());
        axiomSet.removeAll(getFormulaStack());
        updateAxioms(getOntology(), axiomSet, getBackgroundFormulas());

        boolean consistent = !doConsistencyTest(getSolver());

        if (logger.isTraceEnabled())
            logger.trace(ontology.getOntologyID() + " is consistent: " + consistent);
        return consistent;
    }

}
