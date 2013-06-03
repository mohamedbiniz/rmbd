package at.ainf.owlapi3.module.iterative;

import at.ainf.diagnosis.Searchable;
//import at.ainf.diagnosis.logging.old.MetricsManager;
import at.ainf.diagnosis.logging.MetricsLogger;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.logging.old.IterativeStatistics;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.03.13
 * Time: 14:13
 * To change this template use File | Settings | File Templates.
 */
public class ModuleQuerDiagSearcher extends ModuleTargetDiagSearcher {

    private static Logger logger = LoggerFactory.getLogger(ModuleQuerDiagSearcher.class.getName());

    private boolean isMinimizerActive;

    Set<OWLLogicalAxiom> correctAxioms;

    Set<OWLLogicalAxiom> falseAxioms;

    public ModuleQuerDiagSearcher(String path, Set<OWLLogicalAxiom> correctAxioms, Set<OWLLogicalAxiom> falseAxioms, boolean isMinimizerActive) {
        this (path, correctAxioms, falseAxioms, null, isMinimizerActive);
    }

    public ModuleQuerDiagSearcher(String path, Set<OWLLogicalAxiom> correctAxioms, Set<OWLLogicalAxiom> falseAxioms, Map<OWLLogicalAxiom, BigDecimal> confidences, boolean isMinimizerActive) {
        super(path,confidences);
        this.isMinimizerActive = isMinimizerActive;
        this.correctAxioms = correctAxioms;
        this.falseAxioms = falseAxioms;
    }

    public boolean isMinimizerActive() {
            return isMinimizerActive;
        }

    public void setMinimizerActive(boolean minimizerActive) {
        isMinimizerActive = minimizerActive;
    }

    protected boolean askUser(Partition<OWLLogicalAxiom> partition) throws AnswerException {
        if (correctAxioms.containsAll(partition.partition))
            return true;
        else if (falseAxioms.containsAll(partition.partition))
            return false;
        else {
            //logger.info("we doesn't know an answer for sure (not all axioms false or correct) so we return false ");
            //for (OWLLogicalAxiom axiom : partition.partition)
            //    logger.info(axiom + ", " + correctAxioms.contains(axiom) + ", " + falseAxioms.contains(axiom));
            //logger.info(partition.dx.size() + ", " + partition.dnx.size() + ", " + partition.dz.size() + ", ");
            throw new AnswerException("some axioms true, some false");

        }
    }

