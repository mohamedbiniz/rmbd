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
            solver.isSatisfiable();
        } catch (TimeoutException e) {
            throw new TimeOutException();
        }

        return true;
    }

    @Override
    protected void handleChanges(Set<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
        super.handleChanges(addAxioms, removeAxioms);
        processAxioms(addAxioms, true);
    }

    private void processAxioms(Set<OWLAxiom> axioms, boolean add) {
        for (OWLAxiom axiom : axioms) {
            if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF)
                processAxiom((OWLSubClassOfAxiom) axiom);
            else if (axiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES)
                processAxiom((OWLEquivalentClassesAxiom) axiom);
            else if (axiom.getAxiomType() == AxiomType.DISJOINT_UNION)
                processAxiom((OWLDisjointUnionAxiom) axiom);
            else if (axiom.getAxiomType() == AxiomType.DISJOINT_CLASSES)
                processAxiom((OWLDisjointClassesAxiom) axiom);

            if (add) {
                getSolverClauses().addAll(getTranslations().get(axiom));
            } else
                getSolverClauses().removeAll(getTranslations().get(axiom));
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

                    clause.push(getIndex(expr));
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
            IVecInt clause = new VecInt();
            OWLClassExpression subClass = axiom.getSubClass();
            clause.push(getIndex(subClass));

            // TODO: finish implementation
            OWLClassExpression nnf = axiom.getSuperClass().getNNF();
        }
        return getTranslations().get(axiom);
    }


    private int getIndex(OWLClassExpression expr) {
        if (expr.isClassExpressionLiteral())
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
        return getIndex().put(cl, maxIndex++);
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
