package at.ainf.pluginprotege.views.diagnosistreeview;

import at.ainf.diagnosis.storage.HittingSet;
import at.ainf.diagnosis.storage.HittingSetImpl;
import at.ainf.diagnosis.tree.Node;
import at.ainf.diagnosis.tree.NodeCostsEstimator;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.pluginprotege.WorkspaceTab;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.usage.UsageFilter;
import org.semanticweb.owlapi.model.*;

import javax.swing.tree.DefaultTreeModel;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.08.11
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class DiagnosesTreeModel extends DefaultTreeModel  {

    /**
     *
     */
    private static final long serialVersionUID = -2530774548488512609L;

    private OWLModelManager owlModelManager;

    //private DefaultMutableTreeNode rootNode;

    private AxiomSorter axiomSorter;

    private OWLOntology currentOntology;

    private Map<Node<OWLLogicalAxiom>, DiagnosesTreeNode> nodeMap;

    private OWLEntity entity;

    private Map<OWLEntity, Set<OWLAxiom>> axiomsByEntityMap;

    // axioms that cannot be indexed by entity
    private Set<OWLAxiom> additionalAxioms = new HashSet<OWLAxiom>();

    private int usageCount;

    private Set<UsageFilter> filters = new HashSet<UsageFilter>();


    public DiagnosesTreeModel(OWLEditorKit owlEditorKit) {
        super(new DiagnosesTreeNode(""));
        owlModelManager = owlEditorKit.getModelManager();
        axiomSorter = new AxiomSorter();
        nodeMap = new HashMap<Node<OWLLogicalAxiom>,DiagnosesTreeNode>();
        axiomsByEntityMap = new TreeMap<OWLEntity, Set<OWLAxiom>>(owlModelManager.getOWLObjectComparator());
    }

    /*public DiagnosesTreeModel(OWLEditorKit owlEditorKit, OWLEntity entity) {
        this(owlEditorKit);
        setOWLEntity(entity);
    }*/

    private String getRootContent(OWLModelManager mngr, OWLEntity entity){
        return entity != null ? "Found " + usageCount + " uses of " + mngr.getRendering(entity) : "";
    }

    public class Entry {
        int num;

        double probability;

        public Entry(int num) {
            this.num = num;
        }
    }

    private HashMap<Collection<OWLLogicalAxiom>,Entry> conflictSetMap;

    public void setConflictSets(Set<Set<OWLLogicalAxiom>> conflictSets, OWLWorkspace owlworkspace) {
        WorkspaceTab workspace = (WorkspaceTab) owlworkspace.getWorkspaceTab("at.ainf.pluginprotege.WorkspaceTab");

        NodeCostsEstimator<OWLLogicalAxiom> es = null;
        if (workspace.getSearch() instanceof UniformCostSearch) {
            es = ((UniformCostSearch<OWLLogicalAxiom>) workspace.getSearch()).getNodeCostsEstimator();
        }

        if (conflictSets == null) {
            conflictSetMap = null; return;
        }

          int number = 1;
        conflictSetMap = new HashMap<Collection<OWLLogicalAxiom>, Entry>();
        for (Set<OWLLogicalAxiom> conflictSet : conflictSets) {
            conflictSetMap.put(conflictSet,new Entry(number));
            if (es != null)
                conflictSetMap.get(conflictSet).probability = es.getNodeSetCosts(conflictSet);
            number++;
        }

    }

    public DiagnosesTreeNode getHeaderConflictSetSibling(DiagnosesTreeNode node) {
        DiagnosesTreeNode result = null;

        if (node.getNode() == null || node.getNode().getParent() == null)
            return null;
        Collection<OWLLogicalAxiom> c = node.getNode().getParent().getConflict();
        if (conflictSetMap == null) {
            return new DiagnosesTreeNode("Conflict Set");
        }
        Entry ent = conflictSetMap.get(c);
        if (  ent==null  )
            return null;
        //if (ent.probability == 0)
          result = new DiagnosesTreeNode("Conflict Set " + ent.num);
          result.setConflictSet(c);
          result.setConflictNumber(ent.num);
        /*else
          result = new DiagnosesTreeNode("Conflict Set " + ent.num + " " + ent.probability);*/

        return result;
    }


    public void setHittingSets(Set<? extends HittingSet<OWLLogicalAxiom>> hs) {

        /*axiomsByEntityMap.clear();
        usageCount = 0;

        for (OWLOntology ont : owlModelManager.getActiveOntologies()) {
            currentOntology = ont;
            Set<OWLAxiom> axioms = ont.getReferencingAxioms(owlEntity);
            for (OWLAxiom ax : axioms) {
                axiomSorter.setAxiom(ax);
                ax.accept(axiomSorter);
            }
        }*/

        root = new DiagnosesTreeNode("");
        setRoot(root);

        if (hs == null) {
            root = new DiagnosesTreeNode("No Diagnoses calculated");
            setRoot(root);
            return;
        }

        for (Object hittingSet : hs) {
            Node<OWLLogicalAxiom> node = ((HittingSetImpl<OWLLogicalAxiom>) hittingSet).getNode();
            Node<OWLLogicalAxiom> parent = node.getParent();

            while (parent != null) {
                if ( parent.getParent() != null ) {
                  if (!getNode(parent).isConflictSetHeaderAdded()) {
                      getNode(parent).add(getHeaderConflictSetSibling(getNode(node)));
                      getNode(parent).setConflictSetHeaderAdded(true);
                  }
                  getNode(parent).add(getNode(node));
                }
                else {
                  if (!((DiagnosesTreeNode)root).isConflictSetHeaderAdded()) {
                      ((DiagnosesTreeNode)root).add(getHeaderConflictSetSibling(getNode(node)));
                      ((DiagnosesTreeNode)root).setConflictSetHeaderAdded(true);
                  }
                  ((DiagnosesTreeNode)root).add(getNode(node));
                }

                node = parent;
                parent = parent.getParent();

            }
        }

        searchTreeForConflictSetHeader ((DiagnosesTreeNode) root);


        /*for (OWLEntity ent : axiomsByEntityMap.keySet()) {
            for (OWLAxiom ax : axiomsByEntityMap.get(ent)) {
                getNode(ent).add(new DiagnosesTreeNode(null, ax));
            }
        }

        if (!additionalAxioms.isEmpty()){
            DefaultMutableTreeNode otherNode = new DefaultMutableTreeNode("Other");
            rootNode.add(otherNode);
            for (OWLAxiom ax : additionalAxioms){
                otherNode.add(new DefaultMutableTreeNode(ax));
            }} */

    }

    private void searchTreeForConflictSetHeader(DiagnosesTreeNode n) {
        for (int i = 0; i < n.getChildCount(); i++) {
            DiagnosesTreeNode child = (DiagnosesTreeNode) n.getChildAt(i);
            if (child.isHeader()) {
                for (OWLLogicalAxiom axiom : child.getConflictSet()) {
                    if (!isAxiomInChild(n,axiom)) {
                       n.add(new DiagnosesTreeNode(axiom));
                    }
                }
            }
            else {
                searchTreeForConflictSetHeader(child);
            }
        }
    }

    public boolean isAxiomInChild(DiagnosesTreeNode node, OWLLogicalAxiom axiom) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DiagnosesTreeNode c = (DiagnosesTreeNode) node.getChildAt(i);
            if (c.getNode() != null && c.getNode().getArcLabel().equals(axiom)) {
                return true;
            }
        }
        return false;
    }


    public void addFilter(UsageFilter filter) {
        filters.add(filter);
    }


    public void addFilters(Set<UsageFilter> filters) {
        this.filters.addAll(filters);
    }


    public void removeFilter(UsageFilter filter) {
        filters.remove(filter);
    }


    private boolean isFilterSet(UsageFilter filter){
        return filters.contains(filter);
    }


    public DiagnosesTreeNode getNode(Node<OWLLogicalAxiom> node) {
        DiagnosesTreeNode n1 = nodeMap.get(node);
        if (n1 == null) {
            n1 = new DiagnosesTreeNode(node);
            nodeMap.put(node, n1);
        }
        return n1;
    }


    /*public void refresh(){
        setOWLEntity(entity);  }*/


    private class AxiomSorter implements OWLAxiomVisitor, OWLEntityVisitor, OWLPropertyExpressionVisitor {

        private OWLAxiom currentAxiom;


        public void setAxiom(OWLAxiom axiom) {
            currentAxiom = axiom;
        }


        private void add(OWLEntity ent) {

            if (isFilterSet(UsageFilter.filterSelf) && entity.equals(ent)) {
                return;
            }
            usageCount++;
            Set<OWLAxiom> axioms = axiomsByEntityMap.get(ent);
            if (axioms == null) {
                axioms = new HashSet<OWLAxiom>();
                axiomsByEntityMap.put(ent, axioms);
            }
            axioms.add(currentAxiom);
        }


        public void visit(OWLClass cls) {
            add(cls);
        }


        public void visit(OWLDatatype dataType) {
            add(dataType);
        }


        public void visit(OWLNamedIndividual individual) {
            add(individual);
        }


        public void visit(OWLDataProperty property) {
            add(property);
        }


        public void visit(OWLObjectProperty property) {
            add(property);
        }


        public void visit(OWLAnnotationProperty property) {
            add(property);
        }


        public void visit(OWLObjectInverseOf property) {
            property.getInverse().accept(this);
        }


        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLAnnotationAssertionAxiom axiom) {
            if (axiom.getSubject() instanceof IRI){
                IRI subjectIRI = (IRI)axiom.getSubject();
                for (OWLOntology ont : owlModelManager.getActiveOntologies()){
                    if (ont.containsClassInSignature(subjectIRI)){
                        add(owlModelManager.getOWLDataFactory().getOWLClass(subjectIRI));
                    }
                    if (ont.containsObjectPropertyInSignature(subjectIRI)){
                        add(owlModelManager.getOWLDataFactory().getOWLObjectProperty(subjectIRI));
                    }
                    if (ont.containsDataPropertyInSignature(subjectIRI)){
                        add(owlModelManager.getOWLDataFactory().getOWLDataProperty(subjectIRI));
                    }
                    if (ont.containsIndividualInSignature(subjectIRI)){
                        add(owlModelManager.getOWLDataFactory().getOWLNamedIndividual(subjectIRI));
                    }
                    if (ont.containsAnnotationPropertyInSignature(subjectIRI)){
                        add(owlModelManager.getOWLDataFactory().getOWLAnnotationProperty(subjectIRI));
                    }
                    if (ont.containsDatatypeInSignature(subjectIRI)){
                        add(owlModelManager.getOWLDataFactory().getOWLDatatype(subjectIRI));
                    }
                }
            }
        }


        public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
            axiom.getSubProperty().accept(this);
        }


        public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLClassAssertionAxiom axiom) {
            if (!axiom.getIndividual().isAnonymous()){
                axiom.getIndividual().asOWLNamedIndividual().accept(this);
            }
        }


        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            if (!axiom.getSubject().isAnonymous()){
                axiom.getSubject().asOWLNamedIndividual().accept(this);
            }
        }


        public void visit(OWLDataPropertyDomainAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLDataPropertyRangeAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLSubDataPropertyOfAxiom axiom) {
            axiom.getSubProperty().accept(this);
        }


        public void visit(OWLDeclarationAxiom axiom) {
            axiom.getEntity().accept(this);
        }


        public void visit(OWLDifferentIndividualsAxiom axiom) {
            for (OWLIndividual ind : axiom.getIndividuals()) {
                if (!ind.isAnonymous()){
                    ind.asOWLNamedIndividual().accept(this);
                }
            }
        }


        public void visit(OWLDisjointClassesAxiom axiom) {
            boolean hasBeenIndexed = false;
            if (!isFilterSet(UsageFilter.filterDisjoints)){
                for (OWLClassExpression desc : axiom.getClassExpressions()) {
                    if (!desc.isAnonymous()) {
                        desc.asOWLClass().accept(this);
                        hasBeenIndexed = true;
                    }
                }
            }
            if (!hasBeenIndexed){
                additionalAxioms.add(axiom);
                usageCount++;
            }
        }


        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
            if (!isFilterSet(UsageFilter.filterDisjoints)){
                for (OWLDataPropertyExpression prop : axiom.getProperties()) {
                    prop.accept(this);
                }
            }
        }


        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            if (!isFilterSet(UsageFilter.filterDisjoints)){
                for (OWLObjectPropertyExpression prop : axiom.getProperties()) {
                    prop.accept(this);
                }
            }
        }


        public void visit(OWLDisjointUnionAxiom axiom) {
            if (!isFilterSet(UsageFilter.filterDisjoints)){
                axiom.getOWLClass().accept(this);
            }
        }


        public void visit(OWLEquivalentClassesAxiom axiom) {
            boolean hasBeenIndexed = false;
            for (OWLClassExpression desc : axiom.getClassExpressions()) {
                if (!desc.isAnonymous()) {
                    desc.asOWLClass().accept(this);
                    hasBeenIndexed = true;
                }
            }
            if (!hasBeenIndexed){
                additionalAxioms.add(axiom);
                usageCount++;
            }
        }


        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            for (OWLDataPropertyExpression prop : axiom.getProperties()) {
                prop.accept(this);
            }
        }


        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            for (OWLObjectPropertyExpression prop : axiom.getProperties()) {
                prop.accept(this);
            }
        }


        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            for (OWLObjectPropertyExpression prop : axiom.getProperties()) {
                prop.accept(this);
            }
        }


        public void visit(OWLHasKeyAxiom axiom) {
            //@@TODO implement
        }


        public void visit(OWLDatatypeDefinitionAxiom axiom) {
            axiom.getDatatype().accept(this);
        }


        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
            if (!axiom.getSubject().isAnonymous()){
                axiom.getSubject().asOWLNamedIndividual().accept(this);
            }
        }


        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
            if (!axiom.getSubject().isAnonymous()){
                axiom.getSubject().asOWLNamedIndividual().accept(this);
            }
        }


        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            if (!axiom.getSubject().isAnonymous()){
                axiom.getSubject().asOWLNamedIndividual().accept(this);
            }
        }


        public void visit(OWLSubPropertyChainOfAxiom axiom) {
            axiom.getSuperProperty().accept(this);
        }


        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
            axiom.getSubProperty().accept(this);
        }


        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLSameIndividualAxiom axiom) {
            for (OWLIndividual ind : axiom.getIndividuals()) {
                if (!ind.isAnonymous()){
                    ind.asOWLNamedIndividual().accept(this);
                }
            }
        }


        public void visit(OWLSubClassOfAxiom axiom) {
            if (!axiom.getSubClass().isAnonymous()) {
                if (!isFilterSet(UsageFilter.filterNamedSubsSupers) ||
                    (!axiom.getSubClass().equals(entity) && !axiom.getSuperClass().equals(entity))){
                    axiom.getSubClass().asOWLClass().accept(this);
                }
            }
            else{
                additionalAxioms.add(axiom);
                usageCount++;
            }
        }


        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }


        public void visit(SWRLRule rule) {

        }
    }

}
