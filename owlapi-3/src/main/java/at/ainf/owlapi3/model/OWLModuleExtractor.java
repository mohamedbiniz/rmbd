package at.ainf.owlapi3.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.05.13
 * Time: 10:13
 * To change this template use File | Settings | File Templates.
 */
public class OWLModuleExtractor {

    private SyntacticLocalityModuleExtractor extractor;

    public OWLModuleExtractor(Set<OWLLogicalAxiom> ontology) {
        extractor = new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), createOntology(ontology), ModuleType.STAR);
    }

    private Set<OWLEntity> convertClassToEntity (Collection<OWLClass> signature) {
        Set<OWLEntity> result = new LinkedHashSet<OWLEntity>();
        for (OWLEntity e : signature)
            result.add(e);
        return result;
    }

    private Set<OWLLogicalAxiom> convertToLogicalAxioms (Set<OWLAxiom> module) {
        Set<OWLLogicalAxiom> result = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLAxiom axiom : module)
            result.add((OWLLogicalAxiom)axiom);
        return result;
    }

    public Set<OWLLogicalAxiom> calculateModule (Collection<OWLClass> signature) {
        Set<OWLAxiom> result = extractor.extract(convertClassToEntity(signature));
        return convertToLogicalAxioms(result);
    }

}
