package at.ainf.owlapi3.reasoner.axiomprocessors;

import at.ainf.owlapi3.reasoner.OWLSatReasoner;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OWL2SATTranslator implements Translator<Collection<IVecInt>> {

    private final OWLSatReasoner reasoner;
    private static Logger logger = LoggerFactory.getLogger(OWL2SATTranslator.class.getName());

    public OWL2SATTranslator(OWLSatReasoner reasoner) {
        this.reasoner = reasoner;
    }

    @Override
    public Collection<IVecInt> visit(OWLDisjointClassesAxiom axiom) {
        if (!reasoner.hasTranslations(axiom)) {
            for (OWLDisjointClassesAxiom pair : axiom.asPairwiseAxioms()) {
                IVecInt clause = new VecInt(2);
                for (OWLClassExpression expr : pair.getClassExpressions()) {
                    if (!expr.isClassExpressionLiteral())
                        throw new RuntimeException("Not a literal in a pairwise disjoint axiom! " + axiom);

                    clause.push(-1 * reasoner.getIndex(expr));
                }
                reasoner.addTranslation(axiom, clause);
            }
        }
        return reasoner.getTranslations(axiom);
    }

    @Override
    public Collection<IVecInt> visit(OWLDisjointUnionAxiom axiom) {
        if (!reasoner.hasTranslations(axiom)) {
            Collection<IVecInt> impl1 = visit(axiom.getOWLDisjointClassesAxiom());
            Collection<IVecInt> impl2 = visit(axiom.getOWLEquivalentClassesAxiom());
            reasoner.addTranslations(axiom,  impl1);
            reasoner.addTranslations(axiom,  impl2);
        }
        return reasoner.getTranslations(axiom);
    }

    @Override
    public Collection<IVecInt> visit(OWLEquivalentClassesAxiom axiom) {
        if (!reasoner.hasTranslations(axiom)) {
            List<OWLClassExpression> expr = axiom.getClassExpressionsAsList();
            if (expr.size() != 2) {
                logger.warn("Ignoring equivalence axiom with number of class expressions != 2 " + axiom);
                return null;
            }
            if (expr.contains(getDataFactory().getOWLThing()) || expr.contains(getDataFactory().getOWLNothing())) {
                logger.warn("Ignoring equivalence axiom contains Top or Bottom! " + axiom);
                return null;
            }
            OWLClassExpression cl1 = expr.get(0);
            OWLClassExpression cl2 = expr.get(1);
            Collection<IVecInt> impl1 = visit(getDataFactory().getOWLSubClassOfAxiom(cl1, cl2));
            Collection<IVecInt> impl2 = visit(getDataFactory().getOWLSubClassOfAxiom(cl2, cl1));
            reasoner.addTranslations(axiom,  impl1);
            reasoner.addTranslations(axiom,  impl2);
        }
        return reasoner.getTranslations(axiom);
    }

    private OWLDataFactory getDataFactory() {
        return reasoner.getOWLDataFactory();
    }

    @Override
    public Collection<IVecInt> visit(OWLSubClassOfAxiom axiom) {
        if (!reasoner.hasTranslations(axiom)) {
            // remove implication by transforming to a disjunction
            OWLClassExpression subCl = axiom.getSubClass().getComplementNNF();
            OWLClassExpression superCl = axiom.getSuperClass().getNNF();
            OWLObjectUnionOf fl = getDataFactory().getOWLObjectUnionOf(
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
                        reasoner.addTranslation(axiom, satClause);
                }


            }
        }
        return reasoner.getTranslations(axiom);
    }

    @Override
    public Collection<IVecInt> visit(OWLClassAssertionAxiom axiom) {
        if (!reasoner.hasTranslations(axiom)) {
            OWLClassExpression expr = axiom.getClassExpression();
            if (expr.isOWLThing())
                return Collections.emptySet();
            if (expr.isOWLNothing())
                throw new RuntimeException("Individual is of type Nothing! " + axiom);
            Set<IVecInt> satClauses = reasoner.getiVecInt(expr);
            reasoner.addTranslations(axiom,  satClauses);
        }
        return reasoner.getTranslations(axiom);
    }
}