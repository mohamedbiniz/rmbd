package at.ainf.owlapi3.utils.creation.theory;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.08.12
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class SimpleTheoryCreator implements TheoryCreator {

    protected OWLOntology ontology;

    protected boolean dual;

    public SimpleTheoryCreator(OWLOntology ont, boolean dualTheory) {
        dual = dualTheory;
        ontology = ont;

    }

    protected Set<OWLLogicalAxiom> createBackgroundAxioms(OWLOntology ontology) {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }
        return bax;
    }

    protected OWLTheory createTheory(OWLOntology ontology, boolean dual, Set<OWLLogicalAxiom> bax) {
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = null;
        try {
            if (dual)
                theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
            else
                theory = new OWLTheory(reasonerFactory, ontology, bax);
        }
        catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return theory;
    }

    protected void configureEntailments (OWLTheory theory) {

    }

    public OWLTheory getTheory() {
        Set<OWLLogicalAxiom> bax = createBackgroundAxioms(ontology);
        OWLTheory theory = createTheory(ontology,dual,bax);
        configureEntailments(theory);

        return theory;
    }


}
