package at.ainf.owlapi3.module.iterative;

import at.ainf.diagnosis.Speed4JMeasurement;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.owlapi3.module.iterative.diag.IterativeStatistics;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pr8
 * Date: 09.05.13
 * Time: 20:05
 * To change this template use File | Settings | File Templates.
 */
public class ModuleOptQuerDiagSearcher extends ModuleQuerDiagSearcher {

   private static Logger logger = LoggerFactory.getLogger(ModuleQuerDiagSearcher.class.getName());

    private Collection<Set<OWLLogicalAxiom>> collectedNonEntailedTCs = new LinkedHashSet<Set<OWLLogicalAxiom>>();



    public ModuleOptQuerDiagSearcher(String path, Set<OWLLogicalAxiom> correctAxioms, Set<OWLLogicalAxiom> falseAxioms, boolean isMinimizerActive) {
        super(path, correctAxioms, falseAxioms, null, isMinimizerActive);
    }

    public ModuleOptQuerDiagSearcher(String path, Set<OWLLogicalAxiom> correctAxioms, Set<OWLLogicalAxiom> falseAxioms, Map<OWLLogicalAxiom, BigDecimal> confidences, boolean isMinimizerActive) {
        super(path,correctAxioms,falseAxioms,confidences,isMinimizerActive);
    }

    private enum Answer {
        TRUE, FALSE, STRONGLY_FALSE;
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


    private boolean getUserAnswer(OWLLogicalAxiom axiom) {
        if (correctAxioms.contains(axiom))
            return true;
        else if (falseAxioms.contains(axiom))
            return false;
        else {
            throw new IllegalStateException();
        }
    }

    private Answer getUserAnswer(Set<OWLLogicalAxiom> query) {
        if (correctAxioms.containsAll(query))
            return Answer.TRUE;
        else if (falseAxioms.containsAll(query))
            return Answer.STRONGLY_FALSE;
        else {
            return Answer.FALSE;
        }
    }

    private Set<OWLLogicalAxiom> getIntersectionOfDiags(Set<Set<OWLLogicalAxiom>> diagnoses){
        final Iterator<Set<OWLLogicalAxiom>> i = diagnoses.iterator();
        Set<OWLLogicalAxiom> intersectionOfDiags = new LinkedHashSet<OWLLogicalAxiom>(i.next());
        while (i.hasNext())
            intersectionOfDiags.retainAll(i.next());
        return intersectionOfDiags;
    }

    private Set<OWLLogicalAxiom> getUnionOfDiags(Set<Set<OWLLogicalAxiom>> diagnoses){
        Set<OWLLogicalAxiom> unionOfDiags = new LinkedHashSet<OWLLogicalAxiom>();
        for(Set<OWLLogicalAxiom> diag : diagnoses){
            unionOfDiags.addAll(diag);
        }
        return unionOfDiags;
    }

    private Set<OWLLogicalAxiom> getFirstAlternativeQuery(HashSet<Set<OWLLogicalAxiom>> diagnoses, Set<OWLLogicalAxiom> axioms){

        Set<OWLLogicalAxiom> unionOfDiags = getUnionOfDiags(diagnoses);
        Set<OWLLogicalAxiom> intersectionOfDiags = getIntersectionOfDiags(diagnoses);

        Set<OWLLogicalAxiom> discAx = unionOfDiags;
        discAx.removeAll(intersectionOfDiags);
        discAx.retainAll(axioms);

        Set<OWLLogicalAxiom> d = diagnoses.iterator().next();
        Set<OWLLogicalAxiom> query = new LinkedHashSet<OWLLogicalAxiom>(axioms);
        query.removeAll(d);
        query.retainAll(discAx);
        return query;
    }

