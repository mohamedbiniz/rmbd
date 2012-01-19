/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.Searcher;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.Storage;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.apache.log4j.Logger;

import java.util.*;

import static _dev.TimeLog.start;
import static _dev.TimeLog.stop;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 04.08.2009
 * Time: 08:04:41
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTreeSearch<T extends HittingSet<Id>, E extends Set<Id>, Id> implements TreeSearch<T, E, Id> {

    private int maxHittingSets = Integer.MAX_VALUE;

    private static Logger logger = Logger.getLogger(AbstractTreeSearch.class.getName());

    private static Logger loggerDual = Logger.getLogger("dualtreelogger");

    private final Storage<T, E, Id> storage;

    private Node<Id> root = null;

    // ICONFLICTSEARCHER: is the search algorithm for conflicts (e.g. QuickXplain)
    private Searcher<Id> searcher;
    private int prunedHS;

    public AbstractTreeSearch(Storage<T, E, Id> storage) {
        this.storage = storage;
    }

    abstract public Node<Id> getNode();

    protected abstract Collection<Node<Id>> getOpenNodes();

    public abstract Node<Id> popOpenNodes();

    public abstract void pushOpenNodes(Node<Id> node);

    abstract public void addNodes(ArrayList<Node<Id>> nodeList);

    public abstract void expand(Node<Id> node);

    protected abstract E createConflictSet(Set<Id> quickConflict);

    protected abstract T createHittingSet(Node<Id> labels, boolean valid) throws SolverException;

    public Storage<T, E, Id> getStorage() {
        return this.storage;
    }

    public void setSearcher(Searcher<Id> searcher) {
        this.searcher = searcher;
    }

    public Searcher<Id> getSearcher() {
        return searcher;
    }

    // setter and getter:
    private ITheory<Id> theory = null;

    public void setTheory(ITheory<Id> theory) {
        this.theory = theory;
    }

    public ITheory<Id> getTheory() {
        return theory;
    }

    public Set<T> run() throws
            SolverException, NoConflictException, InconsistentTheoryException {
        clearSearch();
        if (getMaxHittingSets() <= 0)
            return run(-1);
        else
            return run(getMaxHittingSets());
    }

    public void clearSearch() {
        getStorage().resetStorage();
        getOpenNodes().clear();
        this.root = null;
    }

    public Set<T> run(int numberOfHittingSets) throws SolverException, NoConflictException, InconsistentTheoryException {

        start("Overall run");
        start("Diagnosis", "diagnosis");
        try {
            theory.registerTestCases();
            // verify if background theory is consistent
            if (!theory.isConsistent())
                throw new SolverException("Inconsistent background theory!");

            if (logger.isInfoEnabled())
                logger.info("run started");

            if (getRoot() != null) {
                // verify hitting sets and remove invalid
                List<T> invalidHittingSets = new LinkedList<T>();
                for (T hs : getStorage().getValidHittingSets()) {
                    if (!theory.testDiagnosis(hs)) {
                        invalidHittingSets.add(hs);
                    }
                }
                for (T invHS : invalidHittingSets) {
                    getStorage().invalidateHittingSet(invHS);
                }
            } else
                createRoot();

            if (numberOfHittingSets == getStorage().getValidHittingSets().size()) {
                return getStorage().getValidHittingSets();
            }

            setMaxHittingSets(numberOfHittingSets);
            processOpenNodes();

        } finally {
            theory.unregisterTestCases();
            finalizeSearch();
            stop("diagnosis");
            stop();
        }

        return getStorage().getValidHittingSets();
    }

    protected abstract void finalizeSearch();

    private void processOpenNodes() throws SolverException, NoConflictException, InconsistentTheoryException {
        if (getRoot() == null)
            throw new IllegalArgumentException("The tree is not initialized!");
        if (openNodesIsEmpty())
            throw new NoConflictException("There are no open nodes!");
        // while List of openNodes is not empty

        while (!openNodesIsEmpty() && (maxHittingSets <= 0 || (getStorage().getHittingSetsCount() < getMaxHittingSets()))) {
            Node<Id> node = getNode();
            loggerDual.info("now processing node with uplink : " + node.getArcLabel());
            processNode(node);
        }
        if (logger.isInfoEnabled())
            logger.info("Finished search with " + getSizeOpenNodes() + " open nodes. Pruned " + this.prunedHS + " diagnoses on the last iteration.");
    }

    protected void processNode(Node<Id> node) throws SolverException, InconsistentTheoryException {
        boolean prune = pruneHittingSet(node);

        loggerDual.info("arc: " + node.getArcLabel());
        if (!prune) {
            try {
                if (!canReuseConflict(node))
                    calculateConflict(node);
                if (!node.isClosed())
                    expand(node);
            } catch (NoConflictException e) {
                // if(!getSearcher().isDual()) {
                    node.setClosed();
                    stop("diagnosis");
                    if (logger.isInfoEnabled())
                        logger.info("Closing node. " + getSizeOpenNodes() + " more to process.");

                    Set<Id> diagnosis = node.getPathLabels();

                    boolean valid = true;
                    if (getTheory().hasTests())
                        valid = getTheory().testDiagnosis(diagnosis);
                    T hs = createHittingSet(node, valid);
                    loggerDual.info("created hitting set: " + hs);
                    for (Id axiom : hs)
                        loggerDual.info("axiom: " + axiom);
                    hs.setValid(valid);
                    getStorage().addHittingSet(hs);
                    start("Diagnosis", "diagnosis");
                    if (logger.isInfoEnabled()) {
                        logger.info("Found conflicts: " + getStorage().getConflictsCount() + " and diagnoses " + getStorage().getHittingSetsCount());
                        logger.info("Pruned " + this.prunedHS + " diagnoses");
                        this.prunedHS = 0;
                    }
                /*}
                else {
                    E conflictSet = createConflictSet(node.getPathLabels());
                    getStorage().addConflict(conflictSet);
                    // verify if there is a conflict that is a subset of the new conflict
                    Set<E> invalidConflicts = new LinkedHashSet<E>();
                    for (E cs : getStorage().getConflictSets()) {
                        if (cs.containsAll(conflictSet) && cs.size() > conflictSet.size())
                            invalidConflicts.add(cs);
                    }

                    if (!invalidConflicts.isEmpty()) {
                        for (E invalidConflict : invalidConflicts) {
                            loggerDual.info("now conflict invalid: " + invalidConflict);
                            getStorage().removeConflictSet(invalidConflict);
                        }
                        updateTree(conflictSet);
                    }

                }*/
            }
        } else
            this.prunedHS++;
    }


    public void createRoot() throws NoConflictException,
            SolverException, InconsistentTheoryException {
        // if there is already a root
        if (getRoot() != null) return;
        Set<Id> conflict = calculateConflict(null);
        Node<Id> node = new Node<Id>(conflict);
        setRoot(node);
    }

    public Set<Id> calculateConflict(Node<Id> node) throws
            SolverException, NoConflictException, InconsistentTheoryException {
        // if conflict was already calculated
        Set<Id> quickConflict;
        Collection<Id> list = new ArrayList<Id>(getTheory().getActiveFormulas());
        Collections.sort((List<? extends Comparable>)list);
        Set<Id> pathLabels = null;
        if (node != null) {
            if (logger.isDebugEnabled())
                logger.debug("Calculating a conflict for the node: " + node);
            if (node.getConflict() != null) {
                if (logger.isDebugEnabled())
                    logger.debug("The conflict is already calculated: " + node.getConflict());
                return node.getConflict();
            }
            pathLabels = node.getPathLabels();
        }

        quickConflict = getSearcher().search(getTheory(), list, pathLabels);

        //if(!searcher.isDual()) {
            if (logger.isInfoEnabled())
                logger.info("Found conflict: " + quickConflict);

            E conflictSet = createConflictSet(quickConflict);
            loggerDual.info("created conflict set: " + conflictSet);
            getStorage().addConflict(conflictSet);
            // verify if there is a conflict that is a subset of the new conflict
            Set<E> invalidConflicts = new LinkedHashSet<E>();
            for (E e : getStorage().getConflictSets()) {
                if (e.containsAll(conflictSet) && e.size() > conflictSet.size())
                    invalidConflicts.add(e);
            }

            if (!invalidConflicts.isEmpty()) {
                for (E invalidConflict : invalidConflicts) {
                    loggerDual.info("now conflict invalid: " + invalidConflict);
                    getStorage().removeConflictSet(invalidConflict);
                }
                updateTree(conflictSet);
            }
            // current node should ge a conflict only if a path from
            // this node to root does not include closed nodes
            if (node != null && !hasClosedParent(node.getParent()))
                node.setConflict(quickConflict);
            return quickConflict;
        /*}
        else {
                Set<Id> diagnosis = quickConflict;
                boolean valid = true;
                if (getTheory().hasTests())
                    valid = getTheory().testDiagnosis(diagnosis);
                T hs = createHittingSet(diagnosis, valid);
                hs.setValid(valid);
                getStorage().addHittingSet(hs);
                if (node != null && !hasClosedParent(node.getParent()))
                node.setConflict(quickConflict);

            return hs;

        }*/
    }

    protected boolean hasClosedParent(Node<Id> node) {
        if (node.isRoot())
            return node.isClosed();
        return node.isClosed() || hasClosedParent(node.getParent());
    }

    private void updateTree(E conflictSet) {
        Node<Id> root = getRoot();
        updateNode(conflictSet, root);
        LinkedList<Node<Id>> children = new LinkedList<Node<Id>>(root.getChildren());
        while (!children.isEmpty()) {
            Node<Id> node = children.removeFirst();
            updateNode(conflictSet, node);
            children.addAll(node.getChildren());
        }
    }

    private void updateNode(E conflict, Node<Id> node) {
        if (node == null || node.getConflict() == null)
            return;
        if (node.getConflict().containsAll(conflict)) {
            for (Iterator<Node<Id>> onodeit = getOpenNodes().iterator(); onodeit.hasNext(); ) {
                Node<Id> openNode = onodeit.next();
                if (!openNode.isRoot() && hasParent(node, openNode.getParent()))
                    onodeit.remove();
            }
            node.setConflict(conflict);
            expand(node);
        }
    }

    private boolean hasParent(Node<Id> node, Node<Id> parent) {
        if (parent.equals(node))
            return true;
        else if (parent.isRoot())
            return false;
        return hasParent(node, parent.getParent());
    }

    public boolean canReuseConflict(Node<Id> node) {
        // check if this is a root
        if (node.isRoot() || node.getConflict() != null) return false;
        Collection<Id> pathLabels = node.getPathLabels();
        for (Set<Id> localConflict : getStorage().getConflictSets()) {
            if (!intersectsWith(pathLabels, localConflict)) {
                node.setConflict(localConflict);
                if (logger.isDebugEnabled())
                    logger.debug("Reusing conflict: " + localConflict);
                return true;
            }
        }
        return false;
    }

    private boolean intersectsWith(Collection<Id> pathLabels, Collection<Id> localConflict) {
        for (Id label : pathLabels) {
            //if (localConflict.contains(label))
            //    return true;
            for (Id axiom : localConflict) {
                if (axiom.equals(label))
                    return true;
            }
        }
        return false;
    }


    public boolean pruneHittingSet(Node<Id> node) {
        if (node.isRoot()) return false;
        Collection<T> diagnoses = null;
        Collection<Id> pathLabels = node.getPathLabels();
        for (T diagnosis : getStorage().getHittingSets()) {
            Collection<Id> larger = (diagnosis.size() > pathLabels.size()) ? diagnosis : pathLabels;
            Collection<Id> smaller = (diagnosis.size() <= pathLabels.size()) ? diagnosis : pathLabels;
            if (larger.containsAll(smaller)) {
                if (diagnosis.size() > pathLabels.size()) {
                    if (diagnoses == null) diagnoses = new LinkedList<T>();
                    diagnoses.add(diagnosis);
                } else if (diagnoses == null) return true;
            }
        }
        if (diagnoses != null)
            for (T hs : diagnoses) {
                getStorage().removeHittingSet(hs);

            }
        return false;
    }

    public void setMaxHittingSets(int maxDiagnoses) {
        this.maxHittingSets = maxDiagnoses;
    }

    // operations for openNodes:

    public int getMaxHittingSets() {
        return this.maxHittingSets;
    }

    public void setRoot(Node<Id> rootNode) {
        root = rootNode;
        clearOpenNodes();
        pushOpenNodes(root);
    }

    public Node<Id> getRoot() {
        return root;
    }

    public boolean openNodesIsEmpty() {
        return getOpenNodes().isEmpty();
    }


    public void addLastOpenNodes(Node<Id> node) {
        getOpenNodes().add(node);
    }

    public int getSizeOpenNodes() {
        return getOpenNodes().size();
    }

    public void clearOpenNodes() {
        getOpenNodes().clear();
    }
}
