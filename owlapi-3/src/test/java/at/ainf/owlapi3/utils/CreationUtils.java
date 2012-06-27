package at.ainf.owlapi3.utils;

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


}
