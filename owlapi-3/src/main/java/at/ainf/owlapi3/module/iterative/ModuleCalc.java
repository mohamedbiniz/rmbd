package at.ainf.owlapi3.module.iterative;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import at.ainf.owlapi3.reasoner.HornSatReasonerFactory;
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

    private Set<OWLLogicalAxiom> ontology;
    private OWLReasoner reasoner;

    private Map<OWLClass,Set<OWLLogicalAxiom>> moduleMap = new HashMap<OWLClass, Set<OWLLogicalAxiom>>();
    private Set<OWLClass> initialUnsatClasses;

    public ModuleCalc(Set<OWLLogicalAxiom> ontology, OWLReasonerFactory reasonerFactory) {
        this.ontology = ontology;

        OWLOntology onto = createOntology(ontology);
        if (reasonerFactory.getReasonerName().equals(HornSatReasoner.NAME)) {
            HornSatReasonerFactory hornSatReasonerFactory = (HornSatReasonerFactory) reasonerFactory;
            hornSatReasonerFactory.precomputeUnsatClasses(onto);
            this.reasoner = reasonerFactory.createNonBufferingReasoner(onto);
            initialUnsatClasses = new HashSet<OWLClass>(hornSatReasonerFactory.getUnsatClasses());
        }
        else {
            this.reasoner = reasonerFactory.createNonBufferingReasoner(onto);
            initialUnsatClasses = new HashSet<OWLClass>(reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom());
        }
    }

    protected OWLOntology createOntology (Set<? extends OWLAxiom> axioms) {
        OWLOntology debuggingOntology = null;
        try {
            debuggingOntology = OWLManager.createOWLOntologyManager().createOntology();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        debuggingOntology.getOWLOntologyManager().addAxioms(debuggingOntology, axioms);
        return debuggingOntology;
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

        result = extractModule(createOntology(ontology), unsatClass);
        moduleMap.put(unsatClass,result);

        return result;
    }

    public Set<OWLLogicalAxiom> extractModule (OWLOntology ontology, OWLClass unsatClass) {
        Set<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
        SyntacticLocalityModuleExtractor extractor =
                new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);

        for (OWLAxiom axiom : extractor.extract(Collections.singleton((OWLEntity) unsatClass)))
            result.add((OWLLogicalAxiom)axiom);
        return result;
    }

    public Map<OWLClass, Set<OWLLogicalAxiom>> getModuleMap() {
        return moduleMap;
    }

    public void removeAxiomsFromOntologyAndModules(Set<OWLLogicalAxiom> axioms) {
        ontology.removeAll(axioms);

        for (Set<OWLLogicalAxiom> module : moduleMap.values())
            module.removeAll(axioms);
    }

}
