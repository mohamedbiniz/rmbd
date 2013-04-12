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
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;

/**
 * This class is taken from LogMap and was written by Ernesto Jimenez Ruiz
 *
 * Repairs a bugs in StructuralReasoner.getDisjointClasses
 * @author ernesto
 *
 */
public class ExtendedStructuralReasoner extends StructuralReasoner {

    public ExtendedStructuralReasoner(OWLOntology rootOntology) {
        super(rootOntology,  new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
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

    public String getReasonerName(){
        return "Extended Structural Reasoner";
    }
}

