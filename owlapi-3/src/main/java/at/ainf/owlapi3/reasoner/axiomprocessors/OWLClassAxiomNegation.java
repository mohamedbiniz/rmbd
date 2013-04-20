package at.ainf.owlapi3.reasoner.axiomprocessors;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 18.04.13
 * Time: 22:02
 * To change this template use File | Settings | File Templates.
 */
public class OWLClassAxiomNegation implements Translator<OWLClassExpression> {

    private final HornSatReasoner reasoner;

    public OWLClassAxiomNegation(HornSatReasoner reasoner) {
        this.reasoner = reasoner;
    }

    @Override
    public OWLClassExpression visit(OWLDisjointClassesAxiom axiom) {
        OWLObjectIntersectionOf conjAxiom = asConjunction(axiom);
        Set<OWLClassExpression> exprs = reasoner.convertToCNF(conjAxiom.getComplementNNF());
        return getDataFactory().getOWLObjectIntersectionOf(exprs);
    }

    private OWLObjectIntersectionOf asConjunction(OWLDisjointClassesAxiom axiom) {
        Set<OWLClassExpression> conjunction = new HashSet<OWLClassExpression>();
        for (OWLDisjointClassesAxiom dc : axiom.asPairwiseAxioms()) {
            Set<OWLClassExpression> disjunction = new HashSet<OWLClassExpression>();
            for (OWLClassExpression expr : dc.getClassExpressions()) {
                disjunction.add(expr.getComplementNNF());
            }
            conjunction.add(getDataFactory().getOWLObjectUnionOf(disjunction));
        }
        return getDataFactory().getOWLObjectIntersectionOf(conjunction);
    }

    @Override
    public OWLClassExpression visit(OWLDisjointUnionAxiom axiom) {
        OWLObjectIntersectionOf conjunction1 = asConjunction(axiom.getOWLDisjointClassesAxiom());
        OWLObjectIntersectionOf conjunction2 = asConjunction(axiom.getOWLEquivalentClassesAxiom());
        OWLClassExpression nnf = getDataFactory().getOWLObjectIntersectionOf(conjunction1, conjunction2).getComplementNNF();
        Set<OWLClassExpression> exprs = reasoner.convertToCNF(nnf);
        return getDataFactory().getOWLObjectIntersectionOf(exprs);
    }

    @Override
    public OWLClassExpression visit(OWLEquivalentClassesAxiom axiom) {
        OWLObjectIntersectionOf equivalence = asConjunction(axiom);

        Set<OWLClassExpression> exprs = reasoner.convertToCNF(equivalence.getComplementNNF());
        return getDataFactory().getOWLObjectIntersectionOf(exprs);
    }

    private OWLObjectIntersectionOf asConjunction(OWLEquivalentClassesAxiom axiom) {
        Set<OWLSubClassOfAxiom> subClasses = axiom.asOWLSubClassOfAxioms();
        if (subClasses.size() != 2)
            throw new RuntimeException("Equivalence results not in to implications! " + axiom);
        Iterator<OWLSubClassOfAxiom> iterator = subClasses.iterator();
        OWLClassExpression disj1 = asDisjunction(iterator.next()).getNNF();
        OWLClassExpression disj2 = asDisjunction(iterator.next()).getNNF();
        return getDataFactory().getOWLObjectIntersectionOf(disj1, disj2);
    }

    @Override
    public OWLClassExpression visit(OWLSubClassOfAxiom axiom) {
        OWLObjectUnionOf disj = asDisjunction(axiom);
        Set<OWLClassExpression> exprs = reasoner.convertToCNF(disj.getComplementNNF());
        return getDataFactory().getOWLObjectIntersectionOf(exprs);
    }

    private OWLObjectUnionOf asDisjunction(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();
        return getDataFactory().
                getOWLObjectUnionOf(getDataFactory().getOWLObjectComplementOf(subClass), superClass);
    }

    private OWLDataFactory getDataFactory() {
        return reasoner.getOWLDataFactory();
    }
}
