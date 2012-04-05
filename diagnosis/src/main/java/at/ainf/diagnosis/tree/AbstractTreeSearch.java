/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomRenderer;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.AxiomSetFactory;
import at.ainf.theory.storage.Storage;
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
public abstract class AbstractTreeSearch<T extends AxiomSet<Id>, Id> implements TreeSearch<T, Id> {

    private int maxHittingSets = Integer.MAX_VALUE;

    private static Logger logger = Logger.getLogger(AbstractTreeSearch.class.getName());

    private static Logger loggerDual = Logger.getLogger("dualtreelogger");

    private final Storage<T, Id> storage;

    private Node<Id> root = null;

    // ICONFLICTSEARCHER: is the search algorithm for conflicts (e.g. QuickXplain)
    private Searcher<Id> searcher;
    private int prunedHS;

    private AxiomRenderer<Id> axiomRenderer;

    private TreeLogic<T,Id> treeLogic;

    public AbstractTreeSearch(Storage<T, Id> storage) {
        this.storage = storage;
    }

    public void setLogic(TreeLogic<T,Id> treeLog) {
        this.treeLogic = treeLog;
        treeLogic.setTreeSearch(this);
    }

    private CostsEstimator<Id> costsEstimator;

    public CostsEstimator<Id> getCostsEstimator() {
        return costsEstimator;
    }

    public void setCostsEstimator(CostsEstimator<Id> costsEstimator) {
        this.costsEstimator = costsEstimator;
    }

    abstract public Node<Id> getNode();

    public abstract Collection<Node<Id>> getOpenNodes();

    public abstract Node<Id> popOpenNodes();

    public abstract void pushOpenNodes(Node<Id> node);

    abstract public void addNodes(ArrayList<Node<Id>> nodeList);

    public abstract void expand(Node<Id> node);

    protected abstract T createConflictSet(Node<Id> node, Set<Id> quickConflict) throws SolverException;

    protected abstract T createHittingSet(Node<Id> labels, boolean valid) throws SolverException;

    protected List<OpenNodesListener> oNodesLsteners = new LinkedList<OpenNodesListener>();

    public void addOpenNodesListener(OpenNodesListener l) {
        oNodesLsteners.add(l);
    }

    public void removeOpenNodesListener(OpenNodesListener l) {
        oNodesLsteners.remove(l);
    }

