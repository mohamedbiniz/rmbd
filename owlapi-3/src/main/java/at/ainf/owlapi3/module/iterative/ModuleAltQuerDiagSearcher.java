package at.ainf.owlapi3.module.iterative;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.HsTreeSearch;
import java.math.BigDecimal;

import javafx.util.converter.BigDecimalStringConverter;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pr8
 * Date: 22.04.13
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public class ModuleAltQuerDiagSearcher extends ModuleQuerDiagSearcher  {

    private static Logger logger = LoggerFactory.getLogger(ModuleAltQuerDiagSearcher.class.getName());
    private int maxAxiomsPerQuery = 10;

    public ModuleAltQuerDiagSearcher(String path, Set<OWLLogicalAxiom> correctAxioms, Set<OWLLogicalAxiom> falseAxioms, boolean isMinimizerActive) {
        super(path, correctAxioms, falseAxioms, null, isMinimizerActive);
    }

    /*
    public ModuleAltQuerDiagSearcher(String path, Set<OWLLogicalAxiom> correctAxioms, Set<OWLLogicalAxiom> falseAxioms, Map<OWLLogicalAxiom, BigDecimal> confidences, boolean isMinimizerActive) {
        super(path,correctAxioms,falseAxioms,confidences,isMinimizerActive);
    }

    /*@Override
        public Set<OWLLogicalAxiom> calculateDiag(Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> backg) {
            HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createSearch(axioms,backg);
            search.setMaxDiagnosesNumber(9);
            search.getCostsEstimator().getFormulaSetCosts(search.getDiagnoses().)

            QSS<OWLLogicalAxiom> qss = QSSFactory.createDynamicRiskQSS(0, 0.5, 0.4);
            CKK<OWLLogicalAxiom> ckk = new CKK<OWLLogicalAxiom>(search.getSearchable(), qss);
            ckk.setThreshold(0.01);

            long time = System.currentTimeMillis();
            runSearch(search);
            time = System.currentTimeMillis() - time;
            logger.info ("time needed to search for diagnoses: " + time);
            Collection<Set<OWLLogicalAxiom>> diagnoses = new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses());

            boolean found = false;
            int numOfQueries = 0;
            while (diagnoses.size() > 1 && !found) {

                Set<OWLLogicalAxiom> query = getQuery(diagnoses);
                logger.info("Query consists of the following " + query.size() + " axioms:");
                Iterator it = query.iterator();
                while(it.hasNext()){
                    logger.info(it.next());
                }
                *//*try {

                    //best = ckk.generatePartition(search.getDiagnoses());
                } catch (SolverException e) {
                    // e.printStackTrace();
                } catch (InconsistentTheoryException e) {
                    // e.printStackTrace();
                }*//*

                try {
                    boolean answer = askUser(query);
                    logger.info("user answered query by" + answer);
                    numOfQueries++;

                    if (answer)
                        search.getSearchable().getKnowledgeBase().addEntailedTest(new TreeSet<OWLLogicalAxiom>(best.partition));
                    else
                        for (OWLLogicalAxiom axiom : best.partition)
                            search.getSearchable().getKnowledgeBase().addNonEntailedTest(new TreeSet<OWLLogicalAxiom>(Collections.singleton(axiom)));
                }
                catch (AnswerException e) {
                    logger.info("user cannot answer this query ");
                    for (OWLLogicalAxiom axiom : best.partition) {
                        boolean answer = askUser(axiom);
                        Set<OWLLogicalAxiom> testcase = new TreeSet<OWLLogicalAxiom>(Collections.singleton(axiom));
                        logger.info("user answers part of query " + answer);
                        numOfQueries++;

                        if (answer)
                            search.getSearchable().getKnowledgeBase().addEntailedTest(testcase);
                        else
                            search.getSearchable().getKnowledgeBase().addNonEntailedTest(testcase);
                    }
                }


                time = System.currentTimeMillis();
                runSearch(search);
                time = System.currentTimeMillis() - time;
                logger.info ("time needed to search for diagnoses: " + time);
                diagnoses = new HashSet<Set<OWLLogicalAxiom>>(search.getDiagnoses());
            }
            logger.info("number of queries: " + numOfQueries);
            return diagnoses.iterator().next();

        }


    protected boolean askUser(Set<OWLLogicalAxiom> query) throws AnswerException {
            if (falseAxioms.containsAll(query)){
                logger.info("Query is STRONG FALSE");
                return false;
            }
            else if (correctAxioms.containsAll(query)){
                logger.info("Query is TRUE");
                return true;
            }
            else {
                logger.info("Query is FALSE");
                throw new AnswerException("don't know answer");
            }
    }

    private Set<OWLLogicalAxiom> getQuery(Collection<Set<OWLLogicalAxiom>> diagnoses){
        List<FormulaSet> sortedDiags = new LinkedList<FormulaSet>((Collection<? extends FormulaSet>) diagnoses);
        Collections.sort(sortedDiags, new Comparator<FormulaSet>() {
                    public int compare(FormulaSet d1, FormulaSet d2) {
                        return d1.getMeasure().compareTo(d2.getMeasure());
                    }

                });
        FormulaSet query = computeQuery(sortedDiags);
        return query;
    }

    private FormulaSet computeQuery(List<FormulaSet> diagnoses){

        List<FormulaSet> queryDiags = new LinkedList<FormulaSet>();
        int index = 0;
        int numAxioms = 0;
        while(index < diagnoses.size() && numAxioms+diagnoses.get(index).size() <= maxAxiomsPerQuery){
            queryDiags.add(diagnoses.get(index));
            numAxioms += diagnoses.get(index).size();
        }
        if(queryDiags.isEmpty()){
            queryDiags.add(getMinCardinalityDiag(diagnoses));
        }
        BigDecimal prob = BigDecimal.ZERO;
        FormulaSet query = queryDiags.get(0);
        for(FormulaSet diag : queryDiags){
            query.addAll(diag);
        }
        return query;
    }

    private FormulaSet getMinCardinalityDiag(List<FormulaSet> diagnoses){
        FormulaSet minCardDiag = diagnoses.get(0);
        for(FormulaSet d : diagnoses){
            if(d.size() < minCardDiag.size()){
                minCardDiag = d;
            }
        }
        return minCardDiag;
    }*/
}
