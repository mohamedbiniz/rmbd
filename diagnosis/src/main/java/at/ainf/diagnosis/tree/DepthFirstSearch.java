/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;

import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Storage;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 04.08.2009
 * Time: 08:49:39
 * To change this template use File | Settings | File Templates.
 */
public class DepthFirstSearch<Id> extends UninformedSearch<Id> {

    public DepthFirstSearch(Storage<AxiomSet<Id>, Id> storage) {
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
        // adds the new open nodes in reverse order at the beginning of the List
        for (int i = nodeList.size() - 1; i >= 0; i--) {
            pushOpenNodes(nodeList.get(i));
        }
    }
}
