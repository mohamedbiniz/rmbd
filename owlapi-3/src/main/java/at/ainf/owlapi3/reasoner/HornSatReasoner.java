package at.ainf.owlapi3.reasoner;

import at.ainf.owlapi3.reasoner.axiomprocessors.OWL2SATTranslator;
import at.ainf.owlapi3.reasoner.axiomprocessors.OWLClassAxiomNegation;
import at.ainf.owlapi3.reasoner.axiomprocessors.Translator;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLReasonerBase;
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
    private Set<OWLClass> unSatClasses = new HashSet<OWLClass>();
    private Boolean sat = null;
    private int maxIndex = 1;

    public HornSatReasoner(OWLOntology ontology) {
        this(ontology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
    }

    public HornSatReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering) {
        super(ontology, config, buffering);
        processAxioms(getReasonerAxioms());
    }

    public Set<OWLClass> getUnSatClasses() {
        return Sets.intersection(unSatClasses, getRootOntology().getClassesInSignature());
    }

    public void setUnSatClasses(Set<OWLClass> unSatClasses) {
        this.unSatClasses = unSatClasses;
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
    public boolean isEntailed(OWLAxiom axiom) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException, AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        if (!isEntailmentCheckingSupported(axiom.getAxiomType()))
            throw new UnsupportedEntailmentTypeException(axiom);
        if (!isConsistent())
            throw new InconsistentOntologyException();
        if (!axiom.getDataPropertiesInSignature().isEmpty() || !axiom.getObjectPropertiesInSignature().isEmpty())
            throw new UnsupportedEntailmentTypeException(axiom);

        OWLClassExpression negation = processAxiom(axiom, new OWLClassAxiomNegation(this));
        return !isSatisfiable(negation);
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException, AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        for (OWLAxiom ax : axioms) {
            if (!isEntailed(ax))
                return false;
        }
        return true;
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() throws ReasonerInterruptedException, TimeOutException {
        Set<OWLClass> unSat = new HashSet<OWLClass>();
        for (OWLClass owlClass : getRootOntology().getClassesInSignature()) {
            if (!isSatisfiable(owlClass))
                unSat.add(owlClass);
        }
        return new OWLClassNode(unSat);
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) throws ReasonerInterruptedException, TimeOutException, ClassExpressionNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        Set<IConstr> iConstr = new HashSet<IConstr>();
        try {
            Set<IVecInt> iVecInt = getiVecInt(classExpression);
            for (IVecInt constr : iVecInt) {
                IConstr cons = solver.addClause(constr);
                iConstr.add(cons);
            }

            if (!solver.isSatisfiable())
                return false;

        } catch (ContradictionException e) {
            return false;
        } catch (TimeoutException e) {
            throw new TimeOutException();
        } finally {
            for (IConstr constr : iConstr) {
                if (constr != null)
                    solver.removeConstr(constr);
            }
        }
        return true;
    }

    @Override
    public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
        if (this.sat != null)
            return this.sat;

        // verify unsatisfiable classes
        // if no classes are given then the solver verifies all classes in the signature
        Set<OWLClass> classes = getUnSatClasses();
        if (classes.isEmpty())
            classes = getRootOntology().getClassesInSignature();

        for (OWLClass owlClass : classes) {
            if (!isSatisfiable(owlClass)) {
                sat = false;
                return sat;
            }
        }
        sat = true;
        return sat;
    }

    @Override
    protected void handleChanges(Set<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
        super.handleChanges(addAxioms, removeAxioms);
        //processAxioms(getRootOntology().getAxioms());
        processAxioms(getReasonerAxioms());
    }

    private void processAxioms(Collection<OWLAxiom> axioms) {
        // clean up the solver instance
        this.sat = null;
        solver.reset();
        getSolverClauses().clear();

        for (OWLAxiom axiom : axioms) {
            processAxiom(axiom, new OWL2SATTranslator(this));
            getSolverClauses().addAll(getTranslations().get(axiom));
        }
        //solver.newVar(getNumberOfVariables(getSolverClauses()));
        //solver.setExpectedNumberOfClauses(getSolverClauses().size());
        try {
            sync();
        } catch (ContradictionException e) {
            this.sat = false;
        }
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return axiomType == AxiomType.SUBCLASS_OF || axiomType == AxiomType.EQUIVALENT_CLASSES ||
                axiomType == AxiomType.DISJOINT_UNION || axiomType == AxiomType.DISJOINT_CLASSES;
    }

    public <T> T processAxiom(OWLAxiom axiom, Translator<T> translator) {
        if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF)
            return translator.visit((OWLSubClassOfAxiom) axiom);
        else if (axiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES)
            return translator.visit((OWLEquivalentClassesAxiom) axiom);
        else if (axiom.getAxiomType() == AxiomType.DISJOINT_UNION)
            return translator.visit((OWLDisjointUnionAxiom) axiom);
        else if (axiom.getAxiomType() == AxiomType.DISJOINT_CLASSES)
            return translator.visit((OWLDisjointClassesAxiom) axiom);
        //else if (axiom.getAxiomType() == AxiomType.CLASS_ASSERTION)
        //    return translator.visit((OWLClassAssertionAxiom) axiom);
        return null;
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


    public int getIndex(OWLClassExpression expr) {
        if (!expr.isClassExpressionLiteral())
            throw new RuntimeException("Only literals are a part of an index! " + expr);

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
            throw new RuntimeException("Adding a key that already exists! " + cl);
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

    public Set<IVecInt> getiVecInt(OWLClassExpression clause) {
        Set<IVecInt> clauses = new HashSet<IVecInt>();
        for (OWLClassExpression conj : clause.asConjunctSet()) {
            IVecInt satClause = new VecInt();
            for (OWLClassExpression expr : conj.asDisjunctSet()) {
                // ignore all restrictions an put only literals including classes
                if (expr.isClassExpressionLiteral())
                    satClause.push(getIndex(expr));
            }
            if (!satClause.isEmpty())
                clauses.add(satClause);
        }
        return clauses;
    }

    public Set<OWLClassExpression> convertToCNF(OWLClassExpression fl) {
        if (isDisjunctionOfLiterals(fl, false)) return Collections.singleton(fl);

        // apply distribution to non-unary disjunctions
        if (fl.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
            Set<OWLClassExpression> disjuncs = fl.asDisjunctSet();
            if (disjuncs.isEmpty())
                return Collections.emptySet();
            if (disjuncs.size() == 1)
                return convertToCNF(disjuncs.iterator().next());
            OWLClassExpression conj = null, cl2 = null;
            Set<OWLClassExpression> disj = new HashSet<OWLClassExpression>();
            for (OWLClassExpression cl : disjuncs) {
                if (conj == null && cl.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
                    conj = cl;
                } else if (cl2 == null) cl2 = cl;
                else
                    disj.add(cl);
            }

            if (conj == null) throw new RuntimeException("No conjunction for distribution! " + fl);

            Set<OWLClassExpression> newConj = new HashSet<OWLClassExpression>();
            for (OWLClassExpression c : conj.asConjunctSet()) {
                OWLClassExpression newClause = getOWLDataFactory().getOWLObjectUnionOf(cl2, c);
                Set<OWLClassExpression> exprs = convertToCNF(newClause);
                newConj.addAll(exprs);
            }

            // return single conjunction as a set of clauses
            if (disj.isEmpty())
                return newConj;

            disj.add(getOWLDataFactory().getOWLObjectIntersectionOf(newConj));

            // add to a source disjunction replacing two selected conjunctions
            OWLClassExpression expr = getOWLDataFactory().getOWLObjectUnionOf(disj);
            return convertToCNF(expr);
        } else {
            // verify whether we have a CNF
            Set<OWLClassExpression> cnf = new HashSet<OWLClassExpression>();
            for (OWLClassExpression expr : fl.asConjunctSet()) {
                cnf.addAll(convertToCNF(expr));
            }
            return cnf;
        }

    }

    public boolean isDisjunctionOfLiterals(OWLClassExpression fl, boolean rejectRestrictions) {
        if (fl.isClassExpressionLiteral())
            return true;
        if (fl.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
            for (OWLClassExpression expr : fl.asDisjunctSet()) {
                if (!expr.isClassExpressionLiteral() && (rejectRestrictions || !(expr instanceof OWLRestriction)))
                    return false;
            }
        } else return false;
        return true;
    }
}
