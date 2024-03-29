package at.ainf.owlapi3.reasoner;

/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;

import java.util.*;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 01-Aug-2009
 */
public abstract class OWLExtendedReasonerBase implements OWLReasoner {

    private final OWLOntologyManager manager;
    private final OWLOntology rootOntology;
    private final BufferingMode bufferingMode;
    private final List<OWLOntologyChange> rawChanges = new ArrayList<OWLOntologyChange>();
    private final Set<OWLAxiom> reasonerAxioms;
    private final long timeOut;
    private final OWLReasonerConfiguration configuration;
    private OWLOntologyChangeListener ontologyChangeListener = new OWLOntologyChangeListener() {
        @Override
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
            handleRawOntologyChanges(changes);
        }
    };

    //private Set<OWLAxiom> added = null;
    //private Set<OWLAxiom> removed = null;

    protected OWLExtendedReasonerBase(OWLOntology rootOntology, OWLReasonerConfiguration configuration, BufferingMode bufferingMode) {
        this.rootOntology = rootOntology;
        this.bufferingMode = bufferingMode;
        this.configuration = configuration;
        timeOut = configuration.getTimeOut();
        manager = rootOntology.getOWLOntologyManager();
        manager.addOntologyChangeListener(ontologyChangeListener);
        reasonerAxioms = new HashSet<OWLAxiom>();
        for (OWLOntology ont : rootOntology.getImportsClosure()) {
            for (OWLAxiom ax : ont.getLogicalAxioms()) {
                getReasonerAxiomsSet().add(ax.getAxiomWithoutAnnotations());
            }
            for (OWLAxiom ax : ont.getAxioms(AxiomType.DECLARATION)) {
                getReasonerAxiomsSet().add(ax.getAxiomWithoutAnnotations());
            }
        }
    }

    /**
     * @return the configuration
     */
    public OWLReasonerConfiguration getReasonerConfiguration() {
        return configuration;
    }

    @Override
    public BufferingMode getBufferingMode() {
        return bufferingMode;
    }

    @Override
    public long getTimeOut() {
        return timeOut;
    }

    @Override
    public OWLOntology getRootOntology() {
        return rootOntology;
    }

    /**
     * Handles raw ontology changes. If the reasoner is a buffering reasoner
     * then the changes will be stored in a buffer. If the reasoner is a
     * non-buffering reasoner then the changes will be automatically flushed
     * through to the change filter and passed on to the reasoner.
     *
     * @param changes The list of raw changes.
     */
    protected synchronized void handleRawOntologyChanges(
            List<? extends OWLOntologyChange> changes) {
        getRawChanges().addAll(changes);
        // We auto-flush the changes if the reasoner is non-buffering
        if (bufferingMode.equals(BufferingMode.NON_BUFFERING)) {
            flush();
        }
    }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        return new ArrayList<OWLOntologyChange>(getRawChanges());
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        Set<OWLAxiom> added = new HashSet<OWLAxiom>();
        computeDiff(added, new HashSet<OWLAxiom>());
        return added;
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        Set<OWLAxiom> removed = new HashSet<OWLAxiom>();
        computeDiff(new HashSet<OWLAxiom>(), removed);
        return removed;
    }

    @Override
    public void flush() {
        // Process the changes
        Set<OWLAxiom> added = null, removed = null; //this.added;
        //Set<OWLAxiom> removed = this.removed;
      //  if (added == null && removed == null) {
            added = new HashSet<OWLAxiom>();
            removed = new HashSet<OWLAxiom>();
            computeDiff(added, removed);
      //  }
        flush(added, removed);
    }

    /*
    public void setDirectChanges(Set<OWLAxiom> added, Set<OWLAxiom> removed){
        this.added = added;
        this.removed = removed;
    }
    */

    public void flush(Set<OWLAxiom> added, Set<OWLAxiom> removed) {
        // Process the changes
        getReasonerAxiomsSet().removeAll(removed);
        getReasonerAxiomsSet().addAll(added);
        getRawChanges().clear();
        if (!added.isEmpty() || !removed.isEmpty()) {
            handleChanges(added, removed);
        }
       // this.added = null;
       // this.removed = null;
    }

    protected Set<OWLAxiom> getReasonerAxiomsSet() {
        return reasonerAxioms;
    }

    protected List<OWLOntologyChange> getRawChanges() {
        return rawChanges;
    }

    /**
     * Computes a diff of what axioms have been added and what axioms have been removed from the list
     * of pending changes.  Note that even if the list of pending changes is non-empty then there may be
     * no changes for the reasoner to deal with.
     *
     * @param added   The logical axioms that have been added to the imports closure of the reasoner root ontology
     * @param removed The logical axioms that have been removed from the imports closure of the reasoner root
     *                ontology
     */
    private void computeDiff(Set<OWLAxiom> added, Set<OWLAxiom> removed) {
        if (getRawChanges().isEmpty()) {
            return;
        }
        Set<OWLAxiom> commonAxioms = new HashSet<OWLAxiom>();
        for (OWLOntology ont : rootOntology.getImportsClosure()) {
            for (OWLAxiom ax : ont.getLogicalAxioms()) {
                if (!getReasonerAxiomsSet().contains(ax.getAxiomWithoutAnnotations())) {
                    added.add(ax);
                } else
                    commonAxioms.add(ax);
            }
            for (OWLAxiom ax : ont.getAxioms(AxiomType.DECLARATION)) {
                if (!getReasonerAxiomsSet().contains(ax.getAxiomWithoutAnnotations())) {
                    added.add(ax);
                }
                else
                    commonAxioms.add(ax);
            }
        }
        removed.addAll(Sets.difference(getReasonerAxiomsSet(), commonAxioms));

        /*
        for (OWLAxiom ax : getReasonerAxiomsSet()) {
            if (!rootOntology.containsAxiomIgnoreAnnotations(ax, true)) {
                removed.add(ax);
            }
        }
        */
    }

    /**
     * Gets the axioms that should be currently being reasoned over.
     *
     * @return A collections of axioms (not containing duplicates) that the reasoner should be taking into consideration
     *         when reasoning.  This set of axioms many not correspond to the current state of the imports closure of the
     *         reasoner root ontology if the reasoner is buffered.
     */
    public Collection<OWLAxiom> getReasonerAxioms() {
        return new ArrayList<OWLAxiom>(reasonerAxioms);
    }

    /**
     * Asks the reasoner implementation to handle axiom additions and removals from the imports closure of the root
     * ontology.  The changes will not include annotation axiom additions and removals.
     *
     * @param addAxioms    The axioms to be added to the reasoner.
     * @param removeAxioms The axioms to be removed from the reasoner
     */
    protected abstract void handleChanges(Set<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms);

    @Override
    public void dispose() {
        manager.removeOntologyChangeListener(ontologyChangeListener);
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        return configuration.getFreshEntityPolicy();
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return configuration.getIndividualNodeSetPolicy();
    }

    /**
     * @return the data factory
     */
    public OWLDataFactory getOWLDataFactory() {
        return rootOntology.getOWLOntologyManager().getOWLDataFactory();
    }
}

