package at.ainf.owlapi3.debugging;

import at.ainf.diagnosis.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.storage.HittingSet;
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
 */
public interface OWLDebugger {

    /**
     * Gets the <code>OWLOntology</code> that is being debugged.
     *
     * @return
     * @throws org.semanticweb.owlapi.model.OWLException
     *
     */
    public OWLOntology getOWLOntology() throws OWLException;

    public Set<Set<OWLLogicalAxiom>> getConflicts() throws OWLException;

    public Set<HittingSet<OWLLogicalAxiom>> getHittingSets() throws OWLException;

    public OWLTheory getTheory();

    public void setTheory(OWLTheory theory);

    void setMaxHittingSets(int number);

    /**
     * Gets the first set of supporting (SOS) axioms that are responsible for the specified class
     * being inconsistent.
     *
     * @return
     * @throws OWLException
     */
    //public Collection<OWLAxiom> getConflictForInconsistentClass(OWLClassExpression cls) throws OWLException;
    public boolean debug() throws OWLException, UnsatisfiableFormulasException;

    void dispose();
}
