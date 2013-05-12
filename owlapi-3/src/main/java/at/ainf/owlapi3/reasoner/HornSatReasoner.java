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
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

//import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 11.04.13
 * Time: 23:20
 * To change this template use File | Settings | File Templates.
 */
public class HornSatReasoner extends ExtendedStructuralReasoner {

    private static Logger logger = LoggerFactory.getLogger(HornSatReasoner.class.getName());

    private Multimap<IVecInt, IConstr> solverClauses;
    private Map<IVecInt, OWLAxiom> constraints = null;

    private final ISolver solver = SolverFactory.newDefault();
    private Boolean sat = null;
    private boolean recomputeUnsatClasses = true;

    private long[] measures = new long[3];

    // fields storing data used in extraction of an unsat core
    private Core relevantClasses = null;
    private Set<OWLClass> unSatClasses = null;
    private boolean extractCoresOnUpdate = true;

    public final static String NAME = "SAT Reasoner for OWL";
    private HornSatReasoner.OWLSatStructure owlSatStructure;


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
        this(ontology, config, buffering, null);
        //getOWLSatStructure().unSatClasses = Collections.unmodifiableSet(getUnsatisfiableClasses().getEntities());
    }

    public HornSatReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering, OWLSatStructure structure) {
        super(ontology, config, buffering);
        if (structure != null)
            setOWLSatStructure(structure);
        else
            setOWLSatStructure(new OWLSatStructure(ontology));
        this.solverClauses = HashMultimap.create(getOWLSatStructure().axiomsCount, 1);

        processAxioms(getReasonerAxiomsSet(), Collections.<OWLAxiom>emptySet());
        //setExtractCoresOnUpdate(false);
        /*
        if (getOWLSatStructure().unSatClasses != null)
            getOWLSatStructure().unSatClasses = Collections.unmodifiableSet(getOWLSatStructure().unSatClasses);
        else getOWLSatStructure().unSatClasses = null;
        */
    }

    public Set<OWLClass> getTestClasses() {
        if (getUnsatClasses() == null)
            return getRootOntology().getClassesInSignature();
        if (getRelevantCore() != null)
            return Sets.intersection(getUnsatClasses(), getRelevantCore().getRelevantClasses());
        return Sets.intersection(getUnsatClasses(), getRootOntology().getClassesInSignature());
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
        if (getReasonerAxiomsSet().contains(axiom))
            return true;

        if (!isEntailmentCheckingSupported(axiom.getAxiomType()))
            throw new UnsupportedEntailmentTypeException(axiom);
        if (!isConsistent())
            throw new InconsistentOntologyException();
        if (!axiom.getDataPropertiesInSignature().isEmpty() || !axiom.getObjectPropertiesInSignature().isEmpty())
            throw new UnsupportedEntailmentTypeException(axiom);

        Boolean result = null;
        /*
        if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF)
            result = verifySubClass((OWLSubClassOfAxiom) axiom);
        else if (axiom.getAxiomType() == AxiomType.DISJOINT_CLASSES)
            result = verifyDisjointness((OWLDisjointClassesAxiom) axiom);
         */
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
                derivable = containsAllNegativeSymbols(clause, hornCore.getSymbolsSet(), true);
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
        final Set<IVecInt> constraints = getRelevantConstraints(getConstraintsSet(), clause);
        for (IVecInt constraint : constraints) {
            for (IteratorInt it = constraint.iterator(); it.hasNext(); ) {
                int literal = Math.abs(it.next());
                Core core = new Core();
                core.useOnlyHornClauses = true;
                Core hornCore = extractCore(literal, core);
                core.addSymbols(constraint);
                // head is derivable if horn core contains all premises
                boolean derivable = containsAllNegativeSymbols(clause, hornCore.getSymbolsSet(), true);
                if (derivable) return true;
                if (!core.isHornComplete)
                    hornComplete = false;
            }
        }
        // there is derivation for the disjointness in a horn complete KB
        if (hornComplete) return false;
        // no decision can be made
        return null;
    }

    @Override
    public void prepareReasoner() throws ReasonerInterruptedException, TimeOutException {
        //super.prepareReasoner();
    }

    private Set<IVecInt> getRelevantConstraints(Set<IVecInt> iVecInts, IVecInt symbols) {
        Set<IVecInt> constraints = new HashSet<IVecInt>(iVecInts.size());
        for (IVecInt clause : iVecInts) {
            if (isConstraint(clause) && areIntersecting(clause, symbols))
                constraints.add(clause);
        }
        return constraints;
    }

    private boolean areIntersecting(IVecInt clause, IVecInt symbols) {
        for (IteratorInt it = clause.iterator(); it.hasNext(); ) {
            int literal = it.next();
            if (symbols.contains(literal))
                return true;
        }
        return false;
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
    public boolean isSatisfiable(OWLClassExpression classExpression) throws
            ReasonerInterruptedException, TimeOutException, ClassExpressionNotInProfileException,
            FreshEntitiesException, InconsistentOntologyException {
        Collection<IVecInt> iVecInts;
        if (classExpression instanceof OWLAxiom) {
            OWLAxiom axiom = (OWLAxiom) classExpression;
            iVecInts = processAxiom(axiom, new OWL2SATTranslator(this));
        } else if (classExpression instanceof OWLClass && isExtractingCoresOnUpdate() &&
                !getRelevantCore().getRelevantClasses().contains(classExpression)) {
            return true;

        } else
            iVecInts = getiVecInt(classExpression);
        return isSatisfiable(iVecInts);
    }

    /*
        if (isConjunctionOfUnits(iVecInts)){
            Boolean result = verifyConstraintEntailment(getConstraint(iVecInts));
            if (result != null)
                return result;
        }

    private IVecInt getConstraint(Collection<IVecInt> iVecInts) {
        VecInt constraint = new VecInt(iVecInts.size());
        for (IVecInt iVecInt : iVecInts) {
            for (IteratorInt it = iVecInt.iterator(); it.hasNext();)
            {
                int value = it.next();
                if (value < 0) return null;
                    constraint.push(-1*value);
            }
        }
        return constraint;
    }


    private boolean isConjunctionOfUnits(Collection<IVecInt> iVecInts) {
        for (IVecInt clause : iVecInts) {
            if (clause.size() > 1)
                return false;
        }
        return true;
    }
    */

    private boolean isSatisfiable(IVecInt clause) {
        try {
            return solver.isSatisfiable(clause);
        } catch (TimeoutException e) {
            throw new TimeOutException();
        }
    }

    private boolean isSatisfiable(Collection<IVecInt> iVecInt) {
        if (iVecInt.size() == 1)
            return isSatisfiable(iVecInt.iterator().next());

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
    public Node<OWLClass> getBottomClassNode() {
        // return super.getBottomClassNode();
        return new OWLClassNode(getDataFactory().getOWLNothing());
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() throws ReasonerInterruptedException, TimeOutException {
        /*
        if (getRelevantClasses() != null && this.sat != null && isExtractingCoresOnUpdate()){
            getOWLSatStructure().unSatClasses = getRelevantClasses();
            return new OWLClassNode(getOWLSatStructure().unSatClasses);
        }
        */

        Set<OWLClass> unSat = computeUnsatisfiableClasses();
        return new OWLClassNode(unSat);
    }

    protected Set<OWLClass> computeUnsatisfiableClasses() {
        if (!this.recomputeUnsatClasses)
            return getUnsatClasses();

        Set<OWLClass> relevantClasses = getRelevantCore().getRelevantClasses();

        if (relevantClasses == null) {
            final Core core = extractPossiblyUnsatCore();
            relevantClasses = convertToOWLClasses(core);
            setRelevantClasses(core);
            /*
            if (core.isHornComplete){
                getOWLSatStructure().unSatClasses = getRelevantCore().getRelevantClasses();
                return new OWLClassNode(getOWLSatStructure().getUnsatClasses());
            }
            */
        }

        Set<OWLClass> unSat = new HashSet<OWLClass>(relevantClasses.size());

        //Set<OWLClass> relevantClasses = getRelevantClasses();
        //getRootOntology().getClassesInSignature() : getRelevantClasses();

        for (OWLClass owlClass : relevantClasses) {
            if (!isSatisfiable(owlClass))
                unSat.add(owlClass);
        }

        this.unSatClasses = unSat;
        this.recomputeUnsatClasses = false;
        return unSat;
    }

    public List<OWLClass> getSortedUnsatisfiableClasses() {
        return getSortedUnsatisfiableClasses(Collections.<OWLClass>emptySet(), 0);
    }

    Map<Integer, Integer> score = null;

    public Set<Set<OWLAxiom>> clusterAxioms(Set<OWLAxiom> axioms) {
        Set<Set<OWLAxiom>> clusters = new HashSet<Set<OWLAxiom>>();
        for (IVecInt clause : getConstraintsSet()) {
            Core core = new Core(axioms.size(), 2);
            core.useOnlyHornClauses = true;
            final Set<Integer> symbolsSet = extractCore(clause, core).getSymbolsSet();
            Set<OWLAxiom> cluster = new HashSet<OWLAxiom>(symbolsSet.size());
            for (OWLAxiom axiom : axioms) {
                for (OWLClass owlClass : axiom.getClassesInSignature()) {
                    if (symbolsSet.contains(owlClass)){
                        cluster.add(axiom);
                        break;
                    }
                }
            }
            clusters.add(cluster);
        }
        return clusters;
    }

    public List<OWLClass> getSortedUnsatisfiableClasses(Collection<OWLClass> excludeClasses, int maxClasses) {
        if (logger.isDebugEnabled())
            logger.debug("Extracting unsatisfiable classes");
        final Core rcore = getRelevantCore();
        //final int avg = average(rcore.getSymbolsMap().values());
        final Set<Integer> symbolsSet = getRelevantCore().getSymbolsSet();
        if (symbolsSet.isEmpty())
            return Collections.emptyList();

        if (score == null) {
            score = new HashMap<Integer, Integer>(symbolsSet.size());
            for (Integer index : symbolsSet) {
                final Set<Integer> dependentSymbols = getDependentSymbols(index, symbolsSet);
                symbolsSet.removeAll(dependentSymbols);
                final int score = dependentSymbols.size(); //Sets.intersection(symbolsSet, dependentSymbols).size();
                this.score.put(index, score);
                if (logger.isDebugEnabled() && dependentSymbols.size() != score)
                    logger.debug("Dependent symbols included irrelevant elements " + (dependentSymbols.size() - score));
            }
        }

        for (OWLClass excludeClass : excludeClasses) {
            final int index = getIndex(excludeClass);
            symbolsSet.remove(index);
            symbolsSet.removeAll(getDependentSymbols(index, symbolsSet));
        }

        ArrayList<Integer> sortedSymbols = new ArrayList<Integer>(symbolsSet);

        if (logger.isDebugEnabled())
            logger.debug("Searching " + maxClasses + " unsat classes from " + sortedSymbols.size());

        if (sortedSymbols.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(sortedSymbols, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                //final Collection<Integer> level1 = rcore.getSymbolsMap().get(o1);
                //final Collection<Integer> level2 = rcore.getSymbolsMap().get(o2);
                //if (level1.size() != level2.size())
                //    return Integer.valueOf(level1.size()).compareTo(level2.size());

                //Integer min1 = Math.abs(Collections.min(level1)); // avg-
                //Integer min2 = Math.abs(Collections.min(level2));
                //return min1.compareTo(min2);
                return -1 * getScore(o1, symbolsSet).compareTo(getScore(o2, symbolsSet));
            }
        });

        Set<Integer> excludedIndexes = new HashSet<Integer>(sortedSymbols.size());
        List<OWLClass> unSat = new ArrayList<OWLClass>(maxClasses);
        for (Integer index : sortedSymbols) {
            OWLClass owlClass = getIndex(index);
            if (!excludedIndexes.contains(index) && !isSatisfiable(owlClass)) {
                unSat.add(owlClass);
                excludedIndexes.addAll(getDependentSymbols(index, symbolsSet));
            }
            if (maxClasses > 0 && unSat.size() == maxClasses)
                break;
        }
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Selected unsat classes with scores: ");
            for (OWLClass owlClass : unSat) {
                sb.append(this.score.get(getIndex(owlClass))).append(" ");
            }
            logger.debug(sb.toString());
        }
        return unSat;
    }

    private Integer getScore(Integer index, Set<Integer> relevantIndexes) {
        if (!score.containsKey(index)) {
            final Set<Integer> dependentSymbols = getDependentSymbols(index, relevantIndexes);
            score.put(index, dependentSymbols.size());//score.put(index, Sets.intersection(relevantIndexes, dependentSymbols).size());
        }
        return score.get(index);
    }

    private Set<Integer> getDependentSymbols(int index, Set<Integer> selectedIndexes) {
        Core core = new Core();
        core.useOnlyHornClauses = true;
        core = extractCore(index, core);
        return core.getSymbolsSet();
    }

    private int average(Collection<Integer> values) {
        int sum = 0;
        for (Integer value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    Core extractPossiblyUnsatCore() {
        Set<IVecInt> constraints = getConstraints(getReasonerAxiomsSet());
        return extractCore(constraints);
    }

    @Override
    protected void handleChanges(Set<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
        super.handleChanges(addAxioms, removeAxioms);
        processAxioms(addAxioms, removeAxioms);

    }

    private void processAxioms(Collection<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
        // clean up the solver instance
        resetCalls();
        invalidateCaches(!addAxioms.isEmpty() || !removeAxioms.isEmpty());

        if (logger.isDebugEnabled())
            logger.debug("Processing axioms a:" + addAxioms.size() + " r:" + removeAxioms.size());

        for (OWLAxiom axiom : removeAxioms) {
            Collection<IVecInt> clauses = processAxiom(axiom, new OWL2SATTranslator(this));
            if (clauses != null)
                for (IVecInt clause : clauses) {
                    for (IConstr iConstr : getSolverClauses().get(clause)) {
                        if (iConstr != null)
                            this.solver.removeConstr(iConstr);
                    }
                    // remove old translations
                    getSolverClauses().removeAll(clause);
                    getConstraints().remove(clause);

                    // unregister clauses by corresponding symbols
                    /*
                    if (isExtractingCoresOnUpdate()) {
                        getSymbolsToClauses().values().remove(clause);
                    }
                    */
                }
        }

        for (OWLAxiom axiom : addAxioms) {
            Collection<IVecInt> clauses = getTranslations(axiom);
            if (clauses == null || clauses.isEmpty()) {
                clauses = processAxiom(axiom, new OWL2SATTranslator(this));
                addSymbolsToClauses(clauses);
            }
            if (clauses != null)
                for (IVecInt clause : clauses) {
                    if (clause == null || getSolverClauses().containsKey(clause))
                        continue;
                    IConstr iConstr = null;
                    try {
                        iConstr = this.solver.addClause(clause);
                    } catch (ContradictionException e) {
                        this.sat = false;
                        return;
                    } finally {
                        if (iConstr != null)
                            getSolverClauses().put(clause, iConstr);
                        if (logger.isDebugEnabled() && getSolverClauses().size() != this.solver.nConstraints()) {
                            logger.debug("Solver cache is not sync! Constraint " + iConstr + " clause " + clause);
                        }
                    }

                    if (isExtractingCoresOnUpdate()) {
                        if (isConstraint(clause))
                            getConstraints().put(clause, axiom);
                    }
                }
        }

        if (isExtractingCoresOnUpdate()) {
            final Core core = extractPossiblyUnsatCore();
            setRelevantClasses(core);
            if (core.isHornComplete) {
                this.sat = core.symbols.isEmpty();
            }
        }

        if (getCalls() != 0 && logger.isInfoEnabled())
            logger.info("Converted to CNF in " + getCnfTime() + " ms using " + getCalls()
                    + " calls with " + getConstraints().size() + " constraints.");
    }

    private void invalidateCaches(boolean update) {
        if (this.constraints == null)
            this.constraints = new HashMap<IVecInt, OWLAxiom>();
        // reset solver state in case of changes
        if (update) {
            setRelevantClasses(null);
            this.sat = null;
            this.recomputeUnsatClasses = true;
        }
    }

    private void addSymbolsToClauses(Collection<IVecInt> clauses) {
        if (clauses == null || clauses.isEmpty())
            return;
        for (IVecInt clause : clauses) {
            addSymbolsToClauses(clause);
        }
    }

    private Set<IVecInt> getConstraintsSet() {
        return this.constraints.keySet();
    }

    private Map<IVecInt, OWLAxiom> getConstraints() {
        return this.constraints;
    }

    private Set<IVecInt> getConstraints(Collection<OWLAxiom> reasonerAxioms) {
        if (getConstraints() != null)
            return getConstraintsSet();
        Set<IVecInt> constraints = new HashSet<IVecInt>();
        for (OWLAxiom axiom : reasonerAxioms) {
            Collection<IVecInt> clauses = processAxiom(axiom, new OWL2SATTranslator(this));
            if (clauses != null)
                for (IVecInt clause : clauses) {
                    if (clause == null)
                        continue;
                    addSymbolsToClauses(clause);
                    if (isConstraint(clause))
                        constraints.add(clause);
                }
        }
        return constraints;
    }

    private Core extractCore(Set<IVecInt> constraints) {
        final int sigSize = getRootOntology().getClassesInSignature().size();
        final int consSize = constraints.size();
        //Multimap<Integer, Integer> supportingMap = HashMultimap.create();

        Core core = new Core(sigSize, consSize);
        for (IVecInt constraint : constraints) {
            core = extractCore(constraint, core);
            if (core.symbols.size() == sigSize)
                break;
        }
        return core;
    }

    private Core extractCore(IVecInt constraint, Core core) { // Multimap<Integer, Integer> supportingMap
        Multimap<Integer, Integer> supportingSymbols = HashMultimap.create(core.signatureSize, core.constraintsCount);
        for (IteratorInt iterator = constraint.iterator(); iterator.hasNext(); ) {
            int symbol = iterator.next();
            Core localCore;
            //if (!supportingMap.containsKey(symbol)) {
            localCore = extractCore(symbol, new Core(core.signatureSize, core.constraintsCount));
            if (!localCore.isHornComplete)
                core.isHornComplete = false;
            //supportingMap.putAll(symbol, localCore.getSymbolsSet());
            //} else symbols = supportingMap.get(symbol);

            if (supportingSymbols.isEmpty())
                supportingSymbols.putAll(localCore.symbols);
            else
                supportingSymbols.keySet().retainAll(localCore.getSymbolsSet());

            if (supportingSymbols.size() == core.signatureSize)
                break;
        }
        core.symbols.putAll(supportingSymbols);
        return core;
    }


    private Set<OWLClass> convertToOWLClasses(Core core) {
        HashSet<OWLClass> classes = new HashSet<OWLClass>(core.getSymbolsSet().size());
        for (Integer symbol : core.getSymbolsSet()) {
            OWLClass ocl = getIndex(symbol);
            classes.add(ocl);
        }
        return classes;
    }

    public Multimap<Integer, IVecInt> getSymbolsToClauses() {
        return getOWLSatStructure().symbolsToClauses;
    }

    private Core extractCore(Integer literal, Core core) {
        return extractCore(literal, core, 0);
    }

    private Core extractCore(Integer literal, Core core, int level) {
        // remove negation
        int symbol = Math.abs(literal);
        if (core.getSymbolsSet().contains(symbol)) return core;
        core.symbols.put(symbol, level);
        //if (core.selectedClasses.contains(symbol))
        //    core.selectedScore++;
        // analyze clauses in which symbol is positive, i.e. in the head of a rule
        for (IVecInt clause : getSymbolsToClauses().get(symbol)) {
            if (!getSolverClauses().containsKey(clause))
                continue;
            final boolean isHornClause = isHornClause(clause);
            if (!isHornClause) core.isHornComplete = false;
            if (core.useOnlyHornClauses && !isHornClause) {
                continue;
            }
            //Set<Integer> neg = containsAllNegativeSymbols(clause, false);

            for (IteratorInt iterator = clause.iterator(); iterator.hasNext(); ) {
                int lit = iterator.next();
                if (lit < 0) extractCore(lit, core, ++level);
            }
        }
        return core;
    }

    private boolean isHornClause(IVecInt clause) {
        return getHornClauseHead(clause) != null;
    }

    private boolean containsAllNegativeSymbols(IVecInt clause, Set<Integer> symbols, boolean ignoreSign) {
        int power = (ignoreSign) ? -1 : 1;
        for (IteratorInt iterator = clause.iterator(); iterator.hasNext(); ) {
            int symbol = iterator.next();
            if (symbol < 0 && symbols.contains(symbol * power))
                return false;
        }
        return true;
    }

    private void addSymbolsToClauses(IVecInt clause) {
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
        return getOWLSatStructure().addToIndex(cl);
    }

    protected Map<OWLClass, Integer> getIndex() {
        return getOWLSatStructure().index;
    }

    public OWLClass getIndex(int index) {
        return getOWLSatStructure().index.inverse().get(Math.abs(index));
    }

    public Collection<IVecInt> getTranslations(OWLAxiom axiom) {
        return getOWLSatStructure().translations.get(axiom);
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

    protected void setRelevantClasses(Core relevantClasses) {
        this.relevantClasses = relevantClasses;
    }

    public Core getRelevantCore() {
        return relevantClasses;
    }


    public boolean isExtractingCoresOnUpdate() {
        return extractCoresOnUpdate;
    }

    void setExtractCoresOnUpdate(boolean extractCoresOnUpdate) {
        this.extractCoresOnUpdate = extractCoresOnUpdate;
    }

    public boolean hasTranslations(OWLAxiom axiom) {
        return getOWLSatStructure().translations.containsKey(axiom);
    }

    public void addTranslation(OWLAxiom axiom, IVecInt clause) {
        getOWLSatStructure().translations.put(axiom, clause);
    }

    public void addTranslations(OWLAxiom axiom, Collection<IVecInt> clauses) {
        getOWLSatStructure().translations.putAll(axiom, clauses);
    }


    public class OWLSatStructure {
        private final BiMap<OWLClass, Integer> index;

        // caching of transformations
        private final Multimap<OWLAxiom, IVecInt> translations;
        private final Multimap<Integer, IVecInt> symbolsToClauses;

        private int maxIndex = 1;
        private final int axiomsCount;
        private final int classesCount;

        public OWLSatStructure(OWLOntology ontology) {
            final Set<OWLClass> classes = ontology.getClassesInSignature(true);
            this.classesCount = classes.size();
            this.axiomsCount = ontology.getAxiomCount();

            // init index
            this.index = HashBiMap.create(classes.size());
            for (OWLClass owlClass : classes) {
                this.index.put(owlClass, this.maxIndex++);
            }

            // initialize caching maps
            this.translations = HashMultimap.create(axiomsCount, 10);
            this.symbolsToClauses = HashMultimap.create(classesCount, 10);
        }

        public int addToIndex(OWLClass cl) {
            int value = maxIndex++;
            if (this.index.containsKey(cl))
                throw new RuntimeException("Adding a key that already exists! " + cl);
            this.index.put(cl, value);
            return value;
        }
    }

    public Set<OWLClass> getUnsatClasses() {
        return unSatClasses;
    }

    public void setOWLSatStructure(OWLSatStructure owlSatStructure) {
        this.owlSatStructure = owlSatStructure;
    }

    public HornSatReasoner.OWLSatStructure getOWLSatStructure() {
        return owlSatStructure;
    }

    public class Core {
        private final int signatureSize;
        private final int constraintsCount;
        Multimap<Integer, Integer> symbols;
        boolean useOnlyHornClauses = false;
        boolean isHornComplete = true;
        Set<OWLClass> relevantClasses = null;

        Core(int symbols, int constraints) {
            this.signatureSize = symbols;
            this.constraintsCount = constraints;
            this.symbols = HashMultimap.create(symbols, constraints);
        }

        Core() {
            this(16, 2);
        }

        public void addSymbols(IVecInt clause) {
            for (IteratorInt it = clause.iterator(); it.hasNext(); ) {
                int literal = Math.abs(it.next());
                symbols.put(literal, 0);
            }
        }

        public Set<Integer> getSymbolsSet() {
            return this.symbols.keySet();
        }

        public Set<OWLClass> getRelevantClasses() {
            if (this.relevantClasses == null) {
                final Set<Integer> symbolsSet = getSymbolsSet();
                this.relevantClasses = convertToOWLClasses(this);
            }
            return this.relevantClasses;
        }

        public Multimap<Integer, Integer> getSymbolsMap() {
            return this.symbols;
        }
    }

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

}
