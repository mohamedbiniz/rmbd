package at.ainf.owlapi3.module.iterative.modulediagnosis;


import at.ainf.diagnosis.logging.MetricsLogger;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.owlapi3.module.iterative.diagsearcher.ModuleDiagSearcher;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Multimap;
import at.ainf.owlapi3.reasoner.ExtendedStructuralReasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.03.13
 * Time: 09:54
 * To change this template use File | Settings | File Templates.
 */
public class IterativeModuleDiagnosis extends AbstractModuleDiagnosis {

    public static final int MAX_UNSAT_CLASSES = 10;
    private static Logger logger = LoggerFactory.getLogger(IterativeModuleDiagnosis.class.getName());

    private MetricsLogger metricsLogger = MetricsLogger.getInstance();
    
    private final boolean sortNodes;

    public IterativeModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms,
                                    OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher,
                                    boolean sortNodes) {

        super(mappings, ontoAxioms, factory, moduleDiagSearcher);

        this.sortNodes = sortNodes;

    }

    protected boolean isSortNodes() {
        return sortNodes;
    }

    //private MetricsManager metricsManager = MetricsManager.getInstance();

    public Set<FormulaSet<OWLLogicalAxiom>> start() {
        Set<OWLLogicalAxiom> targetDiagnosis = new HashSet<OWLLogicalAxiom>();
        metricsLogger.startTimer("calculatetargetdiag");
        List<OWLClass> unsatClasses = getModuleCalculator().getInitialUnsatClasses(Collections.<OWLClass>emptySet(),
                MAX_UNSAT_CLASSES);
        if (isSortNodes())
            Collections.sort(unsatClasses, new ChildsComparator(unsatClasses, getMappings(), getOntoAxioms()));
        int toIndex = Collections.min(Arrays.asList(MAX_UNSAT_CLASSES, unsatClasses.size()));
        List<OWLClass> actualUnsatClasses = new LinkedList<OWLClass>(unsatClasses.subList(0, toIndex));

        while (!actualUnsatClasses.isEmpty()) {
            metricsLogger.startTimer("moduleTimeNew");
            metricsLogger.startTimer("calculatemodule");
            getModuleCalculator().calculateModules(actualUnsatClasses);
            Map<OWLClass, Set<OWLLogicalAxiom>> map = getModuleCalculator().getModuleMap();

            getModuleCalculator().sortUnsatisfiableClasses(actualUnsatClasses);
            OWLClass actualUnsatClass = actualUnsatClasses.get(0);

            Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>(map.get(actualUnsatClass));

            actualUnsatClasses.remove(actualUnsatClass);
            if (axioms.size() > 10000){
                Multimap<OWLAxiom,OWLClass> modMap = getModuleCalculator().clusterModule(axioms);
                for (OWLAxiom owlAxiom : modMap.keySet()) {
                    Collection<OWLClass> classes = modMap.get(owlAxiom);
                    Collection<OWLClass> subModule = getModuleCalculator().calculateModules(classes);
                    if (logger.isDebugEnabled())
                        logger.debug("Submodule for " + owlAxiom + " including " + subModule.size() + " axioms");
                }
            }

            //Set<OWLLogicalAxiom> intersection = new HashSet<OWLLogicalAxiom>(map.get(owlClass));
            for (OWLClass unsatClass : actualUnsatClasses) {
                Set<OWLLogicalAxiom> module = map.get(unsatClass);
                if (module.size() + axioms.size() < 500)
                    axioms.addAll(module); // axioms = new HashSet<OWLLogicalAxiom>(module);
                /*else {
                    Sets.SetView<OWLLogicalAxiom> intersection = Sets.intersection(axioms, module);
                    if (!intersection.isEmpty() && intersection.size() < axioms.size() &&
                            !getModuleCalculator().isConsistent(intersection))
                        axioms = new HashSet<OWLLogicalAxiom>(intersection);
                } */
            }

            metricsLogger.stopTimer("calculatemodule");
            /*
            for (OWLClass unsatClass : actualUnsatClasses)
                axioms.addAll(map.get(unsatClass));
            */
            Set<OWLLogicalAxiom> background = new LinkedHashSet<OWLLogicalAxiom>(axioms);
            background.retainAll(getOntoAxioms());

            if (logger.isInfoEnabled())
                logger.info("Processing module with BK: " + background.size() + " from " + axioms.size());

            //Set<? extends Set<OWLLogicalAxiom>> diagnoses = searchDiagnoses(axioms, background);
            //Set<OWLLogicalAxiom> partDiag = diagnosisOracle.chooseDiagnosis(diagnoses);
            metricsLogger.startTimer("calculatepartdiag");
            //metricsManager.getMetrics().histogram("avgCoherencyTimeMetric");
            //IterativeStatistics.avgCoherencyTime.createNewValueGroup();
            //IterativeStatistics.avgConsistencyTime.createNewValueGroup();
            //IterativeStatistics.avgConsistencyCheck.createNewValueGroup();
            //IterativeStatistics.avgCoherencyCheck.createNewValueGroup();
            metricsLogger.addLabel("modulediag");
            Set<OWLLogicalAxiom> partDiag = getDiagSearcher().calculateDiag(axioms, background);
            MetricRegistry metric = metricsLogger.removeLabel("modulediag");
            metricsLogger.stopTimer("calculatepartdiag");

            // TODO null pointer exception
            if (metric.getGauges().get("module-size") != null)
              metricsLogger.getHistogram("moduleSizes").update((Integer)metric.getGauges().get("module-size").getValue());
            if (metric.getGauges().get("card-hs") != null)
                metricsLogger.getHistogram("cardHs").update((Integer)metric.getGauges().get("card-hs").getValue());
            if (metric.getHistograms().get("card-cs") != null)
                metricsLogger.getHistogram("cardCs").update((long)metric.getHistograms().get("card-cs").getSnapshot().getMean());
            if (metric.getHistograms().get("card-cs") != null)
                metricsLogger.getHistogram("numCs").update(metric.getHistograms().get("card-cs").getCount());
            if (metric.getCounters().get("numofqueries") != null)
                metricsLogger.getHistogram("numOfQueries").update(metric.getCounters().get("numofqueries").getCount());
            metricsLogger.getHistogram("reactionTime").update((long)metric.getTimers().get("reactionTime").getSnapshot().getMean());
            metricsLogger.updateHistogram("queryCard",metric.getHistograms().get("partition-size").getSnapshot().getValues());
            //metricsLogger.getHistogram("queryCard").update((long)metric.getHistograms().get("partition-size").getSnapshot().getMean());
            metricsLogger.getHistogram("timeQueryGen").update((long)metric.getTimers().get("calculatingpartition").getSnapshot().getMean());
            metricsLogger.getHistogram("avgRunofhstree").update((long)metric.getTimers().get("runofhstree").getSnapshot().getMean());
            metricsLogger.getHistogram("moduleNumConsistencyChecks").update(metric.getTimers().get("consistencyChecks").getCount());
            metricsLogger.getHistogram("moduleTimeConsistencyChecks").update((long)metric.getTimers().get("consistencyChecks").getSnapshot().getMean());
            metricsLogger.getHistogram("moduleNumCoherencyChecks").update(metric.getTimers().get("coherencyChecks").getCount());
            metricsLogger.getHistogram("moduleTimeCoherencyChecks").update((long)metric.getTimers().get("coherencyChecks").getSnapshot().getMean());

            for (OWLLogicalAxiom axiom : partDiag)
                logger.info("part diag axiom: " + axiom);
            logger.info("---");

            long timeModule = metricsLogger.stopTimer("moduleTimeNew");
            //IterativeStatistics.moduleTime.add(timeModule);
            getModuleCalculator().removeAxiomsFromOntologyAndModules(partDiag);
            getModuleCalculator().updatedLists(actualUnsatClasses, unsatClasses, MAX_UNSAT_CLASSES);
            targetDiagnosis.addAll(partDiag);
        }
        //metricsManager.logAllMetrics();
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.avgCardCS, "average cardinality CS");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.cardHS, "cardinality HS");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.numberCS, "number CS");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.moduleSize, "module size");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.diagnosisTime, "diagnosis time");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.moduleTime, "module time");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.avgConsistencyTime, "consistency time");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.avgCoherencyTime, "coherency time");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.avgConsistencyCheck, "consistency checks");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.avgCoherencyCheck, "coherency checks");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.numOfQueries, "num of queries");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.avgTimeQueryGen, "time querygen");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.avgReactTime, "reaction time");
        //IterativeStatistics.logAndClear(logger, IterativeStatistics.avgQueryCard, "query card");


        metricsLogger.stopTimer("calculatetargetdiag");
        return Collections.singleton(createFormularSet(targetDiagnosis));
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

        protected Map<OWLClass, Integer> calculateNumberTransitiveUnsatChilds(Collection<OWLClass> unsatClasses) {
            Map<OWLClass, Integer> result = new LinkedHashMap<OWLClass, Integer>();
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
                result.put(unsatClass, childs.size());
            }
            return result;
        }

        @Override
        public int compare(OWLClass o1, OWLClass o2) {
            return map.get(o1).compareTo(map.get(o2));
        }

    }

}
