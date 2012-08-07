package at.ainf.owlapi3.utils.creation.theory;

import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.08.12
 * Time: 09:30
 * To change this template use File | Settings | File Templates.
 */
public class BackgroundExtendedTheoryCreator extends SimpleTheoryCreator {
    public BackgroundExtendedTheoryCreator(OWLOntology ont, boolean dualTheory) {
        super(ont, dualTheory);
    }

    @Override
    protected Set<OWLLogicalAxiom> createBackgroundAxioms(OWLOntology ontology) {
        Set<OWLLogicalAxiom> bax = super.createBackgroundAxioms(ontology);
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }
        return bax;

    }

    @Override
    protected void configureEntailments(OWLTheory theory) {
        theory.activateReduceToUns();
        theory.setIncludeTrivialEntailments(false);
        theory.setIncludeSubClassOfAxioms(false);
        theory.setIncludeClassAssertionAxioms(false);
        theory.setIncludeEquivalentClassAxioms(false);
        theory.setIncludeDisjointClassAxioms(false);
        theory.setIncludePropertyAssertAxioms(false);
        theory.setIncludeReferencingThingAxioms(false);
        theory.setIncludeOntologyAxioms(true);
    }

}
