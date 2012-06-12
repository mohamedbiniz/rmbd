package at.ainf.diagnosis.tree.searchstrategy;

import at.ainf.diagnosis.tree.Node;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.06.12
 * Time: 16:00
 * To change this template use File | Settings | File Templates.
 */
public class DepthFirstSearchStrategy<Id> extends UninformedSearchStrategy<Id> {

    public void expand(Node<Id> node) {
        addNodes(node.expandNode());
    }

    public Node<Id> getNode() {
        // gets the first open node of the List
        return popOpenNodes();
    }

    public void addNodes(List<Node<Id>> nodeList) {
        // adds the new open nodes in reverse order at the beginning of the List
        for (int i = nodeList.size() - 1; i >= 0; i--) {
            pushOpenNode(nodeList.get(i));
        }
    }

}
