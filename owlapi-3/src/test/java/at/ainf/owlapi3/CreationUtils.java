package at.ainf.owlapi3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import java.io.File;
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

    public static OWLOntology mergeOntologies (OWLOntology ontology1, OWLOntology ontology2) {
        try {
            OWLOntologyManager man = ontology1.getOWLOntologyManager();
            final Set<OWLOntology> ontologies = new LinkedHashSet<OWLOntology>();
            ontologies.add(ontology1);
            ontologies.add(ontology2);

            OWLOntologyMerger merger = new OWLOntologyMerger(new OWLOntologySetProvider() {
                public Set<OWLOntology> getOntologies() {
                    return ontologies;
                }
            });
            String o1I = ontology1.getOntologyID().getOntologyIRI().toString();
            String o2I = ontology2.getOntologyID().getOntologyIRI().toString();
            IRI mergedIRI = IRI.create("matched_" + o1I + "_" + o2I);

            return merger.createMergedOntology(man, mergedIRI);
        } catch (OWLOntologyCreationException e) {
            return null;
        }
    }

    public static Set<OWLLogicalAxiom> getIntersection (Set<OWLLogicalAxiom> axioms1, Set<OWLLogicalAxiom> axioms2) {
        Set<OWLLogicalAxiom> intersection = new LinkedHashSet<OWLLogicalAxiom>();
        intersection.addAll(axioms1);
        intersection.retainAll(axioms2);

        return intersection;
    }

    public static void writeDiagnosisToFile(String filename, Set<OWLLogicalAxiom> diagnosis) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        String directory = ClassLoader.getSystemResource("diags").getPath();
        File file = new File(directory + "/" + filename + ".dg");
        OWLOntology ontology = null;
        try {
            ontology = man.createOntology();
            man.addAxioms(ontology, diagnosis);
            man.saveOntology(ontology, IRI.create(file.toURI()));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static Set<OWLLogicalAxiom> readDiagnosisFromFile (String filename) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        String directory = ClassLoader.getSystemResource("diags").getPath();
        File file = new File(directory + "/" + filename + ".dg");
        OWLOntology ontology = null;
        try {
            ontology = man.loadOntologyFromOntologyDocument(file);
            return ontology.getLogicalAxioms();
        } catch (OWLOntologyCreationException e) {
            return null;
        }
    }

}
