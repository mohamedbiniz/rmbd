package at.ainf.owlapi3.utils.creation.ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.08.12
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class SimpleOntologyCreator implements OntologyCreator {

    private File file;

    public SimpleOntologyCreator (String path, String name) {
        String directory = ClassLoader.getSystemResource(path).getPath();
        file = new File(directory + "/" + name);
    }

    public SimpleOntologyCreator (File filename) {
        file = filename;
    }

    public OWLOntology getOntology() {
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
