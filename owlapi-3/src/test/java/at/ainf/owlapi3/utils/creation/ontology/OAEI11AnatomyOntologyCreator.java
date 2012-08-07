package at.ainf.owlapi3.utils.creation.ontology;

import at.ainf.owlapi3.utils.session.OAEI11AnatomySession;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import java.io.InputStream;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.08.12
 * Time: 14:40
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11AnatomyOntologyCreator implements OntologyCreator {

    private String file;

    public OAEI11AnatomyOntologyCreator (String file) {
        this.file = file;
    }

    public OWLOntology getOntology () {
        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();

            InputStream st = ClassLoader.getSystemResourceAsStream("oaei11/mouse.owl");
            OWLOntology mouse = man.loadOntologyFromOntologyDocument(st);
            st = ClassLoader.getSystemResourceAsStream("oaei11/human.owl");
            OWLOntology human = man.loadOntologyFromOntologyDocument(st);

            OWLOntologyMerger merger = new OWLOntologyMerger(man);
            OWLOntology merged = merger.createMergedOntology(man, IRI.create("matched" + file + ".txt"));
            Set<OWLLogicalAxiom> mappAx = OAEI11AnatomySession.getAxiomsInMappingOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/", file);
            for (OWLLogicalAxiom axiom : mappAx)
                man.applyChange(new AddAxiom(merged, axiom));

            return merged;
        } catch (OWLOntologyCreationException e) {
            return null;
        }
    }

}
