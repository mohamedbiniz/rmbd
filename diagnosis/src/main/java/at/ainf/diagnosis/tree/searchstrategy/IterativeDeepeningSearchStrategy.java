package at.ainf.diagnosis.tree.searchstrategy;

import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Storage;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.06.12
 * Time: 16:04
 * To change this template use File | Settings | File Templates.
 */
public class IterativeDeepeningSearchStrategy<Id> extends DepthLimitedSearchStrategy<Id> {

    private final long LIMIT = Integer.MAX_VALUE;
    private int startDepth;
    private int step;

    public IterativeDeepeningSearchStrategy() {
        this.startDepth = 1;
        this.step = 1;
    }

    public IterativeDeepeningSearchStrategy(int startDepth, int step) {
        this.startDepth = startDepth;
        this.step = step;
    }

    /*public Set<AxiomSet<Id>> run() throws SolverException, NoConflictException, InconsistentTheoryException {
        int iterationDepth = this.startDepth;
        do {
            setLimit(iterationDepth);
            super.run();
            // resets List of openNodes
            clearOpenNodes();
            pushOpenNode(getRoot());
            iterationDepth += step;

        } while (iterationDepth < LIMIT && isExpandable());
        return getStorage().getDiagnoses();
    }*/

    public void setStartDepth(int startDepth) {
        this.startDepth = startDepth;
    }

    public int getStartDepth() {
        return startDepth;
    }

}
