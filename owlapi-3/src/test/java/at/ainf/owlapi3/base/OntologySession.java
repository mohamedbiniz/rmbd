package at.ainf.owlapi3.base;

import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.owlapi3.base.distribution.ExtremeDistribution;
import at.ainf.owlapi3.base.distribution.ModerateDistribution;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.performance.OntologyTests;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 23.08.12
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */
public class OntologySession extends SimulatedSession {

    private Logger logger = LoggerFactory.getLogger(OntologySession.class.getName());

    private Random rnd = new Random();

    public Random getRandom() {
        return rnd;
    }

    public FormulaSet<OWLLogicalAxiom> chooseTargetDiagnosis
            (OntologyTests.DiagProbab
                     diagProbab, TreeSet<FormulaSet<OWLLogicalAxiom>> diagnoses) {


        BigDecimal sum = new BigDecimal("0");
        TreeSet<FormulaSet<OWLLogicalAxiom>> res;
        TreeSet<FormulaSet<OWLLogicalAxiom>> good = new TreeSet<FormulaSet<OWLLogicalAxiom>>();
        TreeSet<FormulaSet<OWLLogicalAxiom>> avg = new TreeSet<FormulaSet<OWLLogicalAxiom>>();
        TreeSet<FormulaSet<OWLLogicalAxiom>> bad = new TreeSet<FormulaSet<OWLLogicalAxiom>>();

        for (FormulaSet<OWLLogicalAxiom> hs : diagnoses.descendingSet()) {
            if (sum.compareTo(BigDecimal.valueOf(0.33)) <= 0) {
                good.add(hs);
            } else if (sum.compareTo(BigDecimal.valueOf(0.33)) >= 0 && sum.compareTo(BigDecimal.valueOf(0.66)) <= 0) {
                avg.add(hs);
            } else if (sum.compareTo(BigDecimal.valueOf(0.66)) >= 0) {
                bad.add(hs);
            }
            sum = sum.add(hs.getMeasure());
        }
        switch (diagProbab) {
            case GOOD:
                while (good.size() < 3) {
                    if (!avg.isEmpty()) {
                        good.add(avg.pollLast());
                    } else if (!bad.isEmpty())
                        good.add(bad.pollLast());
                    else
                        break;
                }
                res = good;
                break;
            case AVERAGE:
                if (avg.size() < 3 && !good.isEmpty())
                    avg.add(good.pollFirst());
                while (avg.size() < 3) {
                    if (!bad.isEmpty())
                        avg.add(bad.pollLast());
                    else break;
                }
                res = avg;
                break;
            default: {
                if (bad.size() < 3)
                    logger.error("No diagnoses in bad! " + diagnoses);
                while (bad.size() < 3) {
                    if (!avg.isEmpty()) {
                        bad.add(avg.pollFirst());
                    } else if (!good.isEmpty())
                        bad.add(good.pollFirst());
                    else
                        break;
                }
                res = bad;
            }
        }

        int n = res.size() + (int) (System.currentTimeMillis() % 10000);
        int number = rnd.nextInt(n);


        int i = 1;
        FormulaSet<OWLLogicalAxiom> next = null;
        for (Iterator<FormulaSet<OWLLogicalAxiom>> it = res.descendingIterator(); it.hasNext(); i++) {
            next = it.next();
            if (i == number)
                break;
        }
        logger.info(diagProbab + ": selected target diagnosis " + next + " positioned " + number + " out of " + res.size());
        return next;
    }

    private Set<FormulaSet<OWLLogicalAxiom>> sortDiagnoses(Set<FormulaSet<OWLLogicalAxiom>> formulaSets) {
        TreeSet<FormulaSet<OWLLogicalAxiom>> phs = new TreeSet<FormulaSet<OWLLogicalAxiom>>();
        for (FormulaSet<OWLLogicalAxiom> hs : formulaSets)
            phs.add(hs);
        return Collections.unmodifiableSet(phs);
    }

    private void shuffleKeyword(ArrayList<ManchesterOWLSyntax> keywordList) {
        ArrayList<ManchesterOWLSyntax> cp = new ArrayList<ManchesterOWLSyntax>(keywordList.size());
        cp.addAll(keywordList);
        keywordList.clear();
        for (int i = 0; cp.size() > 0; i++) {
            int j = rnd.nextInt(cp.size());
            keywordList.add(i, cp.remove(j));
        }
        keywordList.addAll(cp);
    }

