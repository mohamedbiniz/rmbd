package at.ainf.diagnosis.tree.searchstrategy;

import at.ainf.diagnosis.tree.Node;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.06.12
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class BreadthFirstSearchStrategy<Id> extends AbstractUninformedSearchStrategy<Id> {

    public void expand(Node<Id> node) {
        addNodes(node.expandNode());
    }

    public Node<Id> getNode() {
        // gets the first open node of the List
        return popOpenNodes();
    }

    public void addNodes(List<Node<Id>> nodeList) {
        // adds the new open nodes at the end of the List
        for (Node<Id> node : nodeList) getOpenNodes().add(node);
    }

}
