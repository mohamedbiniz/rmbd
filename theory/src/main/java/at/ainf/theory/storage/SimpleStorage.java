/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.theory.storage;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 03.08.2009
 * Time: 14:29:03
 * To change this template use File | Settings | File Templates.
 */

public class SimpleStorage<Id> implements Storage<AxiomSet<Id>,Id> {
    private static Logger logger = Logger.getLogger(SimpleStorage.class.getName());

    protected Set<AxiomSet<Id>> nodeLabels = new LinkedHashSet<AxiomSet<Id>>();
    protected Set<AxiomSet<Id>> hittingSets = new LinkedHashSet<AxiomSet<Id>>();

    private StorageListener<AxiomSet<Id>, Id> hittingSetListener = new StorageListener<AxiomSet<Id>, Id>() {
        public boolean remove(AxiomSet<Id> oldObject) {
            boolean remValid = oldObject.isValid();
            if (!hittingSets.remove(oldObject)) {
                // perhaps treeset order is not correct
                hittingSets = copy(hittingSets);
                if (hittingSets.remove(oldObject))
                    logger.error("treeset ordering is not correct - updates of probabilities? ");
                else
                    throw new IllegalStateException("Existing hitting set was not removed!");
            }

            return remValid;
        }

        public void add(AxiomSet<Id> newObject, boolean addValid) {
            hittingSets.add(newObject);
        }
    };

    public void resetStorage() {
        for (AxiomSet<Id> hs : this.getHittingSets())
            hs.setListener(null);
        hittingSets.clear();
        nodeLabels.clear();
    }


    public boolean addNodeLabel(AxiomSet<Id> nodeLabel) {
        return nodeLabels.add(nodeLabel);
    }

    public boolean removeNodeLabel(AxiomSet<Id> nodeLabel) {
        return this.nodeLabels.remove(nodeLabel);
    }

    public Set<AxiomSet<Id>> getNodeLabels() {
        return Collections.unmodifiableSet(nodeLabels);
    }

    public Set<AxiomSet<Id>> getConflicts() {
        return getValidAxiomSets(copy(nodeLabels));
    }


    public boolean addHittingSet(final AxiomSet<Id> hittingSet) {
        hittingSet.setListener(this.hittingSetListener);
        if (logger.isInfoEnabled()) {
            logger.info("Adding a hitting set: " + hittingSet);
        }

        Set<AxiomSet<Id>> del = new HashSet<AxiomSet<Id>>();
        for (AxiomSet<Id> set : hittingSets) {
            if (set.containsAll(hittingSet))
                del.add(set);
        }
        if (!del.isEmpty())
            for (AxiomSet<Id> ids : del) {
                removeHittingSet(ids);
            }




        return hittingSets.add(hittingSet);
    }

    public boolean removeHittingSet(final AxiomSet<Id> diagnosis) {
        return hittingSets.remove(diagnosis);
    }

    public Set<AxiomSet<Id>> getHittingSets() {
        return Collections.unmodifiableSet(hittingSets);
    }

    public Set<AxiomSet<Id>> getDiagnoses() {
        return getValidAxiomSets(copy(hittingSets));
    }


    public Set<AxiomSet<Id>> getValidAxiomSets(Set<AxiomSet<Id>> set) {
        Set<AxiomSet<Id>> valid = new LinkedHashSet<AxiomSet<Id>>();

        for (AxiomSet<Id> s : set) {
            if (s.isValid())
                valid.add(s);
        }
        return Collections.unmodifiableSet(valid);

    }



    protected Set<AxiomSet<Id>> copy(Set<AxiomSet<Id>> set) {
        Set<AxiomSet<Id>> hs = new LinkedHashSet<AxiomSet<Id>>();
        for (AxiomSet<Id> hset : set)
            hs.add(hset);
        return hs;
    }







}