    protected void minimizePartitionAx(Partition<OWLLogicalAxiom> query, Searchable<OWLLogicalAxiom> searchable) {
        if (query.partition == null) return;
        QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(query, searchable);
        QuickXplain<OWLLogicalAxiom> q = new QuickXplain<OWLLogicalAxiom>();
        try {
            query.partition = q.search(mnz, query.partition).iterator().next();
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    class AnswerException extends Exception {
        AnswerException(String message) {
            super(message);
        }
    }

    protected boolean askUser(OWLLogicalAxiom axiom) {
        if (correctAxioms.contains(axiom))
            return true;
        else if (falseAxioms.contains(axiom))
            return false;
        else
            throw new IllegalStateException("don't know answer");

    }

    private MetricsLogger metricsLogger = MetricsLogger.getInstance();

    //private MetricsManager metricsManager = MetricsManager.getInstance();

    @Override
    public Set<OWLLogicalAxiom> calculateDiag(Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createSearch(axioms,backg);
        MetricsLogger.getInstance().createGauge("module-size",axioms.size());
        search.setMaxDiagnosesNumber(9);

        //QSS<OWLLogicalAxiom> qss = QSSFactory.createDynamicRiskQSS(0, 0.5, 0.4);
        QSS<OWLLogicalAxiom> qss = QSSFactory.createSplitInHalfQSS();
        CKK<OWLLogicalAxiom> ckk = new CKK<OWLLogicalAxiom>(search.getSearchable(), qss);
        ckk.setThreshold(0.1); //old: 0.01

        long time = System.currentTimeMillis();
        runSearch(search);
        time = System.currentTimeMillis() - time;
        logger.info ("time needed to search for diagnoses: " + time);
        Collection<Set<OWLLogicalAxiom>> diagnoses = new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses());

        IterativeStatistics.avgTimeQueryGen.createNewValueGroup();
        IterativeStatistics.avgQueryCard.createNewValueGroup();
        IterativeStatistics.avgReactTime.createNewValueGroup();

        int numOfQueries = 0;
        long reactionTime = System.currentTimeMillis();
        MetricsLogger.getInstance().startTimer("reactionTime");
        while (diagnoses.size() > 1) {
            String lastLabel = "";
            Partition<OWLLogicalAxiom> best = null;
            try {
                metricsLogger.startTimer("calculatingpartition");
                best = ckk.generatePartition(search.getDiagnoses());
                lastLabel = metricsLogger.getLabelsConcat();
                long queryCalc = metricsLogger.stopTimer("calculatingpartition");
                IterativeStatistics.avgTimeQueryGen.addValue(queryCalc);
            } catch (SolverException e) {
                // e.printStackTrace();
            } catch (InconsistentTheoryException e) {
                // e.printStackTrace();
            }

            if (isMinimizerActive())
                minimizePartitionAx(best,search.getSearchable());

            IterativeStatistics.avgQueryCard.addValue((long)best.partition.size());
            MetricsLogger.getInstance().getHistogram("partition-size").update(best.partition.size());

            logger.info(lastLabel + " size of partition " + best.partition.size());
            for (OWLLogicalAxiom axiom : best.partition)
                logger.info("query axiom: " + axiom);
            logger.info("query axiom end");

            try {
                MetricsLogger.getInstance().stopTimer("reactionTime");
                reactionTime = System.currentTimeMillis() - reactionTime;
                IterativeStatistics.avgReactTime.addValue(reactionTime);
                boolean answer = askUser(best);
                logger.info("user answered query: All axioms " + answer);
                numOfQueries++;
                MetricsLogger.getInstance().getCounter("numfofqueries").inc();

                if (answer)
                    search.getSearchable().getKnowledgeBase().addEntailedTest(new TreeSet<OWLLogicalAxiom>(best.partition));
                else
                    search.getSearchable().getKnowledgeBase().addNonEntailedTest(new TreeSet<OWLLogicalAxiom>(best.partition));
                    //for (OWLLogicalAxiom axiom : best.partition)
                    //    search.getSearchable().getKnowledgeBase().addNonEntailedTest(new TreeSet<OWLLogicalAxiom>(Collections.singleton(axiom)));
            }
            catch (AnswerException e) {
                logger.info("user cannot answer this query ");
                for (OWLLogicalAxiom axiom : best.partition) {
                    boolean answer = askUser(axiom);
                    Set<OWLLogicalAxiom> testcase = new TreeSet<OWLLogicalAxiom>(Collections.singleton(axiom));
                    logger.info("user answers part of query " + answer);
                    numOfQueries++;
                    MetricsLogger.getInstance().getCounter("numfofqueries").inc();

                    if (answer)
                        search.getSearchable().getKnowledgeBase().addEntailedTest(testcase);
                    else
                        search.getSearchable().getKnowledgeBase().addNonEntailedTest(testcase);
                }
                // search.reset();
            }
            reactionTime = 0;


            time = System.currentTimeMillis();
            runSearch(search);
            time = System.currentTimeMillis() - time;
            logger.info ("time needed to search for diagnoses: " + time);
            diagnoses = new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses());
        }
        logger.info("number of queries: " + numOfQueries);
        IterativeStatistics.numOfQueries.add((long)numOfQueries);

        if (diagnoses.isEmpty())
            return Collections.emptySet();
        else
            return diagnoses.iterator().next();

    }



}
