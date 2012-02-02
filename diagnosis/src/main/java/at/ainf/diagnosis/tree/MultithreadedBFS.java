package at.ainf.diagnosis.tree;

import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Storage;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 16.06.2010
 * Time: 17:08:26
 * To change this template use File | Settings | File Templates.
 */
public class MultithreadedBFS<Id> extends UninformedSearch<Id> {

    public MultithreadedBFS(Storage<AxiomSet<Id>, Id> storage) {
        super(storage);
    }

    public void expand(Node<Id> node) {
        addNodes(node.expandNode());
    }

    public Node<Id> getNode() {
        // gets the first open node of the List
        return popOpenNodes();
    }

    public void addNodes(ArrayList<Node<Id>> nodeList) {
        // adds the new open nodes at the end of the List
        for (Node<Id> node : nodeList) addLastOpenNodes(node);
    }

}
