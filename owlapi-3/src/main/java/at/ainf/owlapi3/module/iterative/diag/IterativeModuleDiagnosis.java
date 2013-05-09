package at.ainf.owlapi3.module.iterative.diag;

import at.ainf.diagnosis.Speed4JMeasurement;
import at.ainf.owlapi3.module.iterative.ModuleDiagSearcher;
import at.ainf.owlapi3.reasoner.ExtendedStructuralReasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
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
            Speed4JMeasurement.start("calculatemodule");
            Map<OWLClass, Set<OWLLogicalAxiom>> map = getModuleCalculator().getModuleMap();
            OWLClass actualUnsatClass = Collections.min(actualUnsatClasses,new ModuleSizeComparator(map));
            Set<OWLLogicalAxiom> axioms = new LinkedHashSet<OWLLogicalAxiom>(map.get(actualUnsatClass));
            Set<OWLLogicalAxiom> background = new LinkedHashSet<OWLLogicalAxiom>(axioms);
            background.retainAll(getOntoAxioms());
            //Set<? extends Set<OWLLogicalAxiom>> diagnoses = searchDiagnoses(axioms, background);
            //Set<OWLLogicalAxiom> partDiag = diagnosisOracle.chooseDiagnosis(diagnoses);
            Speed4JMeasurement.start("calculatepartdiag");
            IterativeStatistics.avgCoherencyTime.createNewValueGroup();
            IterativeStatistics.avgConsistencyTime.createNewValueGroup();
            IterativeStatistics.avgConsistencyCheck.createNewValueGroup();
            IterativeStatistics.avgCoherencyCheck.createNewValueGroup();
            Set<OWLLogicalAxiom> partDiag = getDiagSearcher().calculateDiag(axioms, background);
            Speed4JMeasurement.stop();

            for (OWLLogicalAxiom axiom : partDiag)
                logger.info("part diag axiom: " + axiom);
            logger.info("---");

            long timeModule = Speed4JMeasurement.stop();
            IterativeStatistics.moduleTime.add(timeModule);
            getModuleCalculator().removeAxiomsFromOntologyAndModules(partDiag);
            getModuleCalculator().updatedLists(actualUnsatClasses, unsatClasses);
            targetDiagnosis.addAll(partDiag);
        }
        IterativeStatistics.logAndClear(logger, IterativeStatistics.avgCardCS, "average cardinality CS");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.cardHS, "cardinality HS");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.numberCS, "number CS");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.moduleSize, "module size");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.diagnosisTime, "diagnosis time");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.moduleTime, "module time");
        IterativeStatistics.logAndClear (logger, IterativeStatistics.avgConsistencyTime, "consistency time");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.avgCoherencyTime, "coherency time");
        IterativeStatistics.logAndClear (logger, IterativeStatistics.avgConsistencyCheck, "consistency checks");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.avgCoherencyCheck, "coherency checks");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.numOfQueries, "num of queries");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.avgTimeQueryGen, "time querygen");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.avgReactTime, "reaction time");
        IterativeStatistics.logAndClear(logger, IterativeStatistics.avgQueryCard, "query card");

        Speed4JMeasurement.stop();
        return targetDiagnosis;
    }

    protected class ChildsComparator implements Comparator<OWLClass> {
        private Collection<OWLLogicalAxiom> mappings;
        private Collection<OWLLogicalAxiom> ontoAxioms;
        private final Map<OWLClass, Integer> map;

        private static final int LEAST_NUMBER_OF_NODES = 100;

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
            ExtendedStructuralReasoner reasoner = new ExtendedStructuralReasoner (createOntology(allAxioms),
                    new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
            logger.info(" having " + unsatClasses.size() + " unsat classes to calculate childs ");
            for (OWLClass unsatClass : unsatClasses) {
                Set<OWLClass> childs = new HashSet<OWLClass>(
                        reasoner.getSubClasses(unsatClass,false,LEAST_NUMBER_OF_NODES).getFlattened());
                childs.remove(BOT_CLASS);
                logger.info("unsat class " + unsatClass + ", number of childs: " + childs.size());
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
