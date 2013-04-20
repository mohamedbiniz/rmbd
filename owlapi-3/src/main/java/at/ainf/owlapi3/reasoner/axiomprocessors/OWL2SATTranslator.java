package at.ainf.owlapi3.reasoner.axiomprocessors;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public class OWL2SATTranslator implements Translator<Collection<IVecInt>> {

    private final HornSatReasoner reasoner;

    public OWL2SATTranslator(HornSatReasoner reasoner){
        this.reasoner = reasoner;
    }

    @Override
    public Collection<IVecInt> visit(OWLDisjointClassesAxiom axiom) {
        if (!reasoner.getTranslations().containsKey(axiom)) {
            for (OWLDisjointClassesAxiom pair : axiom.asPairwiseAxioms()) {
                IVecInt clause = new VecInt(2);
                for (OWLClassExpression expr : pair.getClassExpressions()) {
                    if (!expr.isClassExpressionLiteral())
                        throw new RuntimeException("Not a literal in a pairwise disjoint axiom! " + axiom);

                    clause.push(-1 * reasoner.getIndex(expr));
                }
                reasoner.getTranslations().put(axiom, clause);
            }
        }
        return reasoner.getTranslations().get(axiom);
    }

    @Override
    public Collection<IVecInt> visit(OWLDisjointUnionAxiom axiom) {
        if (!reasoner.getTranslations().containsKey(axiom)) {
            Collection<IVecInt> impl1 = visit(axiom.getOWLDisjointClassesAxiom());
            Collection<IVecInt> impl2 = visit(axiom.getOWLEquivalentClassesAxiom());
            reasoner.getTranslations().putAll(axiom, impl1);
            reasoner.getTranslations().putAll(axiom, impl2);
        }
        return reasoner.getTranslations().get(axiom);
    }

    @Override
    public Collection<IVecInt> visit(OWLEquivalentClassesAxiom axiom) {
        if (!reasoner.getTranslations().containsKey(axiom)) {
            List<OWLClassExpression> expr = axiom.getClassExpressionsAsList();
            if (expr.size() != 2)
                throw new RuntimeException("Equivalence axiom with number of class expressions != 2 " + axiom);

            OWLClassExpression cl1 = expr.get(0);
            OWLClassExpression cl2 = expr.get(1);
            Collection<IVecInt> impl1 = visit(reasoner.getOWLDataFactory().getOWLSubClassOfAxiom(cl1, cl2));
            Collection<IVecInt> impl2 = visit(reasoner.getOWLDataFactory().getOWLSubClassOfAxiom(cl2, cl1));
            reasoner.getTranslations().putAll(axiom, impl1);
            reasoner.getTranslations().putAll(axiom, impl2);
        }
        return reasoner.getTranslations().get(axiom);
    }

    @Override
    public Collection<IVecInt> visit(OWLSubClassOfAxiom axiom) {
        if (!reasoner.getTranslations().containsKey(axiom)) {
            // remove implication by transforming to a disjunction
            OWLClassExpression subCl = axiom.getSubClass().getComplementNNF();
            OWLClassExpression superCl = axiom.getSuperClass().getNNF();
            OWLObjectUnionOf fl = reasoner.getOWLDataFactory().getOWLObjectUnionOf(
                    subCl,
                    superCl);

            // convert to CNF
            Set<OWLClassExpression> clauses = reasoner.convertToCNF(fl);

            // create DIMACS clauses
            for (OWLClassExpression clause : clauses) {
                if (!reasoner.isDisjunctionOfLiterals(clause, true))
                    continue;
                Set<IVecInt> satClauses = reasoner.getiVecInt(clause);

                // ignore facts, which are impossible in this reasoner without proper grounding
                for (IVecInt satClause : satClauses) {
                    if (satClause.size() > 1) // && isHornClause(satClauses)
                        reasoner.getTranslations().put(axiom, satClause);
                }


            }
        }
        return reasoner.getTranslations().get(axiom);
    }
}