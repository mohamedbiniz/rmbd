package at.ainf.owlapi3.utils;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.05.12
 * Time: 09:14
 * To change this template use File | Settings | File Templates.
 */
public class CreationUtils {

    public static OWLOntology createOwlOntology(String path, String name) {
        String directory = ClassLoader.getSystemResource(path).getPath();
        return createOwlOntology(new File(directory + "/" + name));
    }

    public static OWLOntology createOwlOntology(File file) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }

    public static Set<OWLLogicalAxiom> createBackgroundAxioms(OWLOntology ontology) {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }
        return bax;
    }

    public static Set<OWLLogicalAxiom> createExtendedBackgroundAxioms(OWLOntology ontology) {
        Set<OWLLogicalAxiom> bax = createBackgroundAxioms(ontology);
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }
        return bax;
    }

    public static OWLTheory createTheory(OWLOntology ontology, boolean dual, Set<OWLLogicalAxiom> bax) {
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

    public static OWLTheory createTheory(OWLOntology ontology, boolean dual) {
        Set<OWLLogicalAxiom> bax = createBackgroundAxioms(ontology);
        return createTheory(ontology,dual,bax);

    }


}
