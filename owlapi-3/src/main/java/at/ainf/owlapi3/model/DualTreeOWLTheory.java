package at.ainf.owlapi3.model;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(DualTreeOWLTheory.class.getName());

    public DualTreeOWLTheory(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Set<OWLLogicalAxiom> backgroundAxioms) throws InconsistentTheoryException, SolverException {
        super(reasonerFactory, ontology, backgroundAxioms);
    }

    public boolean verifyRequirements() {
        //OWLOntology ontology = getOriginalOntology();
        Set<OWLLogicalAxiom> faultyFormulas = new LinkedHashSet<OWLLogicalAxiom> (getKnowledgeBase().getFaultyFormulas());
        faultyFormulas.removeAll(getReasoner().getFormulasCache());
        //setFormularCach(axiomSet, getKnowledgeBase().getBackgroundFormulas());
        LinkedHashSet<OWLLogicalAxiom> formularCacheBackup = new LinkedHashSet<OWLLogicalAxiom>(getReasoner().getFormulasCache());
        getReasoner().clearFormulasCache();
        getReasoner().addFormulasToCache(faultyFormulas);
        //getReasoner().addFormulasToCache(getKnowledgeBase().getBackgroundFormulas());
        boolean consistent = !doConsistencyTest();
        getReasoner().clearFormulasCache();
        getReasoner().addFormulasToCache(formularCacheBackup);

        if (logger.isTraceEnabled())
            logger.trace("Ontology is consistent: " + consistent);
        return consistent;
    }

}