    public Storage<T, Id> getStorage() {
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

    public Set<T> continueSearch() throws
            SolverException, NoConflictException, InconsistentTheoryException {
        return run(-1);
    }

    public void clearSearch() {
        getStorage().resetStorage();
        getOpenNodes().clear();
        this.root = null;
    }

    public int getNumOfInvalidatedHS() {
        return numOfInvalidatedHS;
    }

    protected int numOfInvalidatedHS;

    protected void updateHsTree(List<T> invalidHittingSets) 
            throws SolverException, InconsistentTheoryException, NoConflictException {
        if (!getSearcher().isDual()) {
            for (T ax : storage.getConflictSets()) {
                Set<Id> axioms = getSearcher().search(theory, ax, null);
                if (!axioms.equals(ax)) {
                    AxiomSet<Id> conflict = AxiomSetFactory.createConflictSet(ax.getMeasure(), axioms, ax.getEntailments());
                    updateTree(conflict);
                    ax.updateAxioms(conflict);
                }

            }
        } else {
            for (T next : invalidHittingSets) {
                updateTree(next);
                getStorage().removeConflictSet(next);
            }
            getTheory().registerTestCases();
            //  if (getRoot() != null && getOpenNodes().isEmpty())
            //      expandLeafNodes(getRoot());
        }
    }
    
    public Set<T> run(int numberOfHittingSets) throws SolverException, NoConflictException, InconsistentTheoryException {

        start("Overall runPostprocessor");
        start("Diagnosis", "diagnosis");
        try {
            theory.registerTestCases();
            // verify if background theory is consistent
            if (!theory.verifyRequirements())
                throw new SolverException("the background theory doesn't meet requirements");

            if (logger.isInfoEnabled())
                logger.info("runPostprocessor started");

            if (getRoot() != null) {
                // verify hitting sets and remove invalid
                List<T> invalidHittingSets = new LinkedList<T>();
                for (T hs : getStorage().getDiagnoses()) {
                    if (!theory.testDiagnosis(hs)) {
                        invalidHittingSets.add(hs);
                    }
                }
                numOfInvalidatedHS = invalidHittingSets.size();
                for (T invHS : invalidHittingSets) {
                    getStorage().invalidateHittingSet(invHS);
                }
                treeLogic.updateTree(invalidHittingSets);
            }
            if (getRoot() == null) {
                createRoot();
            }


            if (numberOfHittingSets == getStorage().getDiagnoses().size()) {
                return getStorage().getDiagnoses();
            }

            setMaxHittingSets(numberOfHittingSets);
            processOpenNodes();

        } finally {
            theory.unregisterTestCases();
            finalizeSearch();
            stop("diagnosis");
            stop();
        }

        return getStorage().getDiagnoses();
    }

    protected void expandLeafNodes(Node<Id> node) {
        if (node.getChildren().isEmpty() && !node.isClosed()) {
            ArrayList<Node<Id>> nodeList = node.expandNode();
            nodeList.removeAll(getOpenNodes());
            addNodes(nodeList);
            return;
        }
        for (Node<Id> idNode : node.getChildren()) {
            expandLeafNodes(idNode);
        }
    }

    protected abstract void finalizeSearch();

    private void processOpenNodes() throws SolverException, NoConflictException, InconsistentTheoryException {
        if (getRoot() == null)
            throw new IllegalArgumentException("The tree is not initialized!");
        if (openNodesIsEmpty())
            throw new NoConflictException("There are no open nodes!");
        // while List of openNodes is not empty

        while (!openNodesIsEmpty() && (maxHittingSets <= 0 || (getStorage().getDiagsCount() < getMaxHittingSets()))) {
            Node<Id> node = getNode();
            if (axiomRenderer != null)
                logMessage(getDepth(node), " now processing node with uplink : ", node.getArcLabel());
            processNode(node);
        }
        if (logger.isInfoEnabled())
            logger.info("Finished search with " + getSizeOpenNodes() + " open nodes. Pruned " + this.prunedHS + " diagnoses on the last iteration.");
    }

    private void logMessage(int depth, String message, Set<Id> axioms) {
        String prefix = "";
        if (depth > 0)
            for (int i = 0; i < depth; i++)
                prefix += "    ";

        loggerDual.info(prefix + "o " + message + axiomRenderer.renderAxioms(axioms));
    }

    private void logMessage(int depth, String message, Id axioms) {
        String prefix = "";
        if (depth > 0)
            for (int i = 0; i < depth; i++)
                prefix += "    ";

        loggerDual.info(prefix + message + axiomRenderer.renderAxiom(axioms));
    }

    private int getDepth(Node<Id> node) {
        if (node == null) return -1;
        if (node.getParent() == null)
            return 0;
        else
            return getDepth(node.getParent()) + 1;
    }
    
    protected boolean proveValidnessDiagnosis(Set<Id> diagnosis) throws SolverException {
        if (!getSearcher().isDual()) {
            if (getTheory().hasTests())
                return getTheory().testDiagnosis(diagnosis);
        }
        return true;
    }

    protected void processNode(Node<Id> node) throws SolverException, InconsistentTheoryException {
        boolean prune = pruneHittingSet(node);

        // if(axiomRenderer!=null) loggerDual.info("arc: " + axiomRenderer.renderAxiom(node.getArcLabel()));
        if (!prune) {
            try {
                if (!canReuseConflict(node))
                    calculateConflict(node);
                if (!node.isClosed() && node.getAxiomSet() != null)
                    expand(node);
            } catch (NoConflictException e) {
                // if(!getSearcher().isDual()) {
                node.setClosed();
                stop("diagnosis");
                if (logger.isInfoEnabled())
                    logger.info("Closing node. " + getSizeOpenNodes() + " more to process.");

                Set<Id> diagnosis = node.getPathLabels();

                boolean valid = treeLogic.proveValidnessDiagnosis(diagnosis);
                
                T hs = createHittingSet(node, valid);

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
    
    protected void proveValidnessConflict(T conflictSet) throws SolverException {
        if (getSearcher().isDual()) {
            boolean valid = true;
            if (getTheory().hasTests()) {
//                getTheory().addBackgroundFormulas(pathLabels);
                valid = getTheory().testDiagnosis(conflictSet);
                //              getTheory().removeBackgroundFormulas(pathLabels);
            }
            conflictSet.setValid(valid);

        }
    }

    public Set<Id> calculateConflict(Node<Id> node) throws
            SolverException, NoConflictException, InconsistentTheoryException {
        // if conflict was already calculated
        Set<Id> quickConflict;
        List<Id> list = new ArrayList<Id>(getTheory().getActiveFormulas());
        Collections.sort(list, new Comparator<Id>() {
            public int compare(Id o1, Id o2) {
                double nodeCosts = getCostsEstimator().getAxiomCosts(o1);
                int value = -1 * Double.valueOf(nodeCosts).compareTo(getCostsEstimator().getAxiomCosts(o2));
                if (value == 0)
                    return ((Comparable)o1).compareTo(o2);
                return value;
            }
        });

        Set<Id> pathLabels = null;
        if (node != null) {
            if (logger.isDebugEnabled())
                logger.debug("Calculating a conflict for the node: " + node);
            if (node.getAxiomSet() != null) {
                if (logger.isDebugEnabled())
                    logger.debug("The conflict is already calculated: " + node.getAxiomSet());
                return node.getAxiomSet();
            }
            pathLabels = node.getPathLabels();
        }

        quickConflict = getSearcher().search(getTheory(), list, pathLabels);

        //if(!searcher.isDual()) {
        if (logger.isInfoEnabled())
            logger.info("Found conflict: " + quickConflict);

        T conflictSet = createConflictSet(node, quickConflict);

        treeLogic.proveValidnessConflict(conflictSet);

        if (axiomRenderer != null)
            logMessage(getDepth(node), "created conflict set: ", conflictSet);
        if (axiomRenderer != null)
            logMessage(getDepth(node), "pathlabels: ", pathLabels);


            treeLogic.pruneConflictSets(node, conflictSet);

        getStorage().addConflict(conflictSet);

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

    protected void pruneConflictSets(Node<Id> node, T conflictSet) throws SolverException, InconsistentTheoryException {
        if (!getSearcher().isDual()) {
        // DAG: verify if there is a conflict that is a subset of the new conflict
        Set<T> invalidConflicts = new LinkedHashSet<T>();
        for (T e : getStorage().getConflictSets()) {
            if (e.containsAll(conflictSet) && e.size() > conflictSet.size())
                invalidConflicts.add(e);
        }

        if (!invalidConflicts.isEmpty()) {
            for (T invalidConflict : invalidConflicts) {
                if (axiomRenderer != null) logMessage(getDepth(node), "now conflict invalid: ", invalidConflict);
                getStorage().removeConflictSet(invalidConflict);
            }
            updateTree(conflictSet);
        }
        }
    }

    protected boolean hasClosedParent(Node<Id> node) {
        if (node.isRoot())
            return node.isClosed();
        return node.isClosed() || hasClosedParent(node.getParent());
    }

    public void updateTree(AxiomSet<Id> conflictSet) throws SolverException, InconsistentTheoryException {
        Node<Id> root = getRoot();
        if (getRoot() == null) {
            return;
        }
        LinkedList<Node<Id>> children = new LinkedList<Node<Id>>();
        children.add(root);
        while (!children.isEmpty()) {
            Node<Id> node = children.removeFirst();
            Set<Node<Id>> nodeChildren = treeLogic.updateNode(conflictSet, node);
            children.addAll(nodeChildren);
        }
    }

    public boolean canReuseConflict(Node<Id> node) {
        // check if this is a root
        if (node.isRoot() || node.getAxiomSet() != null) return false;
        Collection<Id> pathLabels = node.getPathLabels();
        for (AxiomSet<Id> localConflict : getStorage().getConflictSets()) {
            if (localConflict.isValid() && !intersectsWith(pathLabels, localConflict)) {
                node.setConflict(localConflict);
                if (logger.isDebugEnabled())
                    logger.debug("Reusing conflict: " + localConflict);
                if (axiomRenderer != null) logMessage(getDepth(node), "reusing conflict ", localConflict);
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

    public void setAxiomRenderer(AxiomRenderer<Id> idAxiomRenderer) {
        this.axiomRenderer = idAxiomRenderer;

    }
}
