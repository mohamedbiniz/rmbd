package at.ainf.owlcontroller;

import at.ainf.controller.QueryDebuggerCont;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.12.11
 * Time: 14:40
 * To change this template use File | Settings | File Templates.
 */
public class OwlQueryDebuggerCont extends QueryDebuggerCont {

    public OwlQueryDebuggerCont(OWLOntology ontology) {
        super();
        getPico().addComponent(OWLTheory.class);
        getPico().addComponent(OWLAxiomNodeCostsEstimator.class);

    }

}