    protected Set<FormulaSet<OWLLogicalAxiom>> chooseUserProbab
            (OntologyTests.UsersProbab
                     usersProbab, TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, Set<FormulaSet<OWLLogicalAxiom>> diagnoses, ExtremeDistribution extremeDistribution, ModerateDistribution moderateDistribution) {
        Map<ManchesterOWLSyntax, BigDecimal> keywordProbs = new HashMap<ManchesterOWLSyntax, BigDecimal>();
        //ProbabilityTableModel m = new ProbabilityTableModel();
        ArrayList<ManchesterOWLSyntax> keywordList = new ArrayList<ManchesterOWLSyntax>(EnumSet.copyOf(getProbabMap().keySet()));
        ManchesterOWLSyntax[] selectedKeywords = new ManchesterOWLSyntax[]{ManchesterOWLSyntax.SOME, ManchesterOWLSyntax.ONLY,
                ManchesterOWLSyntax.DISJOINT_CLASSES, ManchesterOWLSyntax.DISJOINT_WITH, ManchesterOWLSyntax.SUBCLASS_OF,
                ManchesterOWLSyntax.EQUIVALENT_CLASSES, ManchesterOWLSyntax.NOT, ManchesterOWLSyntax.AND};

        /*
        keywordList = new ArrayList<ManchesterOWLSyntax>(Arrays.asList(selectedKeywords));
        shuffleKeyword(keywordList);
        */
        //Set<Integer> highKeywordPos = new HashSet<Integer>();

        List<ManchesterOWLSyntax> c = new ArrayList<ManchesterOWLSyntax>(Arrays.asList(selectedKeywords));
        for (int i = 0; i < c.size() / 2; i++) {
            int j = rnd.nextInt(c.size());
            keywordList.remove(c.get(j));
        }
        c.removeAll(keywordList);
        shuffleKeyword(keywordList);
        for (int i = 0; c.size() > 0; i++) {
            int j = rnd.nextInt(c.size());
            keywordList.add(i, c.remove(j));
        }
        //keywordList.addAll(c);

        int n = keywordList.size();
        int k = n / 4;
        double[] probabilities;

        switch (usersProbab) {
            case EXTREME:
                probabilities = extremeDistribution.getProbabilities(n);
                for (ManchesterOWLSyntax keyword : keywordList) {
                    keywordProbs.put(keyword, BigDecimal.valueOf(probabilities[keywordList.indexOf(keyword)]));
                }
                /*highKeywordPos.add(rnd.nextInt(n));
                for(ManchesterOWLSyntax keyword : keywordList) {
                    if (highKeywordPos.contains(keywordList.indexOf(keyword)))
                        keywordProbs.put(keyword,getProbabBetween(LOWER_BOUND_EXTREME_HIGH,HIGHER_BOUND_EXTREME_HIGH));
                    else
                        keywordProbs.put(keyword, getProbabBetween(LOWER_BOUND_EXTREME_LOW,HIGHER_BOUND_EXTREME_LOW));
                }*/
                break;
            case MODERATE:
                probabilities = moderateDistribution.getProbabilities(n);
                for (ManchesterOWLSyntax keyword : keywordList) {
                    keywordProbs.put(keyword, BigDecimal.valueOf(probabilities[keywordList.indexOf(keyword)]));
                }
                /*for (int i = 0; i < k; i++) {
                    int num = rnd.nextInt(n);
                    while (!highKeywordPos.add(num))
                        num = rnd.nextInt();
                }
                for(ManchesterOWLSyntax keyword : keywordList) {
                    if (highKeywordPos.contains(keywordList.indexOf(keyword)))
                        keywordProbs.put(keyword,getProbabBetween(LOWER_BOUND_MODERATE_HIGH,HIGHER_BOUND_MODERATE_HIGH));
                    else
                        keywordProbs.put(keyword, getProbabBetween(LOWER_BOUND_MODERATE_LOW,HIGHER_BOUND_MODERATE_LOW));
                }*/
                break;
            case UNIFORM:
                for (ManchesterOWLSyntax keyword : keywordList) {
                    keywordProbs.put(keyword, BigDecimal.valueOf(1.0 / n));
                }
                break;
        }
        ((OWLAxiomKeywordCostsEstimator)search.getCostsEstimator()).setKeywordProbabilities(keywordProbs, diagnoses);
        return sortDiagnoses(diagnoses);


    }

    protected FormulaSet<OWLLogicalAxiom> getMostProbable(Set<FormulaSet<OWLLogicalAxiom>> diagnoses) {
        TreeSet<FormulaSet<OWLLogicalAxiom>> ts = new TreeSet<FormulaSet<OWLLogicalAxiom>>();
        ts.addAll(diagnoses);
        return ts.last();
    }
}
