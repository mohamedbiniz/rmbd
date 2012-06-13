package at.ainf.diagnosis.tree.searchstrategy;

import at.ainf.diagnosis.tree.Node;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.06.12
 * Time: 16:01
 * To change this template use File | Settings | File Templates.
 */
public class DepthLimitedSearchStrategy<Id> extends DepthFirstSearchStrategy<Id> {

    private int limit;
    private boolean expandable = false;

    public DepthLimitedSearchStrategy() {
        this.limit = Integer.MAX_VALUE;
    }

    /*public Set<AxiomSet<Id>> run() throws NoConflictException, SolverException, InconsistentTheoryException {
        this.expandable = false;
        return super.run();
    }*/

    @Override
    public void expand(Node<Id> node) {
        int level = node.getLevel();
        if (level < this.limit) {
            addNodes(node.expandNode());
        } else if (level == this.limit) {
            this.expandable = true;
        }
    }

    public DepthLimitedSearchStrategy(int limit) {
        this.limit = limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isExpandable() {
        return expandable;
    }

}
