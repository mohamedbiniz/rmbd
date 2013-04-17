package at.ainf.owlapi3.reasoner;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.util.Version;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 11.04.13
 * Time: 23:20
 * To change this template use File | Settings | File Templates.
 */
public class HornSatReasoner extends StructuralReasoner {


    private final Map<OWLClass, Integer> index = new HashMap<OWLClass, Integer>();
    private final Multimap<OWLAxiom, IVecInt> translations = HashMultimap.create();
    private final ISolver solver = SolverFactory.newDefault();
    private final Set<IVecInt> solverClauses = new HashSet<IVecInt>();
    private Object solverAxioms;
    private Boolean sat = null;
    private int maxIndex = 1;

    public HornSatReasoner(OWLOntology ontology) {
        this(ontology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
    }

    public HornSatReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering) {
        super(ontology, config, buffering);
    }

    @Override
    public String getReasonerName() {
        return "SAT Reasoner for OWL";
    }

    @Override
    public Version getReasonerVersion() {
        return new Version(1, 0, 0, 0);
    }


    @Override
    public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
        if (this.sat != null)
            return this.sat;
        try {
            //NodeSet<OWLClass> classes = getSubClasses(getDataFactory().getOWLThing(), true);
            //for (OWLClass owlClass : classes.getFlattened()) {
            for (OWLClass owlClass : getRootOntology().getClassesInSignature()) {
                IConstr iConstr = null;
                try {
                    iConstr = solver.addClause(getiVecInt(owlClass));
                } catch (ContradictionException e) {
                    return false;
                }
                if (!solver.isSatisfiable())
                    return false;

               if (iConstr != null)
                   solver.removeConstr(iConstr);
            }
            return true;
        } catch (TimeoutException e) {
            throw new TimeOutException();
        }
    }


    @Override
    protected void handleChanges(Set<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
        super.handleChanges(addAxioms, removeAxioms);
        processAxioms(getRootOntology().getAxioms());
    }

    private void processAxioms(Set<OWLAxiom> axioms) {
        getSolverClauses().clear();
        for (OWLAxiom axiom : axioms) {
            if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF)
                processAxiom((OWLSubClassOfAxiom) axiom);
            else if (axiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES)
                processAxiom((OWLEquivalentClassesAxiom) axiom);
            else if (axiom.getAxiomType() == AxiomType.DISJOINT_UNION)
                processAxiom((OWLDisjointUnionAxiom) axiom);
            else if (axiom.getAxiomType() == AxiomType.DISJOINT_CLASSES)
                processAxiom((OWLDisjointClassesAxiom) axiom);

            getSolverClauses().addAll(getTranslations().get(axiom));
        }

        this.sat = null;
        solver.reset();
        //solver.newVar(getNumberOfVariables(getSolverClauses()));
        //solver.setExpectedNumberOfClauses(getSolverClauses().size());
        try {
            sync();
        } catch (ContradictionException e) {
            this.sat = false;
        }
    }

    private int getNumberOfVariables(Set<IVecInt> solverClauses) {
        Set<Integer> var = new HashSet<Integer>();
        for (IVecInt solverClause : solverClauses) {
            for (IteratorInt iterator = solverClause.iterator(); iterator.hasNext(); ) {
                int i = iterator.next();
                if (i < 0) i *= -1;
                var.add(i);
            }
        }
        return var.size();
    }

    private void sync() throws ContradictionException {
        this.sat = null;
        for (IVecInt cl : getSolverClauses()) {
            solver.addClause(cl);
        }
    }

    private Collection<IVecInt> processAxiom(OWLDisjointClassesAxiom axiom) {
        if (!getTranslations().containsKey(axiom)) {
            for (OWLDisjointClassesAxiom pair : axiom.asPairwiseAxioms()) {
                IVecInt clause = new VecInt(2);
                for (OWLClassExpression expr : pair.getClassExpressions()) {
                    if (!expr.isClassExpressionLiteral())
                        throw new RuntimeException("Not a literal in a pairwise disjoint axiom!");

                    clause.push(-1 * getIndex(expr));
                }
                getTranslations().put(axiom, clause);
            }
        }
        return getTranslations().get(axiom);
    }

    private void processAxiom(OWLDisjointUnionAxiom axiom) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void processAxiom(OWLEquivalentClassesAxiom axiom) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private Collection<IVecInt> processAxiom(OWLSubClassOfAxiom axiom) {
        if (!getTranslations().containsKey(axiom)) {
            // remove implication by transforming to a disjunction
            OWLClassExpression subCl = axiom.getSubClass().getComplementNNF();
            OWLClassExpression superCl = axiom.getSuperClass().getNNF();
            OWLObjectUnionOf fl = getOWLDataFactory().getOWLObjectUnionOf(
                    subCl,
                    superCl);

            // convert to CNF
            Set<OWLClassExpression> clauses = convertToCNF(fl);

            // create DIMACS clauses
            for (OWLClassExpression clause : clauses) {
                if (!isDisjunctionOfLiterals(clause, true))
                    continue;
                IVecInt satClause = getiVecInt(clause);

                // ignore facts, which are impossible in this reasoner without proper grounding
                if (satClause.size() > 1) // && isHornClause(satClause)
                    getTranslations().put(axiom, satClause);
            }
        }
        return getTranslations().get(axiom);
    }

    private IVecInt getiVecInt(OWLClassExpression clause) {
        IVecInt satClause = new VecInt();
        for (OWLClassExpression expr : clause.asDisjunctSet()) {
            // ignore all restrictions an put only literals including classes
            if (expr.isClassExpressionLiteral())
                satClause.push(getIndex(expr));
        }
        return satClause;
    }

    private Set<OWLClassExpression> convertToCNF(OWLClassExpression fl) {
        if (isDisjunctionOfLiterals(fl, false)) return Collections.singleton(fl);

        // apply distribution
        if (fl.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
            OWLClassExpression conj = null, cl2 = null;
            Set<OWLClassExpression> disj = new HashSet<OWLClassExpression>();
            for (OWLClassExpression cl : fl.asDisjunctSet()) {
                if (conj == null && cl.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
                    conj = cl;
                } else if (cl2 == null) cl2 = cl;
                else
                    disj.add(cl);
            }

            if (conj == null) throw new RuntimeException("No conjunction for distribution!");

            Set<OWLClassExpression> newConj = new HashSet<OWLClassExpression>();
            for (OWLClassExpression c : conj.asConjunctSet()) {
                OWLClassExpression newClause = getDataFactory().getOWLObjectUnionOf(cl2, c);
                newConj.add(newClause);
            }

            // return single conjunction as a set of clauses
            if (disj.isEmpty())
                return newConj;

            disj.add(getDataFactory().getOWLObjectIntersectionOf(newConj));

            // add to a source disjunction replacing two selected conjunctions
            OWLClassExpression expr = getDataFactory().getOWLObjectUnionOf(disj);
            return Collections.singleton(expr);
        } else {
            // verify whether we have a CNF
            Set<OWLClassExpression> cnf = new HashSet<OWLClassExpression>();
            for (OWLClassExpression expr : fl.asConjunctSet()) {
                cnf.addAll(convertToCNF(expr));
            }
            return cnf;
        }

    }

    private boolean isDisjunctionOfLiterals(OWLClassExpression fl, boolean rejectRestrictions) {
        if (fl.isClassExpressionLiteral())
            return true;
        if (fl.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
            for (OWLClassExpression expr : fl.asDisjunctSet()) {
                if (!expr.isClassExpressionLiteral() || (rejectRestrictions && expr instanceof OWLRestriction))
                    return false;
            }
        } else return false;
        return true;
    }


    private int getIndex(OWLClassExpression expr) {
        if (!expr.isClassExpressionLiteral())
            throw new RuntimeException("Only literals are a part of an index!");

        if (!expr.isAnonymous()) {
            OWLClass cl = expr.asOWLClass();
            if (getIndex().containsKey(cl))
                return getIndex().get(cl);
            else
                return addToIndex(cl);
        }
        OWLClass cl = ((OWLObjectComplementOf) expr).getOperand().asOWLClass();
        if (getIndex().containsKey(cl))
            return -1 * getIndex().get(cl);
        else
            return -1 * addToIndex(cl);
    }

    private int addToIndex(OWLClass cl) {
        int value = maxIndex++;
        if (getIndex().containsKey(cl))
            throw new RuntimeException("Adding a key that already exists!");
        getIndex().put(cl, value);
        return value;
    }

    protected Map<OWLClass, Integer> getIndex() {
        return index;
    }

    protected Set<IVecInt> getSolverClauses() {
        return solverClauses;
    }

    public Multimap<OWLAxiom, IVecInt> getTranslations() {
        return translations;
    }
}
