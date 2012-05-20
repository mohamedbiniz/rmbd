package at.ainf.owlcontroller.queryeval;

import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.theory.storage.AxiomSet;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.05.12
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class TimeoutTask extends TimerTask {

    public static int CYCLE_TIME = 5*1000;

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

    @Override
    public void run() {
        TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> s = getSearch();

        if(s==null)
            logger.info("Statistics: " + matcher + "," + o + " search is null");
        else {
            int numD = s.getStorage().getDiagnoses().size();
            int numC = s.getStorage().getDiagnoses().size();
            int openNodes = s.getOpenNodes().size();

            logger.info("Statistics: " + matcher + "," + o + "," + numD + "," + numC + "," + openNodes);
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
