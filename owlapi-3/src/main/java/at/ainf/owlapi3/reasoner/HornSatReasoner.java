package at.ainf.owlapi3.reasoner;

import at.ainf.owlapi3.reasoner.axiomprocessors.OWL2SATTranslator;
import at.ainf.owlapi3.reasoner.axiomprocessors.OWLClassAxiomNegation;
import at.ainf.owlapi3.reasoner.axiomprocessors.Translator;
import com.google.common.collect.*;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
//import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 11.04.13
 * Time: 23:20
 * To change this template use File | Settings | File Templates.
 */
public class HornSatReasoner extends ExtendedStructuralReasoner {

    private static Logger logger = LoggerFactory.getLogger(HornSatReasoner.class.getName());

    private final BiMap<OWLClass, Integer> index = HashBiMap.create();

    // caching of transformations
    private final Multimap<OWLAxiom, IVecInt> translations = HashMultimap.create();
    private final Multimap<Integer, IVecInt> symbolsToClauses = HashMultimap.create();
    private Multimap<IVecInt, IConstr> solverClauses = HashMultimap.create();

    private final ISolver solver = SolverFactory.newDefault();
    private final Set<OWLClass> unSatClasses;
    private Boolean sat = null;
    private int maxIndex = 1;

    private long[] measures = new long[3];

    // fields storing data used in extraction of an unsat core
    private Set<OWLClass> relevantClasses = null;
    //private Set<IVecInt> constraints = new HashSet<IVecInt>();
    private boolean extractCoresOnUpdate = true;

    public final static String NAME = "SAT Reasoner for OWL";

    public long getCalls() {
        return this.measures[0];
    }

    public long getCnfTime() {
        this.measures[1] = System.currentTimeMillis() - this.measures[1];
        return this.measures[1];
    }

    public void resetCalls() {
        this.measures[0] = 0;
        this.measures[1] = System.currentTimeMillis();
    }

    public HornSatReasoner(OWLOntology ontology) {
        this(ontology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING, null);
    }

