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

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.*;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.util.OWLObjectPropertyManager;
import org.semanticweb.owlapi.util.Version;

import java.util.*;

/**
 * This class is taken from LogMap and was written by Ernesto Jimenez Ruiz
 * <p/>
 * Repairs a bugs in StructuralReasoner.getDisjointClasses
 *
 * @author ernesto
 */
public class ExtendedStructuralReasoner extends OWLExtendedReasonerBase {

    private static final String NAME = "Extended Structural Reasoner";

    public ExtendedStructuralReasoner(OWLOntology rootOntology) {
        this(rootOntology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
    }

    public ExtendedStructuralReasoner(OWLOntology ontology, OWLReasonerConfiguration configuration, BufferingMode buffering) {
        super(ontology, configuration, buffering);
        pm = configuration.getProgressMonitor() == null ? new NullReasonerProgressMonitor() : configuration.getProgressMonitor();
        prepareReasoner();
    }

    protected boolean isLiteral(OWLClassExpression superCls) {
        return !superCls.isAnonymous() || superCls.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF;
    }

    public boolean isSubClassOf(OWLClass cls1, OWLClass cls2) {
        return getSubClasses(cls2, false).getFlattened().contains(cls1);
        //Checks only asserted axioms!!
        //isEntailed(super.getDataFactory().getOWLSubClassOfAxiom(cls1, cls2));
    }


    public boolean areEquivalent(OWLClass cls1, OWLClass cls2) {
        return (getEquivalentClasses(cls1).getEntities().contains(cls2)) ||
                (getEquivalentClasses(cls2).getEntities().contains(cls1));
    }

    public String getReasonerName() {
        return NAME;
    }

    private static final Version version = new Version(1, 0, 0, 0);
    protected final ReasonerProgressMonitor pm;
    private final ClassHierarchyInfo classHierarchyInfo = new ClassHierarchyInfo();
    private final ObjectPropertyHierarchyInfo objectPropertyHierarchyInfo = new ObjectPropertyHierarchyInfo();
    private final DataPropertyHierarchyInfo dataPropertyHierarchyInfo = new DataPropertyHierarchyInfo();
    private boolean interrupted = false;
    private boolean prepared = false;

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        return FreshEntityPolicy.ALLOW;
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return IndividualNodeSetPolicy.BY_NAME;
    }

    @Override
    public Version getReasonerVersion() {
        return version;
    }

    @Override
    protected void handleChanges(Set<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
        handleChanges(addAxioms, removeAxioms, classHierarchyInfo);
        handleChanges(addAxioms, removeAxioms, objectPropertyHierarchyInfo);
        handleChanges(addAxioms, removeAxioms, dataPropertyHierarchyInfo);
    }

    private <T extends OWLObject> void handleChanges(Set<OWLAxiom> added, Set<OWLAxiom> removed, HierarchyInfo<T> hierarchyInfo) {
        Set<T> sig = hierarchyInfo.getEntitiesInSignature(added);
        sig.addAll(hierarchyInfo.getEntitiesInSignature(removed));
        hierarchyInfo.processChanges(sig, added, removed);

    }

    @Override
    public void interrupt() {
        interrupted = true;
    }

    private void ensurePrepared() {
        if (!prepared) {
            prepareReasoner();
        }
    }

    /**
     * @throws ReasonerInterruptedException on interruption
     * @throws TimeOutException             on timeout
     */
    public void prepareReasoner() throws ReasonerInterruptedException, TimeOutException {
        classHierarchyInfo.computeHierarchy();
        objectPropertyHierarchyInfo.computeHierarchy();
        dataPropertyHierarchyInfo.computeHierarchy();
        prepared = true;
    }

    @Override

    public void precomputeInferences(InferenceType... inferenceTypes) throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException {
        prepareReasoner();
    }

    @Override

    public boolean isPrecomputed(InferenceType inferenceType) {
        return true;
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return CollectionFactory.createSet(InferenceType.CLASS_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.DATA_PROPERTY_HIERARCHY);
    }

    protected void throwExceptionIfInterrupted() {
        if (interrupted) {
            interrupted = false;
            throw new ReasonerInterruptedException();
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
        return true;
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) throws ReasonerInterruptedException, TimeOutException, ClassExpressionNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        return !classExpression.isAnonymous() && !getEquivalentClasses(classExpression.asOWLClass()).contains(getDataFactory().getOWLNothing());
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() throws ReasonerInterruptedException, TimeOutException {
        return OWLClassNode.getBottomNode();
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException, AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        return getRootOntology().containsAxiomIgnoreAnnotations(axiom, true);
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException, AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        for (OWLAxiom ax : axioms) {
            if (!getRootOntology().containsAxiomIgnoreAnnotations(ax, true)) {
                return false;
            }
        }
        return true;
    }

    @Override

    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return false;
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        ensurePrepared();
        return classHierarchyInfo.getEquivalents(getDataFactory().getOWLThing());
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        ensurePrepared();
        return classHierarchyInfo.getEquivalents(getDataFactory().getOWLNothing());
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        OWLClassNodeSet ns = new OWLClassNodeSet();
        if (!ce.isAnonymous()) {
            ensurePrepared();
            return classHierarchyInfo.getNodeHierarchyChildren(ce.asOWLClass(), direct, ns);
        }
        return ns;
    }