    private QueryDiag getSecondAlternativeQuery(HashSet<Set<OWLLogicalAxiom>> diagnoses, Set<OWLLogicalAxiom> stronglyFalseQuery){

        Set<OWLLogicalAxiom> intersectionOfDiags = getIntersectionOfDiags(diagnoses);

        Set<OWLLogicalAxiom> temp = null;
        for(Set<OWLLogicalAxiom> diag : diagnoses){
            temp = new HashSet<OWLLogicalAxiom>(diag);
            temp.removeAll(stronglyFalseQuery);
            if(!intersectionOfDiags.containsAll(temp)){
                QueryDiag qd = new QueryDiag();
                qd.setQuery(temp);
                qd.setDiagnosis(diag);
                return qd;
            }
        }
        throw new IllegalStateException("no eligible diagnosis found, BUT there must be such a diagnosis!");
    }



    @Override
    public Set<OWLLogicalAxiom> calculateDiag(Set<OWLLogicalAxiom> module, Set<OWLLogicalAxiom> backg) {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createSearch(module,backg);
        search.setMaxDiagnosesNumber(9);

        Set<OWLLogicalAxiom> possibleFaultyAxioms = new LinkedHashSet<OWLLogicalAxiom>(module);
        possibleFaultyAxioms.removeAll(backg);

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
        while (diagnoses.size() > 1) {
            String lastLabel = "";
            Partition<OWLLogicalAxiom> best = null;
            try {
                Speed4JMeasurement.start("calculatingpartition");
                best = ckk.generatePartition(search.getDiagnoses());
                lastLabel = Speed4JMeasurement.getLabelOfLastStopWatch();
                long queryCalc = Speed4JMeasurement.stop();
                IterativeStatistics.avgTimeQueryGen.addValue(queryCalc);
            } catch (SolverException e) {
                // e.printStackTrace();
            } catch (InconsistentTheoryException e) {
                // e.printStackTrace();
            }

            if (isMinimizerActive())
                minimizePartitionAx(best,search.getSearchable());

            IterativeStatistics.avgQueryCard.addValue((long)best.partition.size());

            logger.info(lastLabel + " size of partition " + best.partition.size());
            for (OWLLogicalAxiom axiom : best.partition)
                logger.info("query axiom: " + axiom);
            logger.info("query axiom end");

            reactionTime = System.currentTimeMillis() - reactionTime;
            IterativeStatistics.avgReactTime.addValue(reactionTime);
            boolean posTcOrTargetDiagFound = false;
            boolean firstAltQuAsked = false;
            boolean secondAltQuAsked = false;
            Set<OWLLogicalAxiom> query = best.partition;
            QueryDiag qd = null;
            while(!posTcOrTargetDiagFound){
                Answer answer = getUserAnswer(query);
                numOfQueries++;


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
                            query = getFirstAlternativeQuery(new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses()),possibleFaultyAxioms);
                            firstAltQuAsked = true;
                        }else if(!secondAltQuAsked){
                            qd = getSecondAlternativeQuery(new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses()),query);
                            query = qd.getQuery();
                            secondAltQuAsked = true;
                        }else{
                            Set<OWLLogicalAxiom> targetDiag = qd.getDiagnosis();
                            targetDiag = extendWithNonEntailedTCs(targetDiag,possibleFaultyAxioms);
                            return targetDiag;
                        }
                        break;
                    case FALSE:
                        for (OWLLogicalAxiom axiom : query){
                            if(getUserAnswer(axiom)){
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
                    runSearch(search);
                    time = System.currentTimeMillis() - time;
                    logger.info ("time needed to search for diagnoses: " + time);
                    if (search.getDiagnoses().size() == 1)
                        posTcOrTargetDiagFound = true;
                }


            }

            reactionTime = 0;

            if (search.getDiagnoses().size() != 1) {
                time = System.currentTimeMillis();
                runSearch(search);
                time = System.currentTimeMillis() - time;
                logger.info ("time needed to search for diagnoses: " + time);
            }
            diagnoses = new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses());
        }
        logger.info("number of queries: " + numOfQueries);
        IterativeStatistics.numOfQueries.add((long)numOfQueries);

        if (diagnoses.isEmpty())
            return Collections.emptySet();
        else {
            Set<OWLLogicalAxiom> targetDiag = diagnoses.iterator().next();
            targetDiag = extendWithNonEntailedTCs(targetDiag,possibleFaultyAxioms);
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
