package at.ainf.owlapi3.reasoner;

import at.ainf.owlapi3.reasoner.axiomprocessors.OWL2SATTranslator;
import at.ainf.owlapi3.reasoner.axiomprocessors.OWLClassAxiomNegation;
import at.ainf.owlapi3.reasoner.axiomprocessors.Translator;
import at.ainf.owlapi3.reasoner.cores.AbstractCore;
import at.ainf.owlapi3.reasoner.cores.Core;
import at.ainf.owlapi3.reasoner.cores.CoreSymbol;
import at.ainf.owlapi3.reasoner.cores.HornCore;
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
public class OWLSatReasoner extends ExtendedStructuralReasoner {

    private static Logger logger = LoggerFactory.getLogger(OWLSatReasoner.class.getName());

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
    private OWLSatReasoner.OWLSatStructure owlSatStructure;


    public OWLSatReasoner(OWLOntology ontology) {
        this(ontology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING, null);
    }

    /**
     * Creates an instance of the reasoner and initializes the set of unsatisfiable classes
     *
     * @param ontology
     * @param config
     * @param buffering
     */
    public OWLSatReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering) {
        this(ontology, config, buffering, null);
        //getOWLSatStructure().unSatClasses = Collections.unmodifiableSet(getUnsatisfiableClasses().getEntities());
    }

    public OWLSatReasoner(OWLOntology ontology, OWLReasonerConfiguration config, BufferingMode buffering, OWLSatStructure structure) {
        super(ontology, config, buffering);
        if (structure != null)
            setOWLSatStructure(structure);
        else
            setOWLSatStructure(new OWLSatStructure(ontology));
        this.solverClauses = HashMultimap.create(getOWLSatStructure().apprAxiomsCount, 3);

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

    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) throws
            ReasonerInterruptedException, TimeOutException, ClassExpressionNotInProfileException,
            FreshEntitiesException, InconsistentOntologyException {
        if (classExpression instanceof OWLClass && isExtractingCoresOnUpdate()) {
            boolean isRelevant = getRelevantCore().getRelevantClasses().contains(classExpression);
            if (!isRelevant) return true;
            else if (getRelevantCore().isHornComplete()) return false;
        }
        Collection<IVecInt> iVecInts;
        if (classExpression instanceof OWLAxiom) {
            OWLAxiom axiom = (OWLAxiom) classExpression;
            iVecInts = processAxiom(axiom, new OWL2SATTranslator(this));
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
        if (logger.isDebugEnabled())
            logger.debug("Veryfying consistency: " + String.valueOf(this.sat == null));

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


    public Multimap<OWLAxiom, OWLClass> clusterAxioms(Set<? extends OWLAxiom> axioms) {
        Set<IVecInt> constraintsSet = getConstraintsSet();
        Multimap<OWLAxiom, OWLClass> clusters = HashMultimap.create(constraintsSet.size(), 20);
        for (IVecInt clause : constraintsSet) {
            Core core = new HornCore(this, axioms.size());
            core.extractCore(clause);
            final Set<CoreSymbol> symbolsSet = core.getSymbols();
            if (symbolsSet.isEmpty())
                continue;
            Set<OWLClass> owlClasses = convertToOWLClasses(symbolsSet);
            clusters.putAll(getConstraints().get(clause), owlClasses);
            if (logger.isDebugEnabled())
                logger.debug("Cluster for a constraint " + getConstraints().get(clause) + " includes " +
                        String.valueOf((owlClasses.size() > 10) ? owlClasses.size() : owlClasses));
            /*
            Set<T> cluster = new HashSet<T>(symbolsSet.size());
            cluster.addAll(convertToOWLClasses(symbolsSet));
            for (T axiom : axioms) {
                for (OWLClass owlClass : axiom.getClassesInSignature()) {
                    if (symbolsSet.contains(getIndex(owlClass))) {
                        cluster.add(axiom);
                        break;
                    }
                }
            }
            clusters.add(cluster);
            */
        }
        return clusters;
    }


    public List<OWLClass> getSortedUnsatisfiableClasses(Collection<OWLClass> excludeClasses, int maxClasses) {
        if (logger.isDebugEnabled())
            logger.debug("Extracting unsatisfiable classes");
        final Core rcore = getRelevantCore();
        //final int avg = average(rcore.getSymbolsMap().values());
        final Set<CoreSymbol> symbolsSet = rcore.getSymbols();
        if (symbolsSet.isEmpty())
            return Collections.emptyList();

        final Map<CoreSymbol, Integer> scores = new HashMap<CoreSymbol, Integer>(symbolsSet.size());
        Set<CoreSymbol> excludedSymbols = new HashSet<CoreSymbol>(symbolsSet.size());

        for (CoreSymbol symbol : symbolsSet) {
            if (excludedSymbols.contains(symbol))
                continue;
            final Set<CoreSymbol> dependentSymbols = getDependentSymbols(symbol);
            excludedSymbols.addAll(dependentSymbols);
            final int score = dependentSymbols.size(); //Sets.intersection(symbolsSet, dependentSymbols).size();
            scores.put(symbol, score);
            if (logger.isDebugEnabled() && dependentSymbols.size() != score)
                logger.debug("Dependent symbols included irrelevant elements " + (dependentSymbols.size() - score));
        }

        //symbolsSet.removeAll(excludedSymbols);

        for (OWLClass excludeClass : excludeClasses) {
            final CoreSymbol index = new CoreSymbol(getIndex(excludeClass), 0);
            symbolsSet.remove(index);
            symbolsSet.removeAll(getDependentSymbols(index));
        }

        ArrayList<CoreSymbol> sortedSymbols = new ArrayList<CoreSymbol>(symbolsSet);

        if (logger.isDebugEnabled())
            logger.debug("Searching " + maxClasses + " unsat classes from " + sortedSymbols.size());

        if (sortedSymbols.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(sortedSymbols, new Comparator<CoreSymbol>() {
            @Override
            public int compare(CoreSymbol o1, CoreSymbol o2) {
                //final Collection<Integer> level1 = rcore.getSymbolsMap().get(o1);
                //final Collection<Integer> level2 = rcore.getSymbolsMap().get(o2);
                //if (level1.size() != level2.size())
                //    return Integer.valueOf(level1.size()).compareTo(level2.size());

                //Integer min1 = Math.abs(Collections.min(level1)); // avg-
                //Integer min2 = Math.abs(Collections.min(level2));
                //return min1.compareTo(min2);
                return -1 * getScore(o1).compareTo(getScore(o2));
            }

            private Integer getScore(CoreSymbol index) {//, Set<Integer> relevantIndexes) {
                if (!scores.containsKey(index)) {
                    return 0;
                    //final Set<Integer> dependentSymbols = getDependentSymbols(index, relevantIndexes);
                    //score.put(index, dependentSymbols.size());//score.put(index, Sets.intersection(relevantIndexes, dependentSymbols).size());
                }
                return scores.get(index);
            }
        });

        excludedSymbols = new HashSet<CoreSymbol>(sortedSymbols.size());
        List<OWLClass> unSat = new ArrayList<OWLClass>(maxClasses);
        for (CoreSymbol symbol : sortedSymbols) {
            OWLClass owlClass = getIndex(symbol.getSymbol());
            if (!excludedSymbols.contains(symbol) && !isSatisfiable(owlClass)) {
                unSat.add(owlClass);
                excludedSymbols.addAll(getDependentSymbols(symbol));
            }
            if (maxClasses > 0 && unSat.size() == maxClasses)
                break;
        }
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Selected unsat classes with scores: ");
            for (OWLClass owlClass : unSat) {
                sb.append(scores.get(getIndex(owlClass))).append(" ");
            }
            logger.debug(sb.toString());
        }
        return unSat;
    }

    private Set<CoreSymbol> getDependentSymbols(CoreSymbol index) {
        HornCore core = new HornCore(this);
        core.extractCore(index);
        return core.getSymbols();
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
        if (getOWLSatStructure().updateStructures(addAxioms, removeAxioms))
        {
            HashMultimap<IVecInt, IConstr> newClauses = HashMultimap.create(getOWLSatStructure().apprAxiomsCount, 3);
            newClauses.putAll(this.solverClauses);
            this.solverClauses = newClauses;
        }

        // process axioms
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
            if (core.isHornComplete()) {
                this.sat = core.getSymbols().isEmpty();
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
        // final int consSize = constraints.size();
        // Multimap<Integer, Integer> supportingMap = HashMultimap.create();

        Core core = new Core(this, sigSize);
        core.extractCore(constraints);
        return core;
    }

    public <T extends CoreSymbol> Set<OWLClass> convertToOWLClasses(Set<T> symbolsSet) {
        HashSet<OWLClass> classes = new HashSet<OWLClass>(symbolsSet.size());
        for (T symbol : symbolsSet) {
            OWLClass ocl = getIndex(symbol.getSymbol());
            classes.add(ocl);
        }
        return classes;
    }

    public Set<OWLClass> convertToOWLClasses(AbstractCore<?> core) {
        return convertToOWLClasses(core.getSymbols());
    }

    public Multimap<Integer, IVecInt> getSymbolsToClauses() {
        return getOWLSatStructure().symbolsToClauses;
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
        private Multimap<OWLAxiom, IVecInt> translations;
        private Multimap<Integer, IVecInt> symbolsToClauses;

        private int maxIndex = 1;
        private int apprAxiomsCount;
        private int apprClassesCount;

        public OWLSatStructure(OWLOntology ontology) {
            final Set<OWLClass> classes = ontology.getClassesInSignature(true);
            this.apprClassesCount = classes.size();
            this.apprAxiomsCount = ontology.getAxiomCount();

            // init index
            this.index = HashBiMap.create(classes.size());
            for (OWLClass owlClass : classes) {
                this.index.put(owlClass, this.maxIndex++);
            }

            // initialize caching maps
            this.translations = HashMultimap.create(apprAxiomsCount, 10);
            this.symbolsToClauses = HashMultimap.create(apprClassesCount, 10);
        }

        public int addToIndex(OWLClass cl) {
            int value = maxIndex++;
            if (this.index.containsKey(cl))
                throw new RuntimeException("Adding a key that already exists! " + cl);
            this.index.put(cl, value);
            return value;
        }

        public boolean updateStructures(Collection<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
            // this factor is used to make caches bigger than required to avoid frequent updates
            double factor = 1.1;

            if (this.apprAxiomsCount >= addAxioms.size())
                return false;

            this.apprAxiomsCount = (int) (addAxioms.size()*factor);

            Set<OWLClass> classes = getRootOntology().getClassesInSignature();
            if (this.apprClassesCount < classes.size()) {
                this.apprClassesCount = (int) (classes.size()*factor);
            }

            // reinitialize caching maps
            HashMultimap<OWLAxiom, IVecInt> newTrans = HashMultimap.create(apprAxiomsCount, 10);
            newTrans.putAll(this.translations);
            this.translations = newTrans;
            HashMultimap<Integer, IVecInt> newSymbols = HashMultimap.create(apprClassesCount, 10);
            newSymbols.putAll(this.symbolsToClauses);
            this.symbolsToClauses = newSymbols;

            return true;
        }
    }

    public Set<OWLClass> getUnsatClasses() {
        return unSatClasses;
    }

    public void setOWLSatStructure(OWLSatStructure owlSatStructure) {
        this.owlSatStructure = owlSatStructure;
    }

    public OWLSatReasoner.OWLSatStructure getOWLSatStructure() {
        return owlSatStructure;
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

    /*
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
                Core hornCore = core.extractCore(head);
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
                Core core = new Core(this);
                core.useOnlyHornClauses = true;
                Core hornCore = core.extractCore(literal);
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
     */

}
