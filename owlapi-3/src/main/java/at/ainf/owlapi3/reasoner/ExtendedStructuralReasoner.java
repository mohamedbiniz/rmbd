package at.ainf.owlapi3.reasoner;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.02.13
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;

import java.util.Set;

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
                        if (!op.isAnonymous() && !op.equals(ce)) { //Op must be differnt to ce
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
        for (OWLClass owlClass : getRootOntology().getClassesInSignature()) {
            if (getEquivalentClasses(owlClass).contains(getDataFactory().getOWLNothing()))
                return false;
            boolean isSat = isSatisfiable(owlClass.getEquivalentClasses(getRootOntology()))
                    && isSatisfiable(owlClass.getSuperClasses(getRootOntology()));
            if (!isSat) return false;
        }
        return true;
    }

    private boolean isSatisfiable(Set<OWLClassExpression> classExpressions) {
        for (OWLClassExpression classExpression : classExpressions) {
            if (!isSatisfiable(classExpression)) return false;
        }
        return true;
    }
}

