package at.ainf.owlcontroller;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.12.11
 * Time: 09:25
 * To change this template use File | Settings | File Templates.
 */
public class OWLControllerImpl implements OWLController {

    private OWLOntology ont;

    private OWLReasonerFactory factory;

    private Map<ManchesterOWLSyntax, Double> keywordProbs = null;


    public OWLControllerImpl(OWLOntology ontology, OWLReasonerFactory factory) {
        this.ont = ontology;
        this.factory = factory;

    }





}
