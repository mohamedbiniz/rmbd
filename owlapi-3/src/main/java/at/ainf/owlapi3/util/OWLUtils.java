package at.ainf.owlapi3.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */
public class OWLUtils {

    public static String calculateExpressivity (Set<OWLLogicalAxiom> axioms) {
        return new DLExpressivityChecker(Collections.singleton(createOntology(axioms))).getDescriptionLogicName();
    }

    public static Set<OWLClass> calculateSignature (Set<OWLLogicalAxiom> module) {
        Set<OWLClass> classesInModule = new LinkedHashSet<OWLClass>();
        for (OWLLogicalAxiom axiom : module)
            classesInModule.addAll (axiom.getClassesInSignature());
        return classesInModule;
    }

    public static OWLOntology createOntology (Set<? extends OWLAxiom> axioms) {
        OWLOntology debuggingOntology = null;
        try {
            debuggingOntology = OWLManager.createOWLOntologyManager().createOntology();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        debuggingOntology.getOWLOntologyManager().addAxioms(debuggingOntology,axioms);
        return debuggingOntology;
    }

}
