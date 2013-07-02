package at.ainf.owlapi3.module.iterative.diagsearcher;

//import at.ainf.diagnosis.logging.old.MetricsManager;
import at.ainf.diagnosis.logging.MetricsLogger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.TreeSearch;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static at.ainf.owlapi3.util.SetUtils.createIntersection;
import static at.ainf.owlapi3.util.SetUtils.createUnion;

/**
 * Created with IntelliJ IDEA.
 * User: pr8
 * Date: 09.05.13
 * Time: 20:05
 * To change this template use File | Settings | File Templates.
 */
public class ModuleOptQuerDiagSearcher extends ModuleQuerDiagSearcher {

   private static Logger logger = LoggerFactory.getLogger(ModuleOptQuerDiagSearcher.class.getName());

   private MetricsLogger metricsLogger = MetricsLogger.getInstance();


   private Collection<Set<OWLLogicalAxiom>> collectedNonEntailedTCs = new LinkedHashSet<Set<OWLLogicalAxiom>>();



    public ModuleOptQuerDiagSearcher(String path, Set<OWLLogicalAxiom> correctAxioms, Set<OWLLogicalAxiom> falseAxioms, boolean isMinimizerActive) {
        super(path, correctAxioms, falseAxioms, null, isMinimizerActive);
    }

    public ModuleOptQuerDiagSearcher(String path, Set<OWLLogicalAxiom> correctAxioms, Set<OWLLogicalAxiom> falseAxioms, Map<OWLLogicalAxiom, BigDecimal> confidences, boolean isMinimizerActive) {
        super(path,correctAxioms,falseAxioms,confidences,isMinimizerActive);
    }



    private class QueryDiag {

        private Set<OWLLogicalAxiom> query;
        private Set<OWLLogicalAxiom> diagnosis;

        private QueryDiag(){

        }

        private Set<OWLLogicalAxiom> getQuery() {
            return query;
        }

        private void setQuery(Set<OWLLogicalAxiom> query) {
            this.query = query;
        }

        private Set<OWLLogicalAxiom> getDiagnosis() {
            return diagnosis;
        }

        private void setDiagnosis(Set<OWLLogicalAxiom> diagnosis) {
            this.diagnosis = diagnosis;
        }




    }

    private class Pair<A,B> {

        private final A first;
        private final B second;

        public Pair(A first, B second){
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }

    }





    private Set<OWLLogicalAxiom> getFirstAlternativeQuery(HashSet<Set<OWLLogicalAxiom>> diagnoses, Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> previousQuery){

        Set<OWLLogicalAxiom> unionOfDiags = createUnion(diagnoses);
        Set<OWLLogicalAxiom> intersectionOfDiags = createIntersection(diagnoses);

        Set<OWLLogicalAxiom> discAx = unionOfDiags;
        discAx.removeAll(intersectionOfDiags);
        discAx.retainAll(axioms);

        Iterator<Set<OWLLogicalAxiom>> diagIter = diagnoses.iterator();
        Set<OWLLogicalAxiom> d = diagIter.next();
        Set<OWLLogicalAxiom> query = new LinkedHashSet<OWLLogicalAxiom>(axioms);
        query.removeAll(d);
        query.retainAll(discAx);
        if(query.equals(previousQuery)){
            if(!diagIter.hasNext())
                throw new IllegalStateException("no eligible diagnosis found, BUT there must be such a diagnosis!");
            else{
                d = diagIter.next();
                query = new LinkedHashSet<OWLLogicalAxiom>(axioms);
                query.removeAll(d);
                query.retainAll(discAx);
                return query;
            }
        } else
            return query;
    }

    private QueryDiag getSecondAlternativeQuery(HashSet<Set<OWLLogicalAxiom>> diagnoses, Set<OWLLogicalAxiom> stronglyFalseQuery, Collection<Set<OWLLogicalAxiom>> entailedTCs){

        Set<OWLLogicalAxiom> intersectionOfDiags = createIntersection(diagnoses);

        Set<OWLLogicalAxiom> temp = null;
        for(Set<OWLLogicalAxiom> diag : diagnoses){
            temp = new HashSet<OWLLogicalAxiom>(diag);
            temp.removeAll(stronglyFalseQuery);
            if(!intersectionOfDiags.containsAll(temp)){
                QueryDiag qd = new QueryDiag();
                temp = minimizeQueryToNotAnsweredAxioms(temp,entailedTCs);
                qd.setQuery(temp);
                qd.setDiagnosis(diag);
                return qd;
            }
        }
        throw new IllegalStateException("no eligible diagnosis found, BUT there must be such a diagnosis!");
    }


