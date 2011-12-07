package at.ainf.pluginprotege.queryeval;

import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 17.05.11
 * Time: 09:33
 * To change this template use File | Settings | File Templates.
 */
public class SimulResult implements Comparable<SimulResult> {
    String ontology;

    UsersProbab usersProbab;

    DiagProbab diagProbab;

    int run;

    QuerySelStrat strategy;

    int querynum;

    public SimulResult(String ontology, UsersProbab usersProbab, DiagProbab diagProbab, int run, QuerySelStrat strategy, int querynum) {
        this.ontology = ontology;
        this.usersProbab = usersProbab;
        this.diagProbab = diagProbab;
        this.run = run;
        this.strategy = strategy;
        this.querynum = querynum;
    }

    public String toString() {
        return ontology + " " + usersProbab + " " + diagProbab + " " + run + " " + strategy + " " + querynum;
    }

    public int compareTo(SimulResult o) {
        if (querynum < o.querynum) {
            return -1;
        }
        else if (querynum > o.querynum) {
            return 1;
        }
        else
            return 0;

    }
}
