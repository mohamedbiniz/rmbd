package at.ainf.owlapi3.module.iterative.diag;

import at.ainf.diagnosis.Speed4JMeasurement;
import at.ainf.owlapi3.module.iterative.ModuleDiagSearcher;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.03.13
 * Time: 09:54
 * To change this template use File | Settings | File Templates.
 */
public class IterativeModuleDiagnosis extends AbstractModuleDiagnosis {

    private static Logger logger = LoggerFactory.getLogger(IterativeModuleDiagnosis.class.getName());

    private final boolean sortNodes;

    public IterativeModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms,
                                    OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher,
                                    boolean sortNodes) {

        super(mappings,ontoAxioms,factory,moduleDiagSearcher);

        this.sortNodes = sortNodes;

    }

    protected boolean isSortNodes() {
        return sortNodes;
    }

    public Set<OWLLogicalAxiom> calculateTargetDiagnosis() {
        Set<OWLLogicalAxiom> targetDiagnosis = new HashSet<OWLLogicalAxiom>();
        Speed4JMeasurement.start("calculatetargetdiag");
        List<OWLClass> unsatClasses = new LinkedList<OWLClass>(getModuleCalculator().getInitialUnsatClasses());
        if (isSortNodes())
            Collections.sort(unsatClasses,new ChildsComparator(unsatClasses,getMappings(),getOntoAxioms()));
        int toIndex = Collections.min(Arrays.asList(10,unsatClasses.size()));
        List<OWLClass> actualUnsatClasses = new LinkedList<OWLClass>(unsatClasses.subList(0,toIndex));

        while (!actualUnsatClasses.isEmpty()) {
            for (OWLClass unsatClass : actualUnsatClasses)
                getModuleCalculator().calculateModule(unsatClass);
            Map<OWLClass, Set<OWLLogicalAxiom>> map = getModuleCalculator().getModuleMap();
            OWLClass actualUnsatClass = Collections.min(actualUnsatClasses,new ModuleSizeComparator(map));
            Set<OWLLogicalAxiom> axioms = new LinkedHashSet<OWLLogicalAxiom>(map.get(actualUnsatClass));
            Set<OWLLogicalAxiom> background = new LinkedHashSet<OWLLogicalAxiom>(axioms);
            background.retainAll(getOntoAxioms());
            //Set<? extends Set<OWLLogicalAxiom>> diagnoses = searchDiagnoses(axioms, background);
            //Set<OWLLogicalAxiom> partDiag = diagnosisOracle.chooseDiagnosis(diagnoses);
            Speed4JMeasurement.start("calculatepartdiag");
            Set<OWLLogicalAxiom> partDiag = getDiagSearcher().calculateDiag(axioms, background);
            Speed4JMeasurement.stop();

            for (OWLLogicalAxiom axiom : partDiag)
                logger.info("part diag axiom: " + axiom);
            logger.info("---");

            getModuleCalculator().removeAxiomsFromOntologyAndModules(partDiag);
            updatedLists(actualUnsatClasses, unsatClasses);
            targetDiagnosis.addAll(partDiag);
        }

        Speed4JMeasurement.stop();
        return targetDiagnosis;
    }

    protected void updatedLists (List<OWLClass> actualUnsat, List<OWLClass> allUnsat) {

        Set<OWLClass> toCheck = new HashSet<OWLClass>(actualUnsat);
        for (OWLClass unsatClass : toCheck) {
            //OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(createOntology(unsatMap.get(unsatClass)));
            if (getModuleCalculator().isSatisfiable(unsatClass)) {
                actualUnsat.remove(unsatClass);
                allUnsat.remove(unsatClass);
            }
        }
        toCheck = new HashSet<OWLClass>(allUnsat);
        toCheck.removeAll(actualUnsat);
        for (OWLClass unsatClass : toCheck) {
            //OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(createOntology(unsatMap.get(unsatClass)));
            if (!getModuleCalculator().isSatisfiable(unsatClass)) {
                actualUnsat.add(unsatClass);
                if (actualUnsat.size() == 10)
                    break;
            }
            else {
                allUnsat.remove(unsatClass);
            }
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

}
