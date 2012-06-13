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
    protected Set<AxiomSet<Id>> hittingSets = new LinkedHashSet<AxiomSet<Id>>();
    protected Set<AxiomSet<Id>> validHittingSets = new LinkedHashSet<AxiomSet<Id>>();
    protected Set<AxiomSet<Id>> conflicts = new LinkedHashSet<AxiomSet<Id>>();

    private StorageListener<AxiomSet<Id>, Id> hittingSetListener = new StorageListener<AxiomSet<Id>, Id>() {
        public boolean remove(AxiomSet<Id> oldObject) {
            boolean remValid = validHittingSets.remove(oldObject);
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
            if (addValid)
                validHittingSets.add(newObject);
            hittingSets.add(newObject);
        }
    };

    public void resetStorage() {
        for (AxiomSet<Id> hs : this.getHittingSets())
            hs.setListener(null);
        hittingSets.clear();
        conflicts.clear();
        validHittingSets.clear();
    }

    public boolean addConflict(AxiomSet<Id> conflict) {
        if (logger.isInfoEnabled())
            logger.info("Adding a conflict: " + conflict);
        return conflicts.add(conflict);
    }

    public Set<AxiomSet<Id>> getConflictSets() {
        return Collections.unmodifiableSet(copy(conflicts));
    }

    public boolean removeConflictSet(AxiomSet<Id> cs) {
        return this.conflicts.remove(cs);
    }

    public Set<AxiomSet<Id>> getConflicts() {
        return getConflictSets();
    }

    public Set<AxiomSet<Id>> getDiagnoses() {
        return Collections.unmodifiableSet(getValidHittingSets());
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

        boolean val = hittingSets.add(hittingSet);

        if (hittingSet.isValid()) {
            validHittingSets.add(hittingSet);
        }

        return val;
    }

    public boolean removeHittingSet(final AxiomSet<Id> diagnosis) {
        boolean val = hittingSets.remove(diagnosis);
        if (diagnosis.isValid())
            validHittingSets.remove(diagnosis);

        return val;
    }

    public void invalidateHittingSet(AxiomSet<Id> hs) {
        hs.setValid(false);
        this.validHittingSets.remove(hs);
    }

    public Set<AxiomSet<Id>> getValidHittingSets() {
        return copy(validHittingSets);
    }

    private Set<AxiomSet<Id>> copy(Set<AxiomSet<Id>> set) {
        Set<AxiomSet<Id>> hs = new LinkedHashSet<AxiomSet<Id>>();
        for (AxiomSet<Id> hset : set)
            hs.add(hset);
        return hs;
    }

    public Set<AxiomSet<Id>> getHittingSets() {
        return Collections.unmodifiableSet(hittingSets);
    }

    public Set<AxiomSet<Id>> getConflictSets(Id axiom) {
        Set<AxiomSet<Id>> conflicts = new LinkedHashSet<AxiomSet<Id>>();

        for (AxiomSet<Id> conflict : getConflictSets()) {
            for (Id ax : conflict) {
                if (ax.equals(axiom)) {
                    conflicts.add(conflict);
                    break;
                }
            }
        }
        return conflicts;
    }




}