    public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct, int leastNumberOfNodes) throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        Set<Node<OWLClass>> ns = new HashSet<Node<OWLClass>>(getRootOntology().getSignature().size());
        if (!ce.isAnonymous()) {
            ensurePrepared();
            OWLClassNodeSet children = (OWLClassNodeSet)classHierarchyInfo.getNodeHierarchyChildren(ce.asOWLClass(), true, new OWLClassNodeSet());
            ns.addAll(children.getNodes());
            if (direct)
                return children;
            else
            {
                while (!children.isEmpty() && ns.size() < leastNumberOfNodes){
                    Iterator<Node<OWLClass>> it = children.iterator();
                    Set<OWLClass> entities = it.next().getEntities();
                    it.remove();
                    for (OWLClass entity : entities) {
                        NodeSet<OWLClass> localChildren = classHierarchyInfo.getNodeHierarchyChildren(entity, true, new OWLClassNodeSet());
                        children.addAllNodes(localChildren.getNodes());
                    }
                    ns.addAll(children.getNodes());
                }
            }
        }
        return new OWLClassNodeSet(ns);
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct) throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        OWLClassNodeSet ns = new OWLClassNodeSet();
        if (!ce.isAnonymous()) {
            ensurePrepared();
            return classHierarchyInfo.getNodeHierarchyParents(ce.asOWLClass(), direct, ns);
        }
        return ns;
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce) throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        if (!ce.isAnonymous()) {
            return classHierarchyInfo.getEquivalents(ce.asOWLClass());
        } else {
            return new OWLClassNode();
        }
    }

    @Override
    /**
     * There was an error in original method. the result set contained both the given class and its equivalents.
     */
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        ensurePrepared();
        OWLClassNodeSet nodeSet = new OWLClassNodeSet();
        if (!ce.isAnonymous()) {
            for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
                for (OWLDisjointClassesAxiom ax : ontology.getDisjointClassesAxioms(ce.asOWLClass())) {
                    for (OWLClassExpression op : ax.getClassExpressions()) {
                        if (!op.isAnonymous() && !op.equals(ce)) {
                            nodeSet.addNode(getEquivalentClasses(op));
                        }
                    }
                }
            }
        }
        return nodeSet;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        ensurePrepared();
        return objectPropertyHierarchyInfo.getEquivalents(getDataFactory().getOWLTopObjectProperty());
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        ensurePrepared();
        return objectPropertyHierarchyInfo.getEquivalents(getDataFactory().getOWLBottomObjectProperty());
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe, boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        OWLObjectPropertyNodeSet ns = new OWLObjectPropertyNodeSet();
        ensurePrepared();
        return objectPropertyHierarchyInfo.getNodeHierarchyChildren(pe, direct, ns);
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe, boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        OWLObjectPropertyNodeSet ns = new OWLObjectPropertyNodeSet();
        ensurePrepared();
        return objectPropertyHierarchyInfo.getNodeHierarchyParents(pe, direct, ns);
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        return objectPropertyHierarchyInfo.getEquivalents(pe);
    }

    @Override

    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return new OWLObjectPropertyNodeSet();
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        OWLObjectPropertyExpression inv = pe.getInverseProperty().getSimplified();
        return getEquivalentObjectProperties(inv);
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {

        ensurePrepared();
        DefaultNodeSet<OWLClass> result = new OWLClassNodeSet();
        for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
            for (OWLObjectPropertyDomainAxiom axiom : ontology.getObjectPropertyDomainAxioms(pe)) {
                result.addNode(getEquivalentClasses(axiom.getDomain()));
                if (!direct) {
                    result.addAllNodes(getSuperClasses(axiom.getDomain(), false).getNodes());
                }
            }

            for (OWLObjectPropertyExpression invPe : getInverseObjectProperties(pe).getEntities()) {
                for (OWLObjectPropertyRangeAxiom axiom : ontology.getObjectPropertyRangeAxioms(invPe)) {
                    result.addNode(getEquivalentClasses(axiom.getRange()));
                    if (!direct) {
                        result.addAllNodes(getSuperClasses(axiom.getRange(), false).getNodes());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        DefaultNodeSet<OWLClass> result = new OWLClassNodeSet();
        for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
            for (OWLObjectPropertyRangeAxiom axiom : ontology.getObjectPropertyRangeAxioms(pe)) {
                result.addNode(getEquivalentClasses(axiom.getRange()));
                if (!direct) {
                    result.addAllNodes(getSuperClasses(axiom.getRange(), false).getNodes());
                }
            }
            for (OWLObjectPropertyExpression invPe : getInverseObjectProperties(pe).getEntities()) {
                for (OWLObjectPropertyDomainAxiom axiom : ontology.getObjectPropertyDomainAxioms(invPe)) {
                    result.addNode(getEquivalentClasses(axiom.getDomain()));
                    if (!direct) {
                        result.addAllNodes(getSuperClasses(axiom.getDomain(), false).getNodes());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        ensurePrepared();
        return dataPropertyHierarchyInfo.getEquivalents(getDataFactory().getOWLTopDataProperty());
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        ensurePrepared();
        return dataPropertyHierarchyInfo.getEquivalents(getDataFactory().getOWLBottomDataProperty());
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        OWLDataPropertyNodeSet ns = new OWLDataPropertyNodeSet();
        return dataPropertyHierarchyInfo.getNodeHierarchyChildren(pe, direct, ns);
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        OWLDataPropertyNodeSet ns = new OWLDataPropertyNodeSet();
        return dataPropertyHierarchyInfo.getNodeHierarchyParents(pe, direct, ns);
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        return dataPropertyHierarchyInfo.getEquivalents(pe);
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        DefaultNodeSet<OWLDataProperty> result = new OWLDataPropertyNodeSet();
        for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
            for (OWLDisjointDataPropertiesAxiom axiom : ontology.getDisjointDataPropertiesAxioms(pe.asOWLDataProperty())) {
                for (OWLDataPropertyExpression dpe : axiom.getPropertiesMinus(pe)) {
                    if (!dpe.isAnonymous()) {
                        result.addNode(dataPropertyHierarchyInfo.getEquivalents(dpe.asOWLDataProperty()));
                        result.addAllNodes(getSubDataProperties(dpe.asOWLDataProperty(), false).getNodes());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        DefaultNodeSet<OWLClass> result = new OWLClassNodeSet();
        for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
            for (OWLDataPropertyDomainAxiom axiom : ontology.getDataPropertyDomainAxioms(pe)) {
                result.addNode(getEquivalentClasses(axiom.getDomain()));
                if (!direct) {
                    result.addAllNodes(getSuperClasses(axiom.getDomain(), false).getNodes());
                }
            }
        }
        return result;
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        DefaultNodeSet<OWLClass> result = new OWLClassNodeSet();
        for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
            for (OWLClassAssertionAxiom axiom : ontology.getClassAssertionAxioms(ind)) {
                OWLClassExpression ce = axiom.getClassExpression();
                if (!ce.isAnonymous()) {
                    result.addNode(classHierarchyInfo.getEquivalents(ce.asOWLClass()));
                    if (!direct) {
                        result.addAllNodes(getSuperClasses(ce, false).getNodes());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        DefaultNodeSet<OWLNamedIndividual> result = new OWLNamedIndividualNodeSet();
        if (!ce.isAnonymous()) {
            OWLClass cls = ce.asOWLClass();
            Set<OWLClass> clses = new HashSet<OWLClass>();
            clses.add(cls);
            if (!direct) {
                clses.addAll(getSubClasses(cls, false).getFlattened());
            }
            for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
                for (OWLClass curCls : clses) {
                    for (OWLClassAssertionAxiom axiom : ontology.getClassAssertionAxioms(curCls)) {
                        OWLIndividual individual = axiom.getIndividual();
                        if (!individual.isAnonymous()) {
                            if (getIndividualNodeSetPolicy().equals(IndividualNodeSetPolicy.BY_SAME_AS)) {
                                result.addNode(getSameIndividuals(individual.asOWLNamedIndividual()));
                            } else {
                                result.addNode(new OWLNamedIndividualNode(individual.asOWLNamedIndividual()));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind, OWLObjectPropertyExpression pe) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        OWLNamedIndividualNodeSet result = new OWLNamedIndividualNodeSet();
        Node<OWLObjectPropertyExpression> inverses = getInverseObjectProperties(pe);
        for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
            for (OWLObjectPropertyAssertionAxiom axiom : ontology.getObjectPropertyAssertionAxioms(ind)) {
                if (!axiom.getObject().isAnonymous()) {
                    if (axiom.getProperty().getSimplified().equals(pe.getSimplified())) {
                        if (getIndividualNodeSetPolicy().equals(IndividualNodeSetPolicy.BY_SAME_AS)) {
                            result.addNode(getSameIndividuals(axiom.getObject().asOWLNamedIndividual()));
                        } else {
                            result.addNode(new OWLNamedIndividualNode(axiom.getObject().asOWLNamedIndividual()));
                        }
                    }
                }
                // Inverse of pe
                if (axiom.getObject().equals(ind) && !axiom.getSubject().isAnonymous()) {
                    OWLObjectPropertyExpression invPe = axiom.getProperty().getInverseProperty().getSimplified();
                    if (!invPe.isAnonymous() && inverses.contains(invPe.asOWLObjectProperty())) {
                        if (getIndividualNodeSetPolicy().equals(IndividualNodeSetPolicy.BY_SAME_AS)) {
                            result.addNode(getSameIndividuals(axiom.getObject().asOWLNamedIndividual()));
                        } else {
                            result.addNode(new OWLNamedIndividualNode(axiom.getObject().asOWLNamedIndividual()));
                        }
                    }
                }

            }
        }
        // Could do other stuff like inspecting owl:hasValue restrictions
        return result;
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        Set<OWLLiteral> literals = new HashSet<OWLLiteral>();
        Set<OWLDataProperty> superProperties = getSuperDataProperties(pe, false).getFlattened();
        superProperties.addAll(getEquivalentDataProperties(pe).getEntities());
        for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
            for (OWLDataPropertyAssertionAxiom axiom : ontology.getDataPropertyAssertionAxioms(ind)) {
                if (superProperties.contains(axiom.getProperty().asOWLDataProperty())) {
                    literals.add(axiom.getObject());
                }
            }
        }
        return literals;
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        ensurePrepared();
        Set<OWLNamedIndividual> inds = new HashSet<OWLNamedIndividual>();
        Set<OWLSameIndividualAxiom> processed = new HashSet<OWLSameIndividualAxiom>();
        List<OWLNamedIndividual> stack = new ArrayList<OWLNamedIndividual>();
        stack.add(ind);
        while (!stack.isEmpty()) {
            OWLNamedIndividual currentInd = stack.remove(0);
            for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
                for (OWLSameIndividualAxiom axiom : ontology.getSameIndividualAxioms(currentInd)) {
                    if (!processed.contains(axiom)) {
                        processed.add(axiom);
                        for (OWLIndividual i : axiom.getIndividuals()) {
                            if (!i.isAnonymous()) {
                                OWLNamedIndividual namedInd = i.asOWLNamedIndividual();
                                if (!inds.contains(namedInd)) {
                                    inds.add(namedInd);
                                    stack.add(namedInd);
                                }
                            }
                        }
                    }
                }
            }
        }

        return new OWLNamedIndividualNode(inds);
    }

    @Override

    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        return new OWLNamedIndividualNodeSet();
    }

    protected OWLDataFactory getDataFactory() {
        return getRootOntology().getOWLOntologyManager().getOWLDataFactory();
    }

    /**
     * @param showBottomNode true if bottom node is to be showed
     */
    public void dumpClassHierarchy(boolean showBottomNode) {
        dumpClassHierarchy(OWLClassNode.getTopNode(), 0, showBottomNode);
    }

    private void dumpClassHierarchy(Node<OWLClass> cls, int level, boolean showBottomNode) {
        if (!showBottomNode && cls.isBottomNode()) {
            return;
        }
        printIndent(level);
        OWLClass representative = cls.getRepresentativeElement();
        System.out.println(getEquivalentClasses(representative));
        for (Node<OWLClass> subCls : getSubClasses(representative, true)) {
            dumpClassHierarchy(subCls, level + 1, showBottomNode);
        }
    }

    /**
     * @param showBottomNode true if bottom node is to be showed
     */
    public void dumpObjectPropertyHierarchy(boolean showBottomNode) {
        dumpObjectPropertyHierarchy(OWLObjectPropertyNode.getTopNode(), 0, showBottomNode);
    }

    private void dumpObjectPropertyHierarchy(Node<OWLObjectPropertyExpression> cls, int level, boolean showBottomNode) {
        if (!showBottomNode && cls.isBottomNode()) {
            return;
        }
        printIndent(level);
        OWLObjectPropertyExpression representative = cls.getRepresentativeElement();
        System.out.println(getEquivalentObjectProperties(representative));
        for (Node<OWLObjectPropertyExpression> subProp : getSubObjectProperties(representative, true)) {
            dumpObjectPropertyHierarchy(subProp, level + 1, showBottomNode);
        }
    }

    /**
     * @param showBottomNode true if bottom node is to be showed
     */
    public void dumpDataPropertyHierarchy(boolean showBottomNode) {
        dumpDataPropertyHierarchy(OWLDataPropertyNode.getTopNode(), 0, showBottomNode);
    }

    private void dumpDataPropertyHierarchy(Node<OWLDataProperty> cls, int level, boolean showBottomNode) {
        if (!showBottomNode && cls.isBottomNode()) {
            return;
        }
        printIndent(level);
        OWLDataProperty representative = cls.getRepresentativeElement();
        System.out.println(getEquivalentDataProperties(representative));
        for (Node<OWLDataProperty> subProp : getSubDataProperties(representative, true)) {
            dumpDataPropertyHierarchy(subProp, level + 1, showBottomNode);
        }
    }

    private void printIndent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////
    //////  HierarchyInfo
    //////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * An interface for objects who can provide the parents and children of some object.
     *
     * @param <T>
     */
    private interface RawHierarchyProvider<T> {

        /**
         * Gets the parents as asserted.  These parents may also be children (resulting in equivalences).
         *
         * @param child The child whose parents are to be retrieved
         * @return The raw asserted parents of the specified child.  If the child does not have any parents
         *         then the empty set can be returned.
         */
        Collection<T> getParents(T child);

        /**
         * Gets the children as asserted
         *
         * @param parent The parent whose children are to be retrieved
         * @return The raw asserted children of the speicified parent
         */
        Collection<T> getChildren(T parent);

    }

    private static class NodeCache<T extends OWLObject> {

        private HierarchyInfo<T> hierarchyInfo;
        private Node<T> topNode;
        private Node<T> bottomNode;
        private Map<T, Node<T>> map = new HashMap<T, Node<T>>();

        protected NodeCache(HierarchyInfo<T> hierarchyInfo) {
            this.hierarchyInfo = hierarchyInfo;
            clearTopNode();
            clearBottomNode();
        }

        public void addNode(Node<T> node) {
            for (T element : node.getEntities()) {
                map.put(element, node);
                if (element.isTopEntity()) {
                    topNode = node;
                } else if (element.isBottomEntity()) {
                    bottomNode = node;
                }
            }
        }

        public Set<Node<T>> getNodes(Set<T> elements) {
            Set<Node<T>> result = new HashSet<Node<T>>();
            for (T element : elements) {
                result.add(getNode(element));
            }
            return result;
        }

        public Node<T> getNode(T containing) {
            Node<T> parentNode = map.get(containing);
            if (parentNode != null) {
                return parentNode;
            } else {
                return hierarchyInfo.createNode(Collections.singleton(containing));
            }
        }

        public void addNode(Set<T> elements) {
            addNode(hierarchyInfo.createNode(elements));
        }

        public Node<T> getTopNode() {
            return topNode;
        }

        public Node<T> getBottomNode() {
            return bottomNode;
        }

        public void clearTopNode() {
            removeNode(hierarchyInfo.topEntity);
            topNode = hierarchyInfo.createNode(Collections.singleton(hierarchyInfo.topEntity));
            addNode(topNode);
        }

        public void clearBottomNode() {
            removeNode(hierarchyInfo.bottomEntity);
            bottomNode = hierarchyInfo.createNode(Collections.singleton(hierarchyInfo.bottomEntity));
            addNode(bottomNode);
        }

        public void clearNodes(Set<T> containing) {
            for (T entity : containing) {
                removeNode(entity);
            }
        }

        public void clear() {
            map.clear();
            clearTopNode();
            clearBottomNode();
        }

        public void removeNode(T containing) {
            Node<T> node = map.remove(containing);
            if (node != null) {
                for (T object : node.getEntities()) {
                    map.remove(object);
                }
            }
        }
    }

    private abstract class HierarchyInfo<T extends OWLObject> {

        /**
         * The entity that always appears in the top node in the hierarchy
         */
        T topEntity;
        /**
         * The entity that always appears as the bottom node in the hierarchy
         */
        T bottomEntity;
        private RawHierarchyProvider<T> rawParentChildProvider;
        private Set<T> directChildrenOfTopNode = new HashSet<T>();
        private Set<T> directParentsOfBottomNode = new HashSet<T>();
        private NodeCache<T> nodeCache;
        private String name;
        private int classificationSize;

        public HierarchyInfo(String name, T topEntity, T bottomEntity, RawHierarchyProvider<T> rawParentChildProvider) {
            this.topEntity = topEntity;
            this.bottomEntity = bottomEntity;
            this.nodeCache = new NodeCache<T>(this);
            this.rawParentChildProvider = rawParentChildProvider;
            this.name = name;
        }

        public RawHierarchyProvider<T> getRawParentChildProvider() {
            return rawParentChildProvider;
        }

        /**
         * Gets the set of relevant entities from the specified ontology
         *
         * @param ont The ontology
         * @return A set of entities to be "classified"
         */
        protected abstract Set<T> getEntities(OWLOntology ont);

        /**
         * Creates a node for a given set of entities
         *
         * @param cycle The set of entities
         * @return A node
         */
        protected abstract DefaultNode<T> createNode(Set<T> cycle);

        protected abstract DefaultNode<T> createNode();

        /**
         * Gets the set of relevant entities in a particular axiom
         *
         * @param ax The axiom
         * @return The set of relevant entities in the signature of the specified axiom
         */
        protected abstract Set<? extends T> getEntitiesInSignature(OWLAxiom ax);

        Set<T> getEntitiesInSignature(Set<OWLAxiom> axioms) {
            Set<T> result = new HashSet<T>();
            for (OWLAxiom ax : axioms) {
                result.addAll(getEntitiesInSignature(ax));
            }
            return result;
        }

        public void computeHierarchy() {
            pm.reasonerTaskStarted("Computing " + name + " hierarchy");
            pm.reasonerTaskBusy();
            nodeCache.clear();
            Map<T, Collection<T>> cache = new HashMap<T, Collection<T>>();
            Set<T> entities = new HashSet<T>();
            for (OWLOntology ont : getRootOntology().getImportsClosure()) {
                entities.addAll(getEntities(ont));
            }
            classificationSize = entities.size();
            pm.reasonerTaskProgressChanged(0, classificationSize);
            updateForSignature(entities, cache);
            pm.reasonerTaskStopped();
        }

        private void updateForSignature(Set<T> signature, Map<T, Collection<T>> cache) {
            HashSet<Set<T>> cyclesResult = new HashSet<Set<T>>();
            Set<T> processed = new HashSet<T>();
            nodeCache.clearTopNode();
            nodeCache.clearBottomNode();
            nodeCache.clearNodes(signature);

            directChildrenOfTopNode.removeAll(signature);

            Set<T> equivTopOrChildrenOfTop = new HashSet<T>();
            Set<T> equivBottomOrParentsOfBottom = new HashSet<T>();
            for (T entity : signature) {
                if (!processed.contains(entity)) {
                    pm.reasonerTaskProgressChanged(processed.size(), signature.size());
                    tarjan(entity, 0, new Stack<T>(), new HashMap<T, Integer>(), new HashMap<T, Integer>(), cyclesResult, processed, new HashSet<T>(), cache, equivTopOrChildrenOfTop, equivBottomOrParentsOfBottom);
                    throwExceptionIfInterrupted();
                }
            }
            // Store new cycles
            for (Set<T> cycle : cyclesResult) {
                nodeCache.addNode(cycle);
            }

            directChildrenOfTopNode.addAll(equivTopOrChildrenOfTop);
            directChildrenOfTopNode.removeAll(nodeCache.getTopNode().getEntities());

            directParentsOfBottomNode.addAll(equivBottomOrParentsOfBottom);
            directParentsOfBottomNode.removeAll(nodeCache.getBottomNode().getEntities());


            // Now check that each found cycle has a proper parent an child
            for (Set<T> node : cyclesResult) {
                if (!node.contains(topEntity) && !node.contains(bottomEntity)) {
                    boolean childOfTop = true;
                    for (T element : node) {
                        Collection<T> parents = rawParentChildProvider.getParents(element);
                        parents.removeAll(node);
                        parents.removeAll(nodeCache.getTopNode().getEntities());
                        if (!parents.isEmpty()) {
                            childOfTop = false;
                            break;
                        }
                    }
                    if (childOfTop) {
                        directChildrenOfTopNode.addAll(node);
                    }

                    boolean parentOfBottom = true;
                    for (T element : node) {
                        Collection<T> children = rawParentChildProvider.getChildren(element);
                        children.removeAll(node);
                        children.removeAll(nodeCache.getBottomNode().getEntities());
                        if (!children.isEmpty()) {
                            parentOfBottom = false;
                            break;
                        }
                    }
                    if (parentOfBottom) {
                        directParentsOfBottomNode.addAll(node);
                    }
                }

            }

        }

        /**
         * Processes the specified signature that represents the signature of potential changes
         *
         * @param signature The signature
         * @param added     added axioms
         * @param removed   removed axioms
         */
        @SuppressWarnings("unused")
        public void processChanges(Set<T> signature, Set<OWLAxiom> added, Set<OWLAxiom> removed) {
            updateForSignature(signature, null);
        }


        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Applies the tarjan algorithm for a given entity.  This computes the cycle that the entity is involved in (if
         * any).
         *
         * @param entity          The entity
         * @param index           index
         * @param stack           stack
         * @param indexMap        index map
         * @param lowlinkMap      low link map
         * @param result          result
         * @param processed       processed
         * @param stackEntities   stack entities
         * @param cache           A cache of children to parents - may be <code>null</code> if no caching is to take place.
         * @param childrenOfTop   A set of entities that have a raw parent that is the top entity
         * @param parentsOfBottom A set of entities that have a raw parent that is the bottom entity
         */
        public void tarjan(T entity, int index, Stack<T> stack, Map<T, Integer> indexMap, Map<T, Integer> lowlinkMap, Set<Set<T>> result, Set<T> processed, Set<T> stackEntities, Map<T, Collection<T>> cache, Set<T> childrenOfTop, Set<T> parentsOfBottom) {
            throwExceptionIfInterrupted();
            if (processed.add(entity)) {
                Collection<T> rawChildren = rawParentChildProvider.getChildren(entity);
                if (rawChildren.isEmpty() || rawChildren.contains(bottomEntity)) {
                    parentsOfBottom.add(entity);
                }
            }
            pm.reasonerTaskProgressChanged(processed.size(), classificationSize);
            indexMap.put(entity, index);
            lowlinkMap.put(entity, index);
            index = index + 1;
            stack.push(entity);
            stackEntities.add(entity);

            // Get the raw parents - cache if necessary
            Collection<T> rawParents = null;
            if (cache != null) {
                // We are therefore caching raw parents of children.
                rawParents = cache.get(entity);
                if (rawParents == null) {
                    // Not in cache!
                    rawParents = rawParentChildProvider.getParents(entity);
                    // Note down if our entity is a
                    if (rawParents.isEmpty() || rawParents.contains(topEntity)) {
                        childrenOfTop.add(entity);
                    }
                    cache.put(entity, rawParents);

                }
            } else {
                rawParents = rawParentChildProvider.getParents(entity);
                // Note down if our entity is a
                if (rawParents.isEmpty() || rawParents.contains(topEntity)) {
                    childrenOfTop.add(entity);
                }
            }


            for (T superEntity : rawParents) {
                if (!indexMap.containsKey(superEntity)) {
                    tarjan(superEntity, index, stack, indexMap, lowlinkMap, result, processed, stackEntities, cache, childrenOfTop, parentsOfBottom);
                    lowlinkMap.put(entity, Math.min(lowlinkMap.get(entity), lowlinkMap.get(superEntity)));
                } else if (stackEntities.contains(superEntity)) {
                    lowlinkMap.put(entity, Math.min(lowlinkMap.get(entity), indexMap.get(superEntity)));
                }
            }
            if (lowlinkMap.get(entity).equals(indexMap.get(entity))) {
                Set<T> scc = new HashSet<T>();
                while (true) {
                    T clsPrime = stack.pop();
                    stackEntities.remove(clsPrime);
                    scc.add(clsPrime);
                    if (clsPrime.equals(entity)) {
                        break;
                    }
                }
                if (scc.size() > 1) {
                    // We ADD a cycle
                    result.add(scc);
                }
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////

        public NodeSet<T> getNodeHierarchyChildren(T parent, boolean direct, DefaultNodeSet<T> ns) {
            Node<T> node = nodeCache.getNode(parent);

            if (node.isBottomNode()) {
                return ns;
            }


            Set<T> directChildren = new HashSet<T>();
            for (T equiv : node) {
                directChildren.addAll(rawParentChildProvider.getChildren(equiv));
                if (directParentsOfBottomNode.contains(equiv)) {
                    ns.addNode(nodeCache.getBottomNode());
                }
            }
            directChildren.removeAll(node.getEntities());

            if (node.isTopNode()) {
                // Special treatment
                directChildren.addAll(directChildrenOfTopNode);
            }

            for (Node<T> childNode : nodeCache.getNodes(directChildren)) {
                ns.addNode(childNode);
            }


            if (!direct) {
                for (T child : directChildren) {
                    getNodeHierarchyChildren(child, direct, ns);
                }
            }
            return ns;
        }

        public NodeSet<T> getNodeHierarchyParents(T child, boolean direct, DefaultNodeSet<T> ns) {
            Node<T> node = nodeCache.getNode(child);

            if (node.isTopNode()) {
                return ns;
            }


            Set<T> directParents = new HashSet<T>();
            for (T equiv : node) {
                directParents.addAll(rawParentChildProvider.getParents(equiv));
                if (directChildrenOfTopNode.contains(equiv)) {
                    ns.addNode(nodeCache.getTopNode());
                }
            }
            directParents.removeAll(node.getEntities());

            if (node.isBottomNode()) {
                // Special treatment
                directParents.addAll(directParentsOfBottomNode);
            }

            for (Node<T> parentNode : nodeCache.getNodes(directParents)) {
                ns.addNode(parentNode);
            }

            if (!direct) {
                for (T parent : directParents) {
                    getNodeHierarchyParents(parent, direct, ns);
                }
            }
            return ns;
        }

        public Node<T> getEquivalents(T element) {
            return nodeCache.getNode(element);
        }
    }

    private class ClassHierarchyInfo extends HierarchyInfo<OWLClass> {

        public ClassHierarchyInfo() {
            super("class", getDataFactory().getOWLThing(), getDataFactory().getOWLNothing(), new RawClassHierarchyProvider());
        }

        @Override
        protected Set<OWLClass> getEntitiesInSignature(OWLAxiom ax) {
            return ax.getClassesInSignature();
        }

        @Override
        protected DefaultNode<OWLClass> createNode(Set<OWLClass> cycle) {
            return new OWLClassNode(cycle);
        }

        @Override
        protected Set<OWLClass> getEntities(OWLOntology ont) {
            return ont.getClassesInSignature();
        }

        @Override
        protected DefaultNode<OWLClass> createNode() {
            return new OWLClassNode();
        }
    }

    private class ObjectPropertyHierarchyInfo extends HierarchyInfo<OWLObjectPropertyExpression> {

        public ObjectPropertyHierarchyInfo() {
            super("object property", getDataFactory().getOWLTopObjectProperty(), getDataFactory().getOWLBottomObjectProperty(), new RawObjectPropertyHierarchyProvider());
        }

        @Override
        protected Set<OWLObjectPropertyExpression> getEntitiesInSignature(OWLAxiom ax) {
            Set<OWLObjectPropertyExpression> result = new HashSet<OWLObjectPropertyExpression>();
            for (OWLObjectProperty property : ax.getObjectPropertiesInSignature()) {
                result.add(property);
                result.add(property.getInverseProperty());
            }
            return result;
        }

        @Override
        protected Set<OWLObjectPropertyExpression> getEntities(OWLOntology ont) {
            Set<OWLObjectPropertyExpression> result = new HashSet<OWLObjectPropertyExpression>();
            for (OWLObjectPropertyExpression property : ont.getObjectPropertiesInSignature()) {
                result.add(property);
                result.add(property.getInverseProperty());
            }
            return result;
        }

        @Override
        protected DefaultNode<OWLObjectPropertyExpression> createNode(Set<OWLObjectPropertyExpression> cycle) {
            return new OWLObjectPropertyNode(cycle);
        }

        @Override
        protected DefaultNode<OWLObjectPropertyExpression> createNode() {
            return new OWLObjectPropertyNode();
        }

        @Override
        public void processChanges(Set<OWLObjectPropertyExpression> signature, Set<OWLAxiom> added, Set<OWLAxiom> removed) {
            boolean rebuild = false;
            for (OWLAxiom ax : added) {
                if (ax instanceof OWLObjectPropertyAxiom) {
                    rebuild = true;
                    break;
                }
            }
            if (!rebuild) {
                for (OWLAxiom ax : removed) {
                    if (ax instanceof OWLObjectPropertyAxiom) {
                        rebuild = true;
                        break;
                    }
                }
            }
            if (rebuild) {
                ((RawObjectPropertyHierarchyProvider) getRawParentChildProvider()).rebuild();
            }
            super.processChanges(signature, added, removed);
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class DataPropertyHierarchyInfo extends HierarchyInfo<OWLDataProperty> {

        public DataPropertyHierarchyInfo() {
            super("data property", getDataFactory().getOWLTopDataProperty(), getDataFactory().getOWLBottomDataProperty(), new RawDataPropertyHierarchyProvider());
        }

        @Override
        protected Set<OWLDataProperty> getEntitiesInSignature(OWLAxiom ax) {
            return ax.getDataPropertiesInSignature();
        }

        @Override
        protected Set<OWLDataProperty> getEntities(OWLOntology ont) {
            return ont.getDataPropertiesInSignature();
        }

        @Override
        protected DefaultNode<OWLDataProperty> createNode(Set<OWLDataProperty> cycle) {
            return new OWLDataPropertyNode(cycle);
        }

        @Override
        protected DefaultNode<OWLDataProperty> createNode() {
            return new OWLDataPropertyNode();
        }
    }

    private class RawClassHierarchyProvider implements RawHierarchyProvider<OWLClass> {
        public RawClassHierarchyProvider() {
        }

        @Override
        public Collection<OWLClass> getParents(OWLClass child) {
            Collection<OWLClass> result = new HashSet<OWLClass>();
            for (OWLOntology ont : getRootOntology().getImportsClosure()) {
                for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(child)) {
                    OWLClassExpression superCls = ax.getSuperClass();
                    if (!superCls.isAnonymous()) {
                        result.add(superCls.asOWLClass());
                    } else if (superCls instanceof OWLObjectIntersectionOf) {
                        OWLObjectIntersectionOf intersectionOf = (OWLObjectIntersectionOf) superCls;
                        for (OWLClassExpression conjunct : intersectionOf.asConjunctSet()) {
                            if (!conjunct.isAnonymous()) {
                                result.add(conjunct.asOWLClass());
                            }
                        }
                    }
                }
                for (OWLEquivalentClassesAxiom ax : ont.getEquivalentClassesAxioms(child)) {
                    for (OWLClassExpression ce : ax.getClassExpressionsMinus(child)) {
                        if (!ce.isAnonymous()) {
                            result.add(ce.asOWLClass());
                        } else if (ce instanceof OWLObjectIntersectionOf) {
                            OWLObjectIntersectionOf intersectionOf = (OWLObjectIntersectionOf) ce;
                            for (OWLClassExpression conjunct : intersectionOf.asConjunctSet()) {
                                if (!conjunct.isAnonymous()) {
                                    result.add(conjunct.asOWLClass());
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public Collection<OWLClass> getChildren(OWLClass parent) {
            Collection<OWLClass> result = new HashSet<OWLClass>();
            for (OWLOntology ont : getRootOntology().getImportsClosure()) {
                for (OWLAxiom ax : ont.getReferencingAxioms(parent)) {
                    if (ax instanceof OWLSubClassOfAxiom) {
                        OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) ax;
                        if (!sca.getSubClass().isAnonymous()) {
                            Set<OWLClassExpression> conjuncts = sca.getSuperClass().asConjunctSet();
                            if (conjuncts.contains(parent)) {
                                result.add(sca.getSubClass().asOWLClass());
                            }
                        }
                    } else if (ax instanceof OWLEquivalentClassesAxiom) {
                        OWLEquivalentClassesAxiom eca = (OWLEquivalentClassesAxiom) ax;
                        for (OWLClassExpression ce : eca.getClassExpressions()) {
                            if (ce.containsConjunct(parent)) {
                                for (OWLClassExpression sub : eca.getClassExpressions()) {
                                    if (!sub.isAnonymous() && !sub.equals(ce)) {
                                        result.add(sub.asOWLClass());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
    }

    private class RawObjectPropertyHierarchyProvider implements RawHierarchyProvider<OWLObjectPropertyExpression> {

        private OWLObjectPropertyManager propertyManager;
        private Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> sub2Super;
        private Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> super2Sub;

        public RawObjectPropertyHierarchyProvider() {
            rebuild();
        }

        public void rebuild() {
            propertyManager = new OWLObjectPropertyManager(getRootOntology().getOWLOntologyManager(), getRootOntology());
            sub2Super = propertyManager.getPropertyHierarchy();
            super2Sub = new HashMap<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>>();
            for (OWLObjectPropertyExpression sub : sub2Super.keySet()) {
                for (OWLObjectPropertyExpression superProp : sub2Super.get(sub)) {
                    Set<OWLObjectPropertyExpression> subs = super2Sub.get(superProp);
                    if (subs == null) {
                        subs = new HashSet<OWLObjectPropertyExpression>();
                        super2Sub.put(superProp, subs);
                    }
                    subs.add(sub);
                }
            }
        }

        @Override
        public Collection<OWLObjectPropertyExpression> getParents(OWLObjectPropertyExpression child) {
            if (child.isBottomEntity()) {
                return Collections.emptySet();
            }
            Set<OWLObjectPropertyExpression> propertyExpressions = sub2Super.get(child);
            if (propertyExpressions == null) {
                return Collections.emptySet();
            } else {
                return new HashSet<OWLObjectPropertyExpression>(propertyExpressions);
            }

        }

        @Override
        public Collection<OWLObjectPropertyExpression> getChildren(OWLObjectPropertyExpression parent) {
            if (parent.isTopEntity()) {
                return Collections.emptySet();
            }
            Set<OWLObjectPropertyExpression> propertyExpressions = super2Sub.get(parent);
            if (propertyExpressions == null) {
                return Collections.emptySet();
            } else {
                return new HashSet<OWLObjectPropertyExpression>(propertyExpressions);
            }

        }
    }

    private class RawDataPropertyHierarchyProvider implements RawHierarchyProvider<OWLDataProperty> {
        public RawDataPropertyHierarchyProvider() {
        }

        @Override
        public Collection<OWLDataProperty> getParents(OWLDataProperty child) {
            Set<OWLDataProperty> properties = new HashSet<OWLDataProperty>();
            for (OWLDataPropertyExpression prop : child.getSuperProperties(getRootOntology().getImportsClosure())) {
                properties.add(prop.asOWLDataProperty());
            }
            return properties;
        }

        @Override
        public Collection<OWLDataProperty> getChildren(OWLDataProperty parent) {
            Set<OWLDataProperty> properties = new HashSet<OWLDataProperty>();
            for (OWLDataPropertyExpression prop : parent.getSubProperties(getRootOntology().getImportsClosure())) {
                properties.add(prop.asOWLDataProperty());
            }
            return properties;
        }
    }
}

