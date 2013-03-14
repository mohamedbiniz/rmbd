package at.ainf.owlapi3.module.iterative;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.03.13
 * Time: 09:54
 * To change this template use File | Settings | File Templates.
 */
public class IterativeModuleDiagnosis {



    private ModuleDiagSearcher diagSearcher;

    //private Map<OWLClass, Set<OWLLogicalAxiom>> unsatMap;

    private ModuleCalc moduleCalculator;

    private Set<OWLLogicalAxiom> mappings;

    private final Set<OWLLogicalAxiom> ontoAxioms;

    private OWLReasonerFactory reasonerFactory;

    public IterativeModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms,
                                   OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {

        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(mappings);

        OWLOntology fullOntology = createOntology(allAxioms);
        //OtfModuleProvider provider = new OtfModuleProvider(fullOntology, new Reasoner.ReasonerFactory(),false);
        //Set<OWLLogicalAxiom> bigModule = provider.getModuleUnsatClass();
        moduleCalculator = new ModuleCalc(fullOntology, new Reasoner.ReasonerFactory());

        //unsatMap = provider.getUnsatClasses();
        this.ontoAxioms = ontoAxioms;
        this.mappings = mappings;
        this.diagSearcher = moduleDiagSearcher;
        this.reasonerFactory = factory;

    }

    public Set<OWLLogicalAxiom> calculateTargetDiagnosis() {
        Set<OWLLogicalAxiom> targetDiagnosis = new HashSet<OWLLogicalAxiom>();
        List<OWLClass> unsatClasses = new LinkedList<OWLClass>(moduleCalculator.getInitialUnsatClasses());
        Collections.sort(unsatClasses,new ChildsComparator(unsatClasses,mappings,ontoAxioms));
        int toIndex = Collections.min(Arrays.asList(10,unsatClasses.size()));
        List<OWLClass> actualUnsatClasses = new LinkedList<OWLClass>(unsatClasses.subList(0,toIndex));

        while (!actualUnsatClasses.isEmpty()) {
            for (OWLClass unsatClass : actualUnsatClasses)
                moduleCalculator.calculateModule(unsatClass);
            Map<OWLClass, Set<OWLLogicalAxiom>> map = moduleCalculator.getModuleMap();
            OWLClass actualUnsatClass = Collections.min(actualUnsatClasses,new ModuleSizeComparator(map));
            Set<OWLLogicalAxiom> axioms = new LinkedHashSet<OWLLogicalAxiom>(map.get(actualUnsatClass));
            Set<OWLLogicalAxiom> background = new LinkedHashSet<OWLLogicalAxiom>(axioms);
            background.retainAll(ontoAxioms);
            //Set<? extends Set<OWLLogicalAxiom>> diagnoses = searchDiagnoses(axioms, background);
            //Set<OWLLogicalAxiom> partDiag = diagnosisOracle.chooseDiagnosis(diagnoses);
            Set<OWLLogicalAxiom> partDiag = diagSearcher.calculateDiag(axioms,background);

            moduleCalculator.removeAxiomsFromOntologyAndModules(partDiag);
            updatedLists(actualUnsatClasses,unsatClasses);
            targetDiagnosis.addAll(partDiag);
        }

        return targetDiagnosis;
    }

    protected boolean isEveryStartUnsatOK(OWLClass unsatClass) {
        //OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(createOntology(unsatMap.get(unsatClass)));
        //if (!reasoner.isSatisfiable(unsatClass))
            //return false;
        /*for (OWLClass unsat : unsatMap.keySet()) {
            if (!reasoner.isSatisfiable(unsat))
                return false;
        }*/
        return true;

    }

    protected void updatedLists (List<OWLClass> actualUnsat, List<OWLClass> allUnsat) {

        Set<OWLClass> toCheck = new HashSet<OWLClass>(actualUnsat);
        for (OWLClass unsatClass : toCheck) {
            //OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(createOntology(unsatMap.get(unsatClass)));
            if (moduleCalculator.isSatisfiable(unsatClass)) {
                actualUnsat.remove(unsatClass);
                allUnsat.remove(unsatClass);
            }
        }
        toCheck = new HashSet<OWLClass>(allUnsat);
        toCheck.removeAll(actualUnsat);
        for (OWLClass unsatClass : toCheck) {
            //OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(createOntology(unsatMap.get(unsatClass)));
            if (!moduleCalculator.isSatisfiable(unsatClass)) {
                actualUnsat.add(unsatClass);
                if (actualUnsat.size() == 10)
                    break;
            }
            else {
                allUnsat.remove(unsatClass);
            }
        }
    }

    protected void removeDiagnosisFromModules(Set<OWLLogicalAxiom> diagnosis, Map<OWLClass, Set<OWLLogicalAxiom>> unsatMap) {
        for (Set<OWLLogicalAxiom> module : unsatMap.values())
            module.removeAll(diagnosis);
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

    protected class ChildsComparator implements Comparator<OWLClass> {
        private Collection<OWLLogicalAxiom> mappings;
        private Collection<OWLLogicalAxiom> ontoAxioms;
        private final Map<OWLClass, Integer> map;

        public ChildsComparator(Collection<OWLClass> unsatClasses, Collection<OWLLogicalAxiom> mappings, Collection<OWLLogicalAxiom> ontoAxioms) {
            this.mappings = mappings;
            this.ontoAxioms = ontoAxioms;

            this.map = calculateNumberTransitiveUnsatChilds(unsatClasses);
        }

        protected Map<OWLClass,Integer> calculateNumberTransitiveUnsatChilds(Collection<OWLClass> unsatClasses) {
            Map<OWLClass,Integer> result = new LinkedHashMap<OWLClass, Integer>();
            Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
            allAxioms.addAll(mappings);
            allAxioms.addAll(ontoAxioms);
            StructuralReasoner reasoner = new StructuralReasoner (createOntology(allAxioms),
                    new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
            for (OWLClass unsatClass : unsatClasses) {
                Set<OWLClass> childs = new HashSet<OWLClass>(reasoner.getSubClasses(unsatClass,false).getFlattened());
                childs.remove(BOT_CLASS);
                result.put(unsatClass,childs.size());
            }
            return result;
        }

        @Override
        public int compare(OWLClass o1, OWLClass o2) {
            return map.get(o1).compareTo(map.get(o2));
        }

    }

    private static final OWLClass BOT_CLASS = OWLDataFactoryImpl.getInstance().getOWLNothing();

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

}
