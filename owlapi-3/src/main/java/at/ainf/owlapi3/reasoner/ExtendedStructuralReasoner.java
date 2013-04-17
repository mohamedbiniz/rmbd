package at.ainf.owlapi3.reasoner;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.02.13
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

//import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;

/**
 * This class is taken from LogMap and was written by Ernesto Jimenez Ruiz
 * <p/>
 * Repairs a bugs in StructuralReasoner.getDisjointClasses
 *
 * @author ernesto
 */
public class ExtendedStructuralReasoner extends StructuralReasoner {

    public ExtendedStructuralReasoner(OWLOntology rootOntology) {
        this(rootOntology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
    }

    public ExtendedStructuralReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering) {
        super(ontology, config, buffering);
    }

    /**
     * It was an error in original method. the result set contained both the given class and its equivalents.
     */
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        //super.ensurePrepared();
        OWLClassNodeSet nodeSet = new OWLClassNodeSet();
        if (!ce.isAnonymous()) {
            for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
                for (OWLDisjointClassesAxiom ax : ontology.getDisjointClassesAxioms(ce.asOWLClass())) {
                    for (OWLClassExpression op : ax.getClassExpressions()) {
                        if (isLiteral(op) && !op.equals(ce)) { //Op must be differnt to ce
                            nodeSet.addNode(getEquivalentClasses(op));
                        }
                    }
                }
            }
        }


        return nodeSet;
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
        return "Extended Structural Reasoner";
    }

    @Override
    public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
        return isCoherent();
    }

    public boolean isCoherent() throws ReasonerInterruptedException, TimeOutException {
        NodeSet<OWLClass> nodes = getSubClasses(getTopClassNode().getRepresentativeElement(), false);
        for (Node<OWLClass> node : nodes) {
            NodeSet<OWLClass> sc = getSuperClasses(node.getRepresentativeElement(), false);
            Set<OWLClassExpression> sb = getSubClassesExpressions(node.getRepresentativeElement(), false);
            NodeSet<OWLClass> dc = getDisjointClasses(node.getRepresentativeElement());


            System.out.println("------------" + node + "----------------");
            System.out.println(sc);
            System.out.println(sb);
            System.out.println(dc);
        }

        for (OWLClass owlClass : getRootOntology().getClassesInSignature()) {
            boolean isSat = isSatisfiable(getSuperClasses(owlClass), owlClass)
                    && isSatisfiable(getSuperClassesExpressions(owlClass, false), owlClass)
                    ;
            if (!isSat) return false;
        }
        return true;
    }

    public Set<OWLClassExpression> getSuperClassesExpressions(OWLClassExpression ce, boolean direct) throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        Set<OWLClassExpression> ns = new HashSet<OWLClassExpression>();
        if (!ce.isAnonymous()) {
            ensurePrepared();

            for (Node<OWLClass> owlClasses : classHierarchyInfo.getNodeHierarchyParents(ce.asOWLClass(), direct, new OWLClassNodeSet())) {
                ns.addAll(owlClasses.getEntities());
            }
            ns.addAll(classHierarchyInfo.getRawParentChildProvider().getParentExpressions(ce.asOWLClass()));
        }
        return ns;
    }


    public Set<OWLClassExpression> getSubClassesExpressions(OWLClassExpression ce, boolean direct) throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
        Set<OWLClassExpression> ns = new HashSet<OWLClassExpression>();
        if (!ce.isAnonymous()) {
            ensurePrepared();

            for (Node<OWLClass> owlClasses : classHierarchyInfo.getNodeHierarchyChildren(ce.asOWLClass(), direct, new OWLClassNodeSet())) {
                ns.addAll(owlClasses.getEntities());
            }
            ns.addAll(classHierarchyInfo.getRawParentChildProvider().getChildrenExpressions(ce.asOWLClass()));
        }
        return ns;
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

    private Set<OWLClassExpression> getSuperClasses(OWLClass owlClass) {
        Set<OWLClassExpression> superClasses = owlClass.getSuperClasses(getRootOntology());
        LinkedList<OWLClassExpression> open = new LinkedList<OWLClassExpression>(superClasses);
        while (!open.isEmpty()) {
            OWLClassExpression exp = open.poll();
            if (exp.isAnonymous())
                continue;
            open.addAll(exp.asOWLClass().getSuperClasses(getRootOntology()));
            superClasses.addAll(exp.asOWLClass().getSuperClasses(getRootOntology()));
        }
        return superClasses;
    }

    private boolean isSatisfiable(Set<OWLClassExpression> classExpressions, OWLClass cls) {
        for (OWLClassExpression classExpression : classExpressions) {
            if (!isSatisfiable(classExpression)) return false;
        }
        return true;
    }

}