    private Set<OWLLogicalAxiom> minimizeQueryToNotAnsweredAxioms(Set<OWLLogicalAxiom> query, Collection<Set<OWLLogicalAxiom>> entailedTCs){
        Set<OWLLogicalAxiom> axToRemove = new HashSet<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> axSet;
        for(OWLLogicalAxiom ax : query){
            axSet = Collections.singleton(ax);
            if(entailedTCs.contains(axSet) || collectedNonEntailedTCs.contains(axSet)){
                axToRemove.add(ax);
            }
        }
        Set<OWLLogicalAxiom> minimizedQuery = new HashSet<OWLLogicalAxiom>(query);
        minimizedQuery.removeAll(axToRemove);
        return minimizedQuery;
    }


    @Override
    public Set<OWLLogicalAxiom> calculateDiag(Set<OWLLogicalAxiom> module, Set<OWLLogicalAxiom> backg) {
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createSearch(module,backg);
        metricsLogger.createGauge("module-size", module.size());
        search.setMaxDiagnosesNumber(9);

        Set<OWLLogicalAxiom> possibleFaultyAxioms = new LinkedHashSet<OWLLogicalAxiom>(module);
        possibleFaultyAxioms.removeAll(backg);

        //QSS<OWLLogicalAxiom> qss = QSSFactory.createDynamicRiskQSS(0, 0.5, 0.4);
        QSS<OWLLogicalAxiom> qss = QSSFactory.createSplitInHalfQSS();
        CKK<OWLLogicalAxiom> ckk = new CKK<OWLLogicalAxiom>(search.getSearchable(), qss);
        ckk.setThreshold(0.1); //old: 0.01

        long time = System.currentTimeMillis();
        getTreeCreator().runSearch(search);
        time = System.currentTimeMillis() - time;
        logger.info ("time needed to search for diagnoses: " + time);
        Collection<Set<OWLLogicalAxiom>> diagnoses = new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses());

        //IterativeStatistics.avgTimeQueryGen.createNewValueGroup();
        //IterativeStatistics.avgQueryCard.createNewValueGroup();
        //IterativeStatistics.avgReactTime.createNewValueGroup();

