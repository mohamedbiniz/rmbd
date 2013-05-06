package at.ainf.owlapi3.module.iterative.diag;

import at.ainf.owlapi3.module.iterative.ModuleCalc;
import at.ainf.owlapi3.module.iterative.ModuleDiagSearcher;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.04.13
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractModuleDiagnosis implements ModuleDiagnosis {

    private static Logger logger = LoggerFactory.getLogger(AbstractModuleDiagnosis.class.getName());

    protected static final OWLClass BOT_CLASS = OWLDataFactoryImpl.getInstance().getOWLNothing();

    private ModuleCalc moduleCalculator;

    private ModuleDiagSearcher diagSearcher;

    private Set<OWLLogicalAxiom> mappings;

    private final Set<OWLLogicalAxiom> ontoAxioms;

    public AbstractModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms,
                               OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {

        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(mappings);

        OWLOntology ontology = createOntology(allAxioms);
        moduleCalculator = new ModuleCalc(ontology, factory);

        this.ontoAxioms = ontoAxioms;
        this.mappings = mappings;
        this.diagSearcher = moduleDiagSearcher;
        diagSearcher.setReasonerFactory(factory);

    }

    protected ModuleCalc getModuleCalculator() {
        return moduleCalculator;
    }

    protected Set<OWLLogicalAxiom> getMappings() {
        return mappings;
    }

    protected Set<OWLLogicalAxiom> getOntoAxioms() {
        return ontoAxioms;
    }

    protected ModuleDiagSearcher getDiagSearcher() {
        return diagSearcher;
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

    protected class ModuleSizeComparator implements Comparator<OWLClass> {

        private final Map<OWLClass, Set<OWLLogicalAxiom>> unsatMap;

        public ModuleSizeComparator(Map<OWLClass, Set<OWLLogicalAxiom>> unsatMap) {
            this.unsatMap = unsatMap;
        }

        @Override
        public int compare(OWLClass o1, OWLClass o2) {
            return new Integer(unsatMap.get(o1).size()).compareTo(unsatMap.get(o2).size());
        }

    }
}
