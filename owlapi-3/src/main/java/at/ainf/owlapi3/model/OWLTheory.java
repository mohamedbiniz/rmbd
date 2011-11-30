package at.ainf.owlapi3.model;

import at.ainf.diagnosis.model.AbstractTheory;
import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.storage.HittingSet;
import at.ainf.diagnosis.tree.NodeCostsEstimator;
import at.ainf.owlapi3.debugging.OWLNegateAxiom;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

import static _dev.TimeLog.start;
import static _dev.TimeLog.stop;

//import org.semanticweb.HermiT.Reasoner;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 13.01.2010
 * Time: 14:21:13
 * To change this template use File | Settings | File Templates.
 */
public class OWLTheory extends AbstractTheory<OWLReasoner, OWLLogicalAxiom> implements
        ITheory<OWLLogicalAxiom> {

    private static Logger logger = Logger.getLogger(OWLTheory.class.getName());

    private OWLOntologyManager owlOntologyManager;

    private OWLOntology ontology;

    private OWLOntology original;

    private boolean includeTrivialEntailments = true;
    private final boolean BUFFERED_SOLVER = true;
    private boolean REDUCE_TO_UNSAT = false;

    public boolean isIncludeTrivialEntailments() {
        return includeTrivialEntailments;
    }

    public void setIncludeTrivialEntailments(boolean includeTrivialEntailments) {
        this.includeTrivialEntailments = includeTrivialEntailments;
    }

    public void doBayesUpdate(Set<? extends HittingSet<OWLLogicalAxiom>> hittingSets) {
        for (HittingSet<OWLLogicalAxiom> hs : hittingSets) {
            for (int i = 0; i < getTestsSize(); i++) {
                Set<OWLLogicalAxiom> testcase = getTest(i);
                List<Set<OWLLogicalAxiom>> olderTestcases = getTests(0, i);
                Set<OWLLogicalAxiom> positive = getPositiveTests(olderTestcases);

                double value = (hs.getMeasure() / 2) > 0 ? (hs.getMeasure() / 2) : Double.MIN_VALUE;

                if (getTypeOfTest(testcase)) {
                    if (!diagnosisEntails(hs, testcase, positive)) {
                        hs.setMeasure(value);
                    }
                } else {
                    if (diagnosisConsistent(hs, testcase, positive)) {
                        hs.setMeasure(value);
                    }
                }
            }
        }
    }

    private Set<OWLLogicalAxiom> getPositiveTests(List<Set<OWLLogicalAxiom>> list) {
        Set<OWLLogicalAxiom> set = new HashSet<OWLLogicalAxiom>();

        for (Set<OWLLogicalAxiom> testcase : list) {
            if (getTypeOfTest(testcase))
                set.addAll(testcase);
        }

        return set;

    }


    protected static final OWLClass TOP_CLASS = OWLDataFactoryImpl.getInstance().getOWLThing();
    protected static final OWLClass BOTTOM_CLASS = OWLDataFactoryImpl.getInstance().getOWLNothing();

    protected boolean isReduceToUnsat() {
        return REDUCE_TO_UNSAT;
    }

    public OWLTheory(OWLReasonerFactory reasonerFactory, OWLOntology ontology)
            throws UnsatisfiableFormulasException, SolverException {
        this(reasonerFactory, ontology, Collections.<OWLLogicalAxiom>emptySet());
    }

    public OWLTheory(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Set<OWLLogicalAxiom> backgroundAxioms, boolean reduce)
            throws UnsatisfiableFormulasException, SolverException {


        OWLOntologyManager man = ontology.getOWLOntologyManager();
        setOwlOntologyManager(man);

        try {
            OWLOntology dontology = owlOntologyManager.createOntology();
            OWLLiteral lit = owlOntologyManager.getOWLDataFactory().getOWLLiteral("Test Ontology");
            IRI iri = OWLRDFVocabulary.RDFS_COMMENT.getIRI();
            OWLAnnotation anno = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(iri), lit);
            owlOntologyManager.applyChange(new AddOntologyAnnotation(dontology, anno));
            this.ontology = dontology;
            if (BUFFERED_SOLVER)
                setSolver(reasonerFactory.createReasoner(this.ontology));
            else
                setSolver(reasonerFactory.createNonBufferingReasoner(this.ontology));
        } catch (OWLOntologyCreationException e) {
            throw new OWLRuntimeException(e);
        }

        Set<OWLLogicalAxiom> logicalAxioms = ontology.getLogicalAxioms();
        addActiveFormulas(setminus(logicalAxioms, backgroundAxioms));
        this.original = ontology;

        // add all axioms from imported ontologies, as well as background axioms into a new test ontology
        //OWLOntology dontology = owlOntologyManager.createOntology(IRI.create("http://ontology.ainf.at/debugging" + System.nanoTime()));
        Set<OWLOntology> importsClosure = man.getImportsClosure(ontology);
        for (OWLOntology ont : importsClosure) {
            if (!ont.equals(ontology))
                for (OWLLogicalAxiom ax : ont.getLogicalAxioms()) {
                    addBackgroundFormula(ax);
                }
        }

        addBackgroundFormulas(backgroundAxioms);

        if (reduce) {
            REDUCE_TO_UNSAT = true;
            updateAxioms(getOntology(), logicalAxioms, backgroundAxioms);
            getSolver().flush();
            if (getSolver().isConsistent()) {
                Set<OWLClass> entities = getSolver().getUnsatisfiableClasses().getEntities();
                updateAxioms(getOntology(), Collections.<OWLLogicalAxiom>emptySet());
                entities.remove(BOTTOM_CLASS);
                if (!entities.isEmpty()) {
                    String iri = "http://ainf.at/testiri#";
                    for (OWLClass cl : entities) {
                        OWLDataFactory fac = man.getOWLDataFactory();
                        OWLIndividual test_individual = fac.getOWLNamedIndividual(IRI.create(iri + "d_" + cl.getIRI().getFragment()));

                        addBackgroundFormula(fac.getOWLClassAssertionAxiom(cl, test_individual));
                    }
                }
            } else
                updateAxioms(getOntology(), Collections.<OWLLogicalAxiom>emptySet());
        }
    }

    public OWLTheory(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Set<OWLLogicalAxiom> backgroundAxioms)
            throws UnsatisfiableFormulasException, SolverException {
        this(reasonerFactory,ontology,backgroundAxioms,false);
    }

    public OWLOntology getOriginalOntology() {
        return this.original;
    }

    public OWLOntology getOntology() {
        return this.ontology;
    }

    public OWLOntologyManager getOwlOntologyManager() {
        return owlOntologyManager;
    }

    protected void setOwlOntologyManager(OWLOntologyManager owlOntologyManager) {
        this.owlOntologyManager = owlOntologyManager;
    }

    private OWLNegateAxiom vis = null;

    public OWLLogicalAxiom negate(OWLLogicalAxiom ax) {
        if (getNegationVisitor() == null)
            vis = new OWLNegateAxiom(getOwlOntologyManager().getOWLDataFactory());
        OWLLogicalAxiom negated = (OWLLogicalAxiom) ax.accept(getNegationVisitor());
        return negated;
    }

    public void registerTestCases() throws SolverException, UnsatisfiableFormulasException {
        Set<OWLLogicalAxiom> tests = new HashSet<OWLLogicalAxiom>();
        for (Set<? extends OWLLogicalAxiom> testCase : getPositiveTests())
            tests.addAll(testCase);
        for (Set<? extends OWLLogicalAxiom> testCase : getEntailedTests())
            tests.addAll(testCase);

        addBackgroundFormulas(tests);
    }

    public void unregisterTestCases() throws SolverException {
        Set<OWLLogicalAxiom> tests = new HashSet<OWLLogicalAxiom>();
        for (Set<? extends OWLLogicalAxiom> testCase : getPositiveTests())
            tests.addAll(testCase);
        for (Set<? extends OWLLogicalAxiom> testCase : getEntailedTests())
            tests.addAll(testCase);
        removeBackgroundFormulas(tests);
    }


    public boolean testDiagnosis(Collection<OWLLogicalAxiom> diag) throws SolverException {
        // clean up formula stack
        pop(getTheoryCount());
        List<OWLLogicalAxiom> kb = new LinkedList<OWLLogicalAxiom>(getActiveFormulas());
        // apply diagnosis
        kb.removeAll(diag);
        push(kb);
        push(getBackgroundFormulas());
        for (Set<OWLLogicalAxiom> test : getEntailedTests()) {
            push(test);
        }

        for (Set<OWLLogicalAxiom> test : getPositiveTests()) {
            push(test);
        }

        if (!isConsistent()) {
            pop(getTheoryCount());
            return false;
        }

        for (Set<OWLLogicalAxiom> test : getNegativeTests()) {
            if (!isEntailed(test)) {
                pop(getTheoryCount());
                return false;
            }
        }

        for (Set<OWLLogicalAxiom> test : getNonentailedTests()) {
            if (isEntailed(test)) {
                pop(getTheoryCount());
                return false;
            }
        }

        pop(getTheoryCount());
        return true;
    }

    protected boolean isTestConsistent() throws SolverException {
        // clear stack
        pop(getTheoryCount());
        for (Set<OWLLogicalAxiom> test : getPositiveTests()) {
            push(test);
        }
        for (Set<OWLLogicalAxiom> test : getEntailedTests()) {
            push(test);
        }
        if (!isConsistent()) {
            pop(getTheoryCount());
            return false;
        }

        // verify negative tests
        for (Set<OWLLogicalAxiom> test : getNegativeTests()) {
            if (isEntailed(test)) {
                pop(getTheoryCount());
                return false;
            }
        }

        // verify negative tests
        for (Set<OWLLogicalAxiom> test : getNonentailedTests()) {
            if (isEntailed(test)) {
                pop(getTheoryCount());
                return false;
            }
        }

        pop(getTheoryCount());
        return true;
    }

    @Override
    protected boolean verifyConsistency() {
        start("Overall consistency check including management");
        updateAxioms(getOntology(), getFormulaStack(), getBackgroundFormulas());
        boolean consistent = doConsistencyTest(getSolver());
        //removeAxioms(getBackgroundFormulas(), getOntology());
        //removeAxioms(getFormulaStack(), getOntology());
        stop();
        if (logger.isTraceEnabled())
            logger.trace(getOntology().getOntologyID() + " is consistent: " + consistent);
        return consistent;
    }

    protected boolean doConsistencyTest(OWLReasoner reasoner) {
        boolean consistent, coherent = true;

        start("Reasoner sync ");
        if (BUFFERED_SOLVER) reasoner.flush();
        stop();
        start("Consistency test");
        consistent = reasoner.isConsistent();
        stop();
        start("Coherency test");
        if (!isReduceToUnsat() && consistent) {
            coherent = checkCoherency(reasoner);
        }
        stop();
        consistent = consistent && coherent;
        if (consistent) {
            if (checkTestsConsistency()) return false;
        }
        return consistent;
    }

    private boolean checkCoherency(OWLReasoner reasoner) {
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        return reasoner.getBottomClassNode().isSingleton();
    }

    private boolean checkTestsConsistency() {
        for (Set<OWLLogicalAxiom> test : getNegativeTests()) {
            if (!isEntailed(test)) {
                return true;
            }
        }

        for (Set<OWLLogicalAxiom> test : getNonentailedTests()) {
            if (isEntailed(test)) {
                return true;
            }
        }
        return false;
    }


    public boolean isEntailed(Set<OWLLogicalAxiom> test) {
        start("Consistency + entailment");
        updateAxioms(getOntology(), getFormulaStack());
        OWLReasoner solver = getSolver();
        if (BUFFERED_SOLVER) solver.flush();
        if (!solver.isConsistent())
            return false;
        //solver.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        boolean res = solver.isEntailed(test);
        //removeAxioms(getFormulaStack(), getOntology());
        //if (BUFFERED_SOLVER) solver.flush();
        stop();
        return res;
    }

    public void updateAxioms(OWLOntology ontology, Set<OWLLogicalAxiom>... axioms) {
        Set<OWLLogicalAxiom> add = new HashSet<OWLLogicalAxiom>();
        Set<OWLLogicalAxiom> remove = new HashSet<OWLLogicalAxiom>();

        for (Set<OWLLogicalAxiom> axiomset : axioms) {
            for (OWLLogicalAxiom axiom : axiomset) {
                if (!ontology.containsAxiom(axiom))
                    add.add(axiom);
            }
        }

        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
            boolean rem = true;
            for (Set<OWLLogicalAxiom> axiomset : axioms)
                if (axiomset.contains(axiom))
                    rem = false;
            if (rem)
                remove.add(axiom);
        }
        if (!add.isEmpty())
            getOwlOntologyManager().addAxioms(ontology, add);
        if (!remove.isEmpty())
            getOwlOntologyManager().removeAxioms(ontology, remove);
    }


    public void addAxioms(Set<OWLLogicalAxiom> axioms, OWLOntology ontology) {
        getOwlOntologyManager().addAxioms(ontology, axioms);
        //for (OWLLogicalAxiom ax : axioms) {
        //    getOwlOntologyManager().addAxiom(ontology, ax);
        //}
    }

    public void removeAxioms(Set<OWLLogicalAxiom> axioms, OWLOntology ontology) {
        getOwlOntologyManager().removeAxioms(ontology, axioms);
        //for (OWLLogicalAxiom ax : axioms) {
        //    getOwlOntologyManager().removeAxioms(ontology, axioms);
        //}

    }

    protected OWLNegateAxiom getNegationVisitor() {
        return vis;
    }

    public boolean diagnosisEntails(HittingSet<OWLLogicalAxiom> hs, Set<OWLLogicalAxiom> ent) {
        // cleanup stack
        Collection<OWLLogicalAxiom> stack = getFormulaStack();
        pop(getTheoryCount());
        // cleanup ontology
        //Set<OWLLogicalAxiom> logicalAxioms = getOntology().getLogicalAxioms();
        //removeAxioms(logicalAxioms, getOntology());

        // add entailed test cases to simulate extension EX
        for (Set<OWLLogicalAxiom> test : getEntailedTests()) {
            push(test);
        }
        // add axioms to the ontology
        push(setminus(getOriginalOntology().getLogicalAxioms(), hs));
        push(getBackgroundFormulas());
        //removeAxioms(hs, getOntology());
        //addAxioms(, getOntology());

        boolean res = isEntailed(new LinkedHashSet<OWLLogicalAxiom>(ent));

        // restore the state of the theory prior to the test
        pop(getTheoryCount());
        //updateAxioms(getOntology(), logicalAxioms);
        push(stack);
        return res;
    }

    public boolean diagnosisEntails(HittingSet<OWLLogicalAxiom> hs, Set<OWLLogicalAxiom> ent, Set<OWLLogicalAxiom> axioms) {
        // cleanup stack
        Collection<OWLLogicalAxiom> stack = getFormulaStack();
        pop(getTheoryCount());
        // cleanup ontology
        //Set<OWLLogicalAxiom> logicalAxioms = getOntology().getLogicalAxioms();
        //removeAxioms(logicalAxioms, getOntology());

        // add entailed test cases to simulate extension EX
        for (Set<OWLLogicalAxiom> test : getEntailedTests()) {
            push(test);
        }
        // add axioms to the ontology
        push(setminus(getOriginalOntology().getLogicalAxioms(), hs));
        push(getBackgroundFormulas());
        push(axioms);
        //removeAxioms(hs, getOntology());
        //addAxioms(, getOntology());

        boolean res = isEntailed(new LinkedHashSet<OWLLogicalAxiom>(ent));

        // restore the state of the theory prior to the test
        pop(getTheoryCount());
        //updateAxioms(getOntology(), logicalAxioms);
        push(stack);
        return res;
    }

    protected Set<OWLLogicalAxiom> setminus(Set<OWLLogicalAxiom> logicalAxioms, Set<OWLLogicalAxiom> hs) {
        Set<OWLLogicalAxiom> res = new LinkedHashSet<OWLLogicalAxiom>(logicalAxioms);
        res.removeAll(hs);
        return res;
    }

    public boolean diagnosisConsistent(HittingSet<OWLLogicalAxiom> hs, Set<OWLLogicalAxiom> ent) {
        // cleanup stack
        Collection<OWLLogicalAxiom> stack = getFormulaStack();
        pop(getTheoryCount());
        // cleanup ontology
        //Set<OWLLogicalAxiom> logicalAxioms = getOntology().getLogicalAxioms();
        //removeAxioms(logicalAxioms, getOntology());

        // add entailed test cases to simulate extension EX
        for (Set<OWLLogicalAxiom> test : getEntailedTests()) {
            push(test);
        }
        // add axioms to the ontology
        push(setminus(getActiveFormulas(), hs));
        push(getBackgroundFormulas());
        push(ent);
        //addAxioms(getOriginalOntology().getLogicalAxioms(), getOntology());
        //removeAxioms(hs, getOntology());
        //addAxioms(ent, getOntology());
        //addAxioms(getBackgroundFormulas(), getOntology());

        boolean res = verifyConsistency();

        // restore the state of the theory prior to the test
        pop(getTheoryCount());
        //removeAxioms(ent, getOntology());
        //removeAxioms(getBackgroundFormulas(), getOntology());
        //removeAxioms(getOriginalOntology().getLogicalAxioms(), getOntology());
        //addAxioms(logicalAxioms, getOntology());
        //updateAxioms(getOntology(), logicalAxioms);
        push(stack);
        return res;
    }

    public boolean diagnosisConsistent(HittingSet<OWLLogicalAxiom> hs, Set<OWLLogicalAxiom> ent, Set<OWLLogicalAxiom> axioms) {
        // cleanup stack
        Collection<OWLLogicalAxiom> stack = getFormulaStack();
        pop(getTheoryCount());
        // cleanup ontology
        //Set<OWLLogicalAxiom> logicalAxioms = getOntology().getLogicalAxioms();
        //removeAxioms(logicalAxioms, getOntology());

        // add entailed test cases to simulate extension EX
        for (Set<OWLLogicalAxiom> test : getEntailedTests()) {
            push(test);
        }
        // add axioms to the ontology
        push(setminus(getActiveFormulas(), hs));
        push(getBackgroundFormulas());
        push(axioms);
        push(ent);
        //addAxioms(getOriginalOntology().getLogicalAxioms(), getOntology());
        //removeAxioms(hs, getOntology());
        //addAxioms(ent, getOntology());
        //addAxioms(getBackgroundFormulas(), getOntology());

        boolean res = verifyConsistency();

        // restore the state of the theory prior to the test
        pop(getTheoryCount());
        //removeAxioms(ent, getOntology());
        //removeAxioms(getBackgroundFormulas(), getOntology());
        //removeAxioms(getOriginalOntology().getLogicalAxioms(), getOntology());
        //addAxioms(logicalAxioms, getOntology());
        //updateAxioms(getOntology(), logicalAxioms);
        push(stack);
        return res;
    }

    public final Set<OWLLogicalAxiom> getEntailments(Set<OWLLogicalAxiom> hittingSet) throws SolverException {

        OWLReasoner reasoner = getSolver();

        Set<OWLLogicalAxiom> axioms = setminus(getActiveFormulas(), hittingSet);
        Collection<OWLLogicalAxiom> stack = getFormulaStack();
        pop(getTheoryCount());

        push(axioms);
        push(getBackgroundFormulas());

        if (!verifyConsistency()) {
            pop(getTheoryCount());
            push(stack);
            return null;
        }

        Set<OWLLogicalAxiom> entailments = extractEntailments(reasoner, getOwlOntologyManager());

        pop(getTheoryCount());
        push(stack);
        return entailments;
    }

    private boolean includeSubClassOfAxioms = false;

    private boolean includeClassAssertionAxioms = false;

    private boolean includeEquivalentClassAxioms = false;

    private boolean includePropertyAssertAxioms = false;

    private boolean includeDisjointClassAxioms = false;

    private boolean includeOntologyAxioms = true;

    private boolean includeReferencingThingAxioms = true;

    public boolean isIncludeSubClassOfAxioms() {
        return includeSubClassOfAxioms;
    }

    public void setIncludeSubClassOfAxioms(boolean includeSubClassOfAxioms) {
        this.includeSubClassOfAxioms = includeSubClassOfAxioms;
    }

    public boolean isIncludeClassAssertionAxioms() {
        return includeClassAssertionAxioms;
    }

    public void setIncludeClassAssertionAxioms(boolean includeClassAssertionAxioms) {
        this.includeClassAssertionAxioms = includeClassAssertionAxioms;
    }

    public boolean isIncludeEquivalentClassAxioms() {
        return includeEquivalentClassAxioms;
    }

    public void setIncludeEquivalentClassAxioms(boolean includeEquivalentClassAxioms) {
        this.includeEquivalentClassAxioms = includeEquivalentClassAxioms;
    }

    public boolean isIncludePropertyAssertAxioms() {
        return includePropertyAssertAxioms;
    }

    public void setIncludePropertyAssertAxioms(boolean includePropertyAssertAxioms) {
        this.includePropertyAssertAxioms = includePropertyAssertAxioms;
    }

    public boolean isIncludeDisjointClassAxioms() {
        return includeDisjointClassAxioms;
    }

    public void setIncludeDisjointClassAxioms(boolean includeDisjointClassAxioms) {
        this.includeDisjointClassAxioms = includeDisjointClassAxioms;
    }

    public boolean isIncludeOntologyAxioms() {
        return includeOntologyAxioms;
    }

    public void setIncludeOntologyAxioms(boolean includeOntologyAxioms) {
        this.includeOntologyAxioms = includeOntologyAxioms;
    }

    public boolean isIncludeReferencingThingAxioms() {
        return includeReferencingThingAxioms;
    }

    public void setIncludeReferencingThingAxioms(boolean includeReferencingThingAxioms) {
        this.includeReferencingThingAxioms = includeReferencingThingAxioms;
    }

    protected Set<OWLLogicalAxiom> extractEntailments(OWLReasoner reasoner, OWLOntologyManager manager) {
        List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> axiomGenerators =
                new LinkedList<InferredAxiomGenerator<? extends OWLLogicalAxiom>>();

        if (isIncludeSubClassOfAxioms())
            axiomGenerators.add(new InferredSubClassAxiomGenerator());
        if (isIncludeClassAssertionAxioms())
            axiomGenerators.add(new InferredClassAssertionAxiomGenerator());
        if (isIncludeEquivalentClassAxioms())
            axiomGenerators.add(new InferredEquivalentClassAxiomGenerator());
        if (isIncludeDisjointClassAxioms())
            axiomGenerators.add(new InferredDisjointClassesAxiomGenerator());
        if (isIncludePropertyAssertAxioms())
            axiomGenerators.add(new InferredPropertyAssertionGenerator());

        /*
        axiomGenerators.add(new InferredSubClassAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentClassAxiomGenerator());
        axiomGenerators.add(new InferredClassAssertionAxiomGenerator());
        axiomGenerators.add(new InferredPropertyAssertionGenerator());
        /*

           // axiomGenerators.add(new InferredDisjointClassesAxiomGenerator());




            axiomGenerators.add(new InferredSubObjectPropertyAxiomGenerator());
              axiomGenerators.add(new InferredSubDataPropertyAxiomGenerator());

              axiomGenerators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
              axiomGenerators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
              axiomGenerators.add(new InferredInverseObjectPropertiesAxiomGenerator());
              axiomGenerators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
              axiomGenerators.add(new InferredSubDataPropertyAxiomGenerator());
              axiomGenerators.add(new InferredSubObjectPropertyAxiomGenerator());
              axiomGenerators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
        */

        InferenceType[] infType = new InferenceType[]{InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS,
                InferenceType.DISJOINT_CLASSES, InferenceType.DIFFERENT_INDIVIDUALS, InferenceType.SAME_INDIVIDUAL};
        if (!axiomGenerators.isEmpty())
            reasoner.precomputeInferences(infType);

        Set<OWLLogicalAxiom> entailments = new TreeSet<OWLLogicalAxiom>();
        //addDisjoint(reasoner, manager, entailments);
        //addSubclass(reasoner, manager, entailments);
        for (InferredAxiomGenerator<? extends OWLLogicalAxiom> axiomGenerator : axiomGenerators) {
            for (OWLLogicalAxiom ax : axiomGenerator.createAxioms(manager, reasoner)) {
                 if (!getOntology().containsAxiom(ax) || isIncludeOntologyAxioms())
                    if (!ax.getClassesInSignature().contains(TOP_CLASS) || isIncludeReferencingThingAxioms()) {
                        entailments.add(ax);
                     }

                //if (includeTrivialEntailments || (!getOntology().containsAxiom(ax) && !ax.getClassesInSignature().contains(TOP_CLASS)))
                //    entailments.add(ax);
            }
        }

        if (isIncludeOntologyAxioms())
            entailments.addAll(getOntology().getLogicalAxioms());
        return entailments;
    }

    /*private void addDisjoint(OWLReasoner reasoner, OWLOntologyManager manager, Set<OWLLogicalAxiom> entailments) {
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        for (OWLClass cls : getOntology().getClassesInSignature()) {
            for (OWLClass dis : ((Reasoner) reasoner).getDisjointClasses(cls).getFlattened()) {
                OWLLogicalAxiom ax = dataFactory.getOWLDisjointClassesAxiom(cls, dis);
                if (includeTrivialEntailments || (!getOntology().containsAxiom(ax) && !ax.getClassesInSignature().contains(TOP_CLASS)))
                    entailments.add(ax);
            }
        }*/
    //}

    private void addSubclass(OWLReasoner reasoner, OWLOntologyManager manager, Set<OWLLogicalAxiom> entailments) {
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        for (OWLClass cls : getOntology().getClassesInSignature()) {
            for (OWLClass dis : reasoner.getSubClasses(cls, true).getFlattened()) {
                OWLLogicalAxiom ax = dataFactory.getOWLSubClassOfAxiom(dis, cls);
                if (includeTrivialEntailments || (!getOntology().containsAxiom(ax) && !ax.getClassesInSignature().contains(TOP_CLASS)))
                    entailments.add(ax);
            }
            ;
        }
    }

    //

}