        int numOfQueries = 0;
        long reactionTime = System.currentTimeMillis();
        while (diagnoses.size() > 1) {
            String lastLabel = "";
            Partition<OWLLogicalAxiom> best = null;
            try {
                metricsLogger.startTimer("calculatingquery");
                best = ckk.generatePartition(search.getDiagnoses());

                while (createUnion(collectedNonEntailedTCs).containsAll(best.partition)) {
                    Partition<OWLLogicalAxiom> next = ckk.nextPartition(best);
                    if (next == null) {
                        logger.info("we have no next alt query and all calculated queries are in NonEntailedTC");
                        break;
                    }
                    else
                        best = next;
                }

                lastLabel = metricsLogger.getLabelManager().getLabelsConc();
                long queryCalc = metricsLogger.stopTimer("calculatingquery");
                //IterativeStatistics.avgTimeQueryGen.addValue(queryCalc);
            } catch (SolverException e) {
                // e.printStackTrace();
            } catch (InconsistentTheoryException e) {
                // e.printStackTrace();
            }

            if (isMinimizerActive())
                minimizePartitionAx(best,search.getSearchable());

            metricsLogger.getHistogram("query-size").update(best.partition.size());

            logger.info(lastLabel + " size of query " + best.partition.size());
            for (OWLLogicalAxiom axiom : best.partition)
                logger.info("query axiom: " + axiom);
            logger.info("query axiom end");

            reactionTime = System.currentTimeMillis() - reactionTime;
            boolean posTcOrTargetDiagFound = false;
            boolean firstAltQuAsked = false;
            boolean secondAltQuAsked = false;
            Set<OWLLogicalAxiom> query = best.partition;
            QueryDiag qd = null;
            while(!posTcOrTargetDiagFound){
                Answer answer = askUser(query);
                numOfQueries++;
                //answer = Answer.FALSE;
                metricsLogger.getCounter("numofqueries").inc();

                switch(answer){
                    case TRUE:
                        logger.info("user answered query: " + Answer.TRUE.name());
                        search.getSearchable().getKnowledgeBase().addEntailedTest(new TreeSet<OWLLogicalAxiom>(best.partition));
                        posTcOrTargetDiagFound = true;
                        break;
                    case STRONGLY_FALSE:
                        for (OWLLogicalAxiom axiom : best.partition)
                            collectedNonEntailedTCs.add(new TreeSet<OWLLogicalAxiom>(Collections.singleton(axiom)));
                        logger.info("user answered query: " + Answer.STRONGLY_FALSE.name());
                        if(!firstAltQuAsked){
                            query = getFirstAlternativeQuery(new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses()),possibleFaultyAxioms, query);
                            firstAltQuAsked = true;
                        }else if(!secondAltQuAsked){
                            qd = getSecondAlternativeQuery(new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses()),query,search.getSearchable().getKnowledgeBase().getEntailedTests());
                            query = qd.getQuery();
                            if(query.isEmpty()){
                                Set<OWLLogicalAxiom> targetDiag = qd.getDiagnosis();
                                targetDiag = extendWithNonEntailedTCs(targetDiag,possibleFaultyAxioms);
                                collectedNonEntailedTCs.clear();
                                saveTestsFromKB(search.getSearchable().getKnowledgeBase());
                                return targetDiag;
                            }
                            secondAltQuAsked = true;
                        }else{
                            Set<OWLLogicalAxiom> targetDiag = qd.getDiagnosis();
                            targetDiag = extendWithNonEntailedTCs(targetDiag,possibleFaultyAxioms);
                            collectedNonEntailedTCs.clear();
                            saveTestsFromKB(search.getSearchable().getKnowledgeBase());
                            return targetDiag;
                        }
                        break;
                    case FALSE:
                        for (OWLLogicalAxiom axiom : query){
                            if(askUser(axiom)){
                                search.getSearchable().getKnowledgeBase().addEntailedTest(Collections.singleton(axiom));
                            } else{
                                collectedNonEntailedTCs.add(new TreeSet<OWLLogicalAxiom>(Collections.singleton(axiom)));
                            }
                        }
                        logger.info("user answered query: " + Answer.FALSE.name());
                        posTcOrTargetDiagFound = true;
                        break;
                    default:
                        logger.info("error in query answering!!");
                }

                if (!posTcOrTargetDiagFound) {
                    time = System.currentTimeMillis();
                    getTreeCreator().runSearch(search);
                    time = System.currentTimeMillis() - time;
                    logger.info ("time needed to search for diagnoses: " + time);
                    if (search.getDiagnoses().size() == 1)
                        posTcOrTargetDiagFound = true;
                }


            }

            reactionTime = 0;

            if (search.getDiagnoses().size() != 1) {
                time = System.currentTimeMillis();
                getTreeCreator().runSearch(search);
                time = System.currentTimeMillis() - time;
                logger.info ("time needed to search for diagnoses: " + time);
            }
            diagnoses = new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses());
        }
        logger.info("number of queries: " + numOfQueries);
        //IterativeStatistics.numOfQueries.add((long)numOfQueries);

        if (diagnoses.isEmpty()) {
            collectedNonEntailedTCs.clear();
            saveTestsFromKB(search.getSearchable().getKnowledgeBase());
            return Collections.emptySet();
        }
        else {
            Set<OWLLogicalAxiom> targetDiag = diagnoses.iterator().next();
            targetDiag = extendWithNonEntailedTCs(targetDiag,possibleFaultyAxioms);
            collectedNonEntailedTCs.clear();
            saveTestsFromKB(search.getSearchable().getKnowledgeBase());
            return targetDiag;
        }

    }

    private Set<OWLLogicalAxiom> extendWithNonEntailedTCs(Set<OWLLogicalAxiom> diagnosis, Set<OWLLogicalAxiom> axioms){
        Set<OWLLogicalAxiom> extended_Diagnosis = new LinkedHashSet<OWLLogicalAxiom>(diagnosis);
        for(Set<OWLLogicalAxiom> testcase : collectedNonEntailedTCs){
            if(testcase.size() == 1 && axioms.containsAll(testcase))
                extended_Diagnosis.addAll(testcase);
        }
        return extended_Diagnosis;
    }



}
