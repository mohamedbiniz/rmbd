package at.ainf.owlapi3.module.iterative;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.03.13
 * Time: 09:03
 * To change this template use File | Settings | File Templates.
 */
public class ModuleCalc {

    private OWLOntology ontology;
    private OWLReasoner reasoner;

    private Map<OWLClass,Set<OWLLogicalAxiom>> moduleMap = new HashMap<OWLClass, Set<OWLLogicalAxiom>>();
    private Set<OWLClass> initialUnsatClasses;
    private SyntacticLocalityModuleExtractor extractor;

    public ModuleCalc(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {
        this.ontology = ontology;
        this.reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
        initialUnsatClasses = new HashSet<OWLClass>(reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom());
        extractor =new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(),ontology,ModuleType.STAR);
    }

    public Set<OWLClass> getInitialUnsatClasses() {
        return initialUnsatClasses;
    }

    public boolean isSatisfiable(OWLClass unsatClass) {
        return reasoner.isSatisfiable(unsatClass);
    }

    public Set<OWLLogicalAxiom> calculateModule(OWLClass unsatClass) {
        Set<OWLLogicalAxiom> result = moduleMap.get(unsatClass);

        if (result != null)
            return result;

        result = new HashSet<OWLLogicalAxiom>();
        for (OWLAxiom axiom : extractor.extract(Collections.singleton((OWLEntity) unsatClass)))
            result.add((OWLLogicalAxiom)axiom);
        moduleMap.put(unsatClass,result);

        return result;
    }

    public Map<OWLClass, Set<OWLLogicalAxiom>> getModuleMap() {
        return moduleMap;
    }

    public void removeAxiomsFromOntologyAndModules(Set<OWLLogicalAxiom> axioms) {
        ontology.getOWLOntologyManager().removeAxioms(ontology,axioms);

        for (Set<OWLLogicalAxiom> module : moduleMap.values())
            module.removeAll(axioms);
    }

}
