package at.ainf.owlapi3.debugging;

import at.ainf.theory.model.UnsatisfiableFormulasException;
import at.ainf.theory.storage.HittingSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;
/*
 * Copyright (C) 2006, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 24-Nov-2006<br><br>
 * <p/>
 * An abstract debugger which provides common infrastructure for finding
 * multiple justification.  This functionality relies on a concrete implementation
 * of a debugger that can compute a minimal set of axioms that cause the unsatisfiability.
 */
public abstract class AbstractOWLDebugger implements OWLDebugger {

    public OWLTheory getTheory() {
        return theory;
    }

    public void setTheory(OWLTheory theory) {
        this.theory = theory;
    }

    private OWLTheory theory;

    private TreeSearch<HittingSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> strategy;

    public TreeSearch<HittingSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> getStrategy() {
        return strategy;
    }

    protected void setStrategy(TreeSearch<HittingSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> strategy) {
        this.strategy = strategy;
    }

    public Set<Set<OWLLogicalAxiom>> getConflicts() throws OWLException {
        return strategy.getStorage().getConflictSets();
    }

    public Set<HittingSet<OWLLogicalAxiom>> getHittingSets() throws OWLException {
        return strategy.getStorage().getValidHittingSets();
    }

    public abstract boolean debug() throws OWLException, UnsatisfiableFormulasException;

    public OWLOntology getOWLOntology() throws OWLException {
        return this.theory.getOntology();
    }
}
