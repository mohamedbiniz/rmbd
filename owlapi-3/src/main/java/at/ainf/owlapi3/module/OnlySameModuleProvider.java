package at.ainf.owlapi3.module;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.02.13
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class OnlySameModuleProvider extends AbstractOWLModuleProvider {

    protected OnlySameModuleProvider(OWLOntology ontology, OWLReasonerFactory factory, boolean isElOnto) {
        super(ontology, factory, isElOnto);
    }

    @Override
    public Set<OWLLogicalAxiom> getSmallerModule(Set<OWLLogicalAxiom> module) {
        return module;
    }

}
