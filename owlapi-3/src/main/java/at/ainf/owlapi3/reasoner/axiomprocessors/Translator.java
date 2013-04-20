package at.ainf.owlapi3.reasoner.axiomprocessors;

import org.sat4j.specs.IVecInt;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 18.04.13
 * Time: 21:59
 * To change this template use File | Settings | File Templates.
 */
public interface Translator<T> {

    T visit(OWLDisjointClassesAxiom axiom);

    T visit(OWLDisjointUnionAxiom axiom);

    T visit(OWLEquivalentClassesAxiom axiom);

    T visit(OWLSubClassOfAxiom axiom);
}
