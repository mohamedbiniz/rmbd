package at.ainf.diagnosis.tree.searchstrategy;

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

    /*public Set<AxiomSet<Id>> start() throws SolverException, NoConflictException, InconsistentTheoryException {
        int iterationDepth = this.startDepth;
        do {
            setLimit(iterationDepth);
            super.start();
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
