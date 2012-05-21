package at.ainf.owlcontroller.queryeval;

import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.theory.storage.AxiomSet;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.05.12
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class TimeoutTask extends TimerTask {

    public static int CYCLE_TIME = 60000;

    private String matcher = "";
    private String o = "";

    private static Logger logger = Logger.getLogger(TimeoutTask.class.getName());

    int maxCycles;
    int cycles = 0;
    private TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search;

    public TimeoutTask(int maxTime, String matcher, String ontology) {
        this.maxCycles = maxTime/CYCLE_TIME;
        this.matcher = matcher;
        this.o = ontology;
    }

    private Set<Integer> getCardinalities(Set<AxiomSet<OWLLogicalAxiom>> set) {
        Set<Integer> r = new LinkedHashSet<Integer>();
        for (Set<OWLLogicalAxiom> s : set)
            r.add(s.size());
        return r;
    }

    private Integer getMin(Set<Integer> set) {
        try {
            return Collections.min(set);

        }
        catch(NoSuchElementException e) {
            return -1;
        }
    }

    private Double getMean(Set<Integer> set) {
        if (set.isEmpty())
            return -1.0;

        Double r = 0.0;
        for (Integer a : set)
            r +=  a;
        return r / set.size();
    }

    private Integer getMax(Set<Integer> set) {
        try {
            return Collections.max(set);

        }
        catch(NoSuchElementException e) {
            return -1;
        }
    }

    @Override
    public void run() {
        TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> s = getSearch();

        if(s==null)
            logger.info("Statistics: " + matcher + "," + o + " search is null");
        else {
            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = s.getStorage().getDiagnoses();
            Set<AxiomSet<OWLLogicalAxiom>> conflicts = s.getStorage().getConflicts();
            Set<Integer> conflictsCard = getCardinalities(conflicts);
            Set<Integer>  diagnosesCard = getCardinalities(diagnoses);

            int numD =diagnoses.size();
            int minDcard = getMin(diagnosesCard);
            double meanDcard = getMean(diagnosesCard);
            int maxDcard = getMax(diagnosesCard);
            int numC = conflicts.size();
            int minCcard = getMin(conflictsCard);
            double meanCcard = getMean(conflictsCard);
            int maxCcard = getMax(conflictsCard);
            int openNodes = s.getOpenNodes().size();

            logger.info("Statistics: " + matcher + "," + o + ","
                    + numD + "," + minDcard + "," + meanDcard + "," + maxDcard + ","
                    + numC + "," + minCcard + "," + meanCcard + "," + maxCcard + ","
                    + openNodes );
        }

        if (cycles > maxCycles) {
            logger.info("Timeout: " + matcher + "," + o);
            System.exit(0);
        }
        else {
            cycles++;
        }
    }


    public synchronized void setSearch(TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> s) {
        this.search = s;
    }

    public synchronized TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch() {
        return search;
    }

}