    /**
     * Creates an instance of the reasoner and initializes the set of unsatisfiable classes
     *
     * @param ontology
     * @param config
     * @param buffering
     */
    public HornSatReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering) {
        super(ontology, config, buffering);
        boolean extract = isExtractingCoresOnUpdate();
        setExtractCoresOnUpdate(true);
        processAxioms(getReasonerAxiomsSet(), Collections.<OWLAxiom>emptySet());
        Set<OWLClass> entities = getUnsatisfiableClasses().getEntities();
        this.unSatClasses = Collections.unmodifiableSet(entities);
        setExtractCoresOnUpdate(extract);
    }

    public HornSatReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering, Set<OWLClass> unSatClasses) {
        super(ontology, config, buffering);
        processAxioms(getReasonerAxiomsSet(), Collections.<OWLAxiom>emptySet());
        if (unSatClasses != null)
            this.unSatClasses = Collections.unmodifiableSet(unSatClasses);
        else this.unSatClasses = null;
    }

    public Set<OWLClass> getTestClasses() {
        if (unSatClasses == null)
            return getRootOntology().getClassesInSignature();
        if (getRelevantClasses() != null)
            return Sets.intersection(unSatClasses, getRelevantClasses());
        return Sets.intersection(unSatClasses, getRootOntology().getClassesInSignature());
    }

    @Override
    public String getReasonerName() {
        return NAME;
    }

    @Override
    public Version getReasonerVersion() {
        return new Version(1, 0, 0, 0);
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException, AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        // all ontology axioms are entailed
        if (getRootOntology().getLogicalAxioms().contains(axiom))
            return true;

        if (!isEntailmentCheckingSupported(axiom.getAxiomType()))
            throw new UnsupportedEntailmentTypeException(axiom);
        if (!isConsistent())
            throw new InconsistentOntologyException();
        if (!axiom.getDataPropertiesInSignature().isEmpty() || !axiom.getObjectPropertiesInSignature().isEmpty())
            throw new UnsupportedEntailmentTypeException(axiom);

        Boolean result = null;
        if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF)
            result = verifySubClass((OWLSubClassOfAxiom) axiom);
        else if (axiom.getAxiomType() == AxiomType.DISJOINT_CLASSES)
            result = verifyDisjointness((OWLDisjointClassesAxiom) axiom);

        if (result == null) {
            OWLClassExpression negation = processAxiom(axiom, new OWLClassAxiomNegation(this));
            result = !isSatisfiable(negation);
        }
        return result;
    }

    private Boolean verifyDisjointness(OWLDisjointClassesAxiom axiom) {
        Collection<IVecInt> iVecInts = processAxiom(axiom, new OWL2SATTranslator(this));
        // verify if premises can be reached from the head (backward chaining) for each clause
        for (IVecInt clause : iVecInts) {
            if (getSolverClauses().containsKey(clause))
                continue;
            Boolean result = verifyConstraintEntailment(clause);
            if (result == null || !result)
                return result;
        }
        return true;
    }

    private Boolean verifySubClass(OWLSubClassOfAxiom axiom) {
        // the search is incomplete if head cannot be derived using only horn clauses
        // convert axiom to CNF and verify whether every element is a horn clause
        Collection<IVecInt> iVecInts = processAxiom(axiom, new OWL2SATTranslator(this));
        // verify if premises can be reached from the head (backward chaining) for each clause
        for (IVecInt clause : iVecInts) {
            if (getSolverClauses().containsKey(clause))
                continue;
            Integer head = getHornClauseHead(clause);
            if (head == null) return null;
            boolean derivable;
            if (head == 0) {
                derivable = verifyConstraintEntailment(clause);
            } else {
                Core core = new Core();
                core.useOnlyHornClauses = true;
                Core hornCore = extractCore(head, core);
                // head is derivable if horn core contains all premises
                derivable = hornCore.symbols.containsAll(getNegativeSymbols(clause, true));
                if (!derivable && core.isHornComplete)
                    return false;
            }
            if (!derivable) return null;
        }
        return true;
    }

    private Boolean verifyConstraintEntailment(IVecInt clause) {
        boolean hornComplete = true;
        // check if this constraint can be derived from the other - getConstraints(getSolverClauses().keys())
        for (IteratorInt it = clause.iterator(); it.hasNext(); ) {
            int literal = it.next();
            Core core = new Core();
            core.useOnlyHornClauses = true;
            Core hornCore = extractCore(literal, core);
            // head is derivable if horn core contains all premises
            Set<Integer> negativeSymbols = getNegativeSymbols(clause, true);
            negativeSymbols.remove(literal);
            boolean derivable = hornCore.symbols.containsAll(negativeSymbols);
            if (derivable) return true;
            if (!core.isHornComplete)
                hornComplete = false;
        }
        // there is derivation for the disjointness in a horn complete KB
        if (hornComplete) return false;
        // no decision can be made
        return null;
    }

    /**
     * Verifies whether an input clause is a Horn clause
     *
     * @param clause input clause to be verified in DIMACS format, with no <code>0</code>  values allowed
     * @return a positive integer if the head has one element, 0 if the head is empty and
     *         <code>null</code> if the clause is not a Horn clause
     */
    private Integer getHornClauseHead(IVecInt clause) {
        int head = 0;
        for (IteratorInt it = clause.iterator(); it.hasNext(); ) {
            int literal = it.next();
            if (literal > 0 && head > 0)
                return null;
            else if (literal > 0)
                head = literal;
        }
        return head;
    }


    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) throws ReasonerInterruptedException, TimeOutException, ClassExpressionNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
        Collection<IVecInt> iVecInts;
        if (classExpression instanceof OWLAxiom) {
            OWLAxiom axiom = (OWLAxiom) classExpression;
            iVecInts = processAxiom(axiom, new OWL2SATTranslator(this));
        } else
            iVecInts = getiVecInt(classExpression);
        return isSatisfiable(iVecInts);
    }

    private boolean isSatisfiable(Collection<IVecInt> iVecInt) {
        Set<IConstr> iConstr = new HashSet<IConstr>();
        try {
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
        Set<OWLClass> classes = getTestClasses();

        /*
        OWLObjectIntersectionOf test = getDataFactory().getOWLObjectIntersectionOf(classes);
        if (!isSatisfiable(test)) {
            sat = false;
            return sat;
        }
        */
        for (OWLClass owlClass : classes) {
            Set<IVecInt> iVecInts = getiVecInt(owlClass);
            if (!isSatisfiable(iVecInts)) {
                sat = false;
                return sat;
            }
        }

        sat = true;
        return sat;
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
        if (this.unSatClasses != null)
            return new OWLClassNode(this.unSatClasses);

        Set<OWLClass> unSat = new HashSet<OWLClass>();

        Set<OWLClass> relevantClasses = getRelevantClasses();

        if (relevantClasses == null) {
            relevantClasses = extractPossiblyUnsatClasses();
            setRelevantClasses(relevantClasses);
        }

        //Set<OWLClass> relevantClasses = getRelevantClasses();
        //getRootOntology().getClassesInSignature() : getRelevantClasses();

        for (OWLClass owlClass : relevantClasses) {
            if (!isSatisfiable(owlClass))
                unSat.add(owlClass);
        }
        return new OWLClassNode(unSat);
    }

    Set<OWLClass> extractPossiblyUnsatClasses() {
        Set<IVecInt> constraints = getConstraints(getReasonerAxiomsSet());
        return extractCore(constraints);
    }

    @Override
    protected void handleChanges(Set<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
        super.handleChanges(addAxioms, removeAxioms);
        //processAxioms(getRootOntology().getAxioms());
        processAxioms(addAxioms, removeAxioms);

    }

    private void processAxioms(Collection<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
        // clean up the solver instance
        resetCalls();
        // reset solver state in case of changes
        if (!addAxioms.isEmpty() || !removeAxioms.isEmpty()) {
            setRelevantClasses(null);
            this.sat = null;
        }

        for (OWLAxiom axiom : removeAxioms) {
            Collection<IVecInt> clauses = processAxiom(axiom, new OWL2SATTranslator(this));
            if (clauses != null)
                for (IVecInt clause : clauses) {
                    for (IConstr iConstr : getSolverClauses().get(clause)) {
                        if (iConstr != null)
                            solver.removeConstr(iConstr);
                    }
                    // remove old translations
                    getSolverClauses().removeAll(clause);
                    // unregister clauses by corresponding symbols
                    if (isExtractingCoresOnUpdate()) {
                        getSymbolsToClauses().values().remove(clause);
                    }
                }
        }

        for (OWLAxiom axiom : addAxioms) {
            Collection<IVecInt> clauses = processAxiom(axiom, new OWL2SATTranslator(this));
            if (clauses != null)
                for (IVecInt clause : clauses) {
                    if (clause == null)
                        continue;
                    IConstr iConstr = null;
                    try {
                        iConstr = solver.addClause(clause);
                    } catch (ContradictionException e) {
                        this.sat = false;
                        return;
                    }
                    getSolverClauses().put(clause, iConstr);
                    //if (isExtractingCoresOnUpdate() && isConstraint(clause))
                    //      addSymbolsToConstants(clause);
                    //    constraints.add(clause);
                }
        }

        if (isExtractingCoresOnUpdate()) {
            //Set<IVecInt> constraints = getConstraints(getReasonerAxioms());
            //setRelevantClasses(extractCore(constraints));
            setRelevantClasses(extractPossiblyUnsatClasses());
        }

        if (getCalls() != 0 && logger.isInfoEnabled())
            logger.info("Converted to CNF in " + getCnfTime() + " ms using " + getCalls()
                    + " calls");

        if (getSolverClauses().size() != solver.nConstraints())
            logger.error("Solver is not synchronized! (T/S) " +
                    getSolverClauses().size() + "/" + solver.nConstraints());
        //solver.newVar(getNumberOfVariables(getSolverClauses()));
        //solver.setExpectedNumberOfClauses(getSolverClauses().size());
        /*
        try {
            this.sat = null;
            for (IVecInt cl : getSolverClauses()) {
                solver.addClause(cl);
            }
        } catch (ContradictionException e) {
            this.sat = false;
        }
        */
    }

    private Set<IVecInt> getConstraints(Collection<OWLAxiom> reasonerAxioms) {
        Set<IVecInt> constraints = new HashSet<IVecInt>();
        for (OWLAxiom axiom : reasonerAxioms) {
            Collection<IVecInt> clauses = processAxiom(axiom, new OWL2SATTranslator(this));
            if (clauses != null)
                for (IVecInt clause : clauses) {
                    if (clause == null)
                        continue;
                    addSymbolsToConstants(clause);
                    if (isConstraint(clause))
                        constraints.add(clause);
                }
        }
        return constraints;
    }

    private Set<OWLClass> extractCore(Set<IVecInt> constraints) {
        int sigSize = getRootOntology().getClassesInSignature().size();
        Multimap<Integer, Integer> supportingMap = HashMultimap.create();
        Set<Integer> core = new HashSet<Integer>();
        for (IVecInt constraint : constraints) {
            Set<Integer> supportingSymbols = new HashSet<Integer>(sigSize);
            for (IteratorInt iterator = constraint.iterator(); iterator.hasNext(); ) {
                int symbol = iterator.next();
                Collection<Integer> symbols;
                if (!supportingMap.containsKey(symbol)) {
                    symbols = extractCore(symbol, new Core(sigSize)).symbols;
                    supportingMap.putAll(symbol, symbols);
                } else symbols = supportingMap.get(symbol);

                if (supportingSymbols.isEmpty())
                    supportingSymbols.addAll(symbols);
                else
                    supportingSymbols.retainAll(symbols);

                if (supportingSymbols.size() == sigSize)
                    break;
            }
            core.addAll(supportingSymbols);
            if (core.size() == sigSize)
                break;
        }

        return convertToOWLClasses(core);
    }

    private Set<OWLClass> convertToOWLClasses(Set<Integer> core) {
        Set<OWLClass> classes = new HashSet<OWLClass>(core.size());
        for (Integer symbol : core) {
            OWLClass ocl = getIndex(symbol);
            classes.add(ocl);
        }
        return classes;
    }

    public Multimap<Integer, IVecInt> getSymbolsToClauses() {
        return symbolsToClauses;
    }


    private class Core {
        Set<Integer> symbols;
        boolean useOnlyHornClauses = false;
        boolean isHornComplete = true;

        Core(int size) {
            symbols = new HashSet<Integer>(size);
        }

        Core() {
            this(16);
        }
    }

    private Core extractCore(Integer literal, Core core) {
        // remove negation
        int symbol = Math.abs(literal);
        if (core.symbols.contains(symbol)) return core;
        core.symbols.add(symbol);
        // analyze clauses in which symbol is positive, i.e. in the head of a rule
        for (IVecInt clause : getSymbolsToClauses().get(symbol)) {
            if (core.useOnlyHornClauses && !isHornClause(clause)) {
                core.isHornComplete = false;
                continue;
            }
            Set<Integer> neg = getNegativeSymbols(clause, false);
            for (Integer lit : neg) {
                extractCore(lit, core);
            }
        }
        return core;
    }

    private boolean isHornClause(IVecInt clause) {
        return getHornClauseHead(clause) != null;
    }

    private Set<Integer> getNegativeSymbols(IVecInt clause, boolean removeSign) {
        int power = (removeSign) ? -1 : 1;
        Set<Integer> symbols = new HashSet<Integer>(clause.size());
        for (IteratorInt iterator = clause.iterator(); iterator.hasNext(); ) {
            int symbol = iterator.next();
            if (symbol < 0)
                symbols.add(symbol * power);
        }
        return symbols;
    }

    private void addSymbolsToConstants(IVecInt clause) {
        for (IteratorInt iterator = clause.iterator(); iterator.hasNext(); ) {
            int symbol = iterator.next();
            if (symbol >= 0) {
                getSymbolsToClauses().put(symbol, clause);
            }
        }
    }

    private boolean isConstraint(IVecInt clause) {
        boolean constraint = true;
        for (IteratorInt iterator = clause.iterator(); iterator.hasNext(); ) {
            int symbol = iterator.next();
            if (symbol >= 0) {
                //getSymbolsToClauses().put(symbol, clause);
                //constraint = false;
                return false;
            }
        }
        return constraint;
    }

    /*
    private Set<Integer> analyzeSymbols(IVecInt clause) {
        Set<Integer> symbols = new HashSet<Integer>(clause.size());
        boolean constraint = true;
        for (IteratorInt iterator = clause.iterator(); iterator.hasNext(); ) {
            int symbol = iterator.next();
            if (symbol >= 0) {
                getSymbolsToClauses().put(symbol, clause);
                constraint = false;
            } else if (constraint)
                symbols.add(-1 * (symbol));
        }
        if (constraint)
            return symbols;
        return Collections.emptySet();
    }
    */

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return axiomType == AxiomType.SUBCLASS_OF || axiomType == AxiomType.EQUIVALENT_CLASSES ||
                axiomType == AxiomType.DISJOINT_UNION || axiomType == AxiomType.DISJOINT_CLASSES;
    }

    public <T> T processAxiom(OWLAxiom axiom, Translator<T> translator) {
        T translation = null;
        if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF)
            translation = translator.visit((OWLSubClassOfAxiom) axiom);
        else if (axiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES)
            translation = translator.visit((OWLEquivalentClassesAxiom) axiom);
        else if (axiom.getAxiomType() == AxiomType.DISJOINT_UNION)
            translation = translator.visit((OWLDisjointUnionAxiom) axiom);
        else if (axiom.getAxiomType() == AxiomType.DISJOINT_CLASSES)
            translation = translator.visit((OWLDisjointClassesAxiom) axiom);
        //else if (axiom.getAxiomType() == AxiomType.CLASS_ASSERTION)
        //    return translator.visit((OWLClassAssertionAxiom) axiom);


        return translation;
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

    private OWLClass getIndex(int index) {
        return this.index.inverse().get(index);
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
        this.measures[0]++;
        if (isDisjunctionOfLiterals(fl, false)) return Collections.singleton(fl);

        // apply distribution to non-unary disjunctions
        if (fl.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
            Set<OWLClassExpression> disjuncs = fl.asDisjunctSet();
            if (disjuncs.isEmpty())
                return Collections.emptySet();
            if (disjuncs.size() == 1)
                return convertToCNF(disjuncs.iterator().next());
            OWLClassExpression conj = null;
            Set<OWLClassExpression> cl2 = new LinkedHashSet<OWLClassExpression>();
            //Set<OWLClassExpression> disj = new HashSet<OWLClassExpression>();
            for (OWLClassExpression cl : disjuncs) {
                if (conj == null && !cl.isClassExpressionLiteral() && !isDisjunctionOfLiterals(cl, false)
                        && cl.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)
                    conj = cl;
                else
                    cl2.add(cl);
            }

            if (conj == null) throw new RuntimeException("No conjunction for distribution! " + fl);

            Set<OWLClassExpression> newConj = new HashSet<OWLClassExpression>();
            for (OWLClassExpression c : conj.asConjunctSet()) {
                OWLClassExpression newClause = getOWLDataFactory().getOWLObjectUnionOf(
                        getOWLDataFactory().getOWLObjectUnionOf(cl2), c);
                Set<OWLClassExpression> exprs = convertToCNF(newClause);
                newConj.addAll(exprs);
            }

            // return single conjunction as a set of clauses
            //if (disj.isEmpty())
            return newConj;

            //disj.add(getOWLDataFactory().getOWLObjectIntersectionOf(newConj));

            // add to a source disjunction replacing two selected conjunctions
            //OWLClassExpression expr = getOWLDataFactory().getOWLObjectUnionOf(disj);
            //return convertToCNF(expr);
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

    public Multimap<IVecInt, IConstr> getSolverClauses() {
        return this.solverClauses;
    }

    protected void setRelevantClasses(Set<OWLClass> relevantClasses) {
        this.relevantClasses = relevantClasses;
    }

    protected Set<OWLClass> getRelevantClasses() {
        return relevantClasses;
    }

    public boolean isExtractingCoresOnUpdate() {
        return extractCoresOnUpdate;
    }

    void setExtractCoresOnUpdate(boolean extractCoresOnUpdate) {
        this.extractCoresOnUpdate = extractCoresOnUpdate;
    }


}
