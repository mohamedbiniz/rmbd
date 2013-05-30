package at.ainf.owlapi3.model;

import at.ainf.diagnosis.logging.MetricsLogger;
import at.ainf.diagnosis.logging.old.MetricsManager;
import at.ainf.diagnosis.model.AbstractReasoner;
import at.ainf.diagnosis.logging.old.IterativeStatistics;
import com.codahale.metrics.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static _dev.TimeLog.start;
import static _dev.TimeLog.stop;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:03
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerOWL extends AbstractReasoner<OWLLogicalAxiom> {


    protected static final OWLClass TOP_CLASS = OWLManager.getOWLDataFactory().getOWLThing();

    private static int cnt = 0;

    protected OWLOntology ontology;

    private OWLReasoner reasoner;
    private List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> axiomGenerators;
    private boolean includeAxiomsReferencingThing;
    private boolean includeOntologyAxioms;
    private OWLReasonerFactory reasonerFactory;

    protected int num = cnt++;
    private final OWLOntologyManager owlOntologyManager;

    public ReasonerOWL(OWLOntologyManager owlOntologyManager, OWLReasonerFactory reasonerFactory) {
        this(owlOntologyManager);

        this.reasonerFactory = reasonerFactory;
        reasoner = reasonerFactory.createReasoner(this.ontology);
    }

    protected ReasonerOWL(OWLOntologyManager owlOntologyManager) {
        try {
            this.owlOntologyManager = owlOntologyManager;

            OWLOntology dontology = owlOntologyManager.createOntology();
            OWLLiteral lit = owlOntologyManager.getOWLDataFactory().getOWLLiteral("Test Reasoner Ontology " + num);
            IRI iri = OWLRDFVocabulary.RDFS_COMMENT.getIRI();
            OWLAnnotation anno = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(iri), lit);
            owlOntologyManager.applyChange(new AddOntologyAnnotation(dontology, anno));
            this.ontology = dontology;

        } catch (OWLOntologyCreationException e) {
            throw new OWLRuntimeException(e);
        }
    }

    private MetricsManager metricsManager = MetricsManager.getInstance();

    private MetricsLogger metricsMgr = MetricsLogger.getInstance();

    @Override
    public boolean isConsistent() {
        Timer.Context timer = metricsMgr.getTimer("consistencyChecks").time();
        metricsManager.startNewTimer("consistencycheck");
        metricsManager.startNewTimer("syncbeforeconsistencycheck");
        sync();
        metricsManager.stopAndLogTimer();
        boolean r = reasoner.isConsistent();
        long time = metricsManager.stopAndLogTimer();
        timer.stop();
        IterativeStatistics.avgConsistencyTime.addValue(time);
        IterativeStatistics.avgConsistencyCheck.addValue(1L);
        return r;
    }

    public boolean isSatisfiable(OWLClass unsatClass) {
        Timer.Context timer = metricsMgr.getTimer("satisfiableChecks").time();
        metricsManager.startNewTimer("issatisfiablecheck");
        metricsManager.startNewTimer("syncbeforeissatisfiablecheck");
        sync();
        metricsManager.stopAndLogTimer();
        boolean r = reasoner.isSatisfiable(unsatClass);
        metricsManager.stopAndLogTimer();
        timer.stop();
        return r;
    }

    @Override
    public boolean isCoherent() {
        Timer.Context coherencyTimer = metricsMgr.getTimer("coherencyChecks").time();
        metricsManager.startNewTimer("iscoherencycheck");
        metricsManager.startNewTimer("syncbeforeiscoherencycheck");
        sync();
        metricsManager.stopAndLogTimer();
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        boolean r = reasoner.getBottomClassNode().isSingleton();
        long time = metricsManager.stopAndLogTimer();
        coherencyTimer.stop();
        IterativeStatistics.avgCoherencyTime.addValue(time);
        IterativeStatistics.avgCoherencyCheck.addValue(1L);
        return r;
    }

    @Override
    public boolean isEntailed(Set<OWLLogicalAxiom> test) {
        sync();
        return reasoner.isEntailed(test);
    }

    @Override
    public ReasonerOWL newInstance() {
        return new ReasonerOWL(getOWLOntologyManager(), reasonerFactory);
    }

    @Override
    public Set<OWLLogicalAxiom> getEntailments() {

        sync();
        InferenceType[] infType = new InferenceType[]{InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS,
                InferenceType.DISJOINT_CLASSES, InferenceType.DIFFERENT_INDIVIDUALS, InferenceType.SAME_INDIVIDUAL};
        if (!getAxiomGenerators().isEmpty())
            reasoner.precomputeInferences(infType);

        Set<OWLLogicalAxiom> entailments = new LinkedHashSet<OWLLogicalAxiom>();
        for (InferredAxiomGenerator<? extends OWLLogicalAxiom> axiomGenerator : getAxiomGenerators()) {
            for (OWLLogicalAxiom ax : axiomGenerator.createAxioms(ontology.getOWLOntologyManager(), reasoner)) {
                if (!ontology.containsAxiom(ax) || isIncludeOntologyAxioms())
                    if (!ax.getClassesInSignature().contains(TOP_CLASS) || isIncludeAxiomsReferencingThing()) {
                        entailments.add(ax);
                    }

            }
        }

        if (isIncludeOntologyAxioms())
            entailments.addAll(ontology.getLogicalAxioms());
        return entailments;
    }

    protected void doReasonerFlush() {
        start("Reasoner sync ");
        reasoner.flush();
        stop();
    }

    @Override
    protected void updateReasonerModel(Set<OWLLogicalAxiom> axiomsToAdd, Set<OWLLogicalAxiom> axiomsToRemove) {
        if (lock != null)
            lock.lock();
        try {
            if (!axiomsToAdd.isEmpty())
                ontology.getOWLOntologyManager().addAxioms(ontology, axiomsToAdd);
            if (!axiomsToRemove.isEmpty())
                ontology.getOWLOntologyManager().removeAxioms(ontology, axiomsToRemove);
            doReasonerFlush();
        } finally {
            if (lock != null)
                lock.unlock();
        }
    }

    public void setAxiomGenerators(List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> axiomGenerators) {
        this.axiomGenerators = axiomGenerators;
    }

    public void setIncludeOntologyAxioms(boolean incOntologyAxioms) {
        includeOntologyAxioms = incOntologyAxioms;
    }

    public void setIncludeAxiomsReferencingThing(boolean incAxiomsReferencingThing) {
        includeAxiomsReferencingThing = incAxiomsReferencingThing;

    }

    public boolean isIncludeAxiomsReferencingThing() {
        return includeAxiomsReferencingThing;
    }

    public boolean isIncludeOntologyAxioms() {
        return includeOntologyAxioms;
    }

    public List<InferredAxiomGenerator<? extends OWLLogicalAxiom>> getAxiomGenerators() {
        return axiomGenerators;
    }

    public Set<OWLClass> getUnsatisfiableEntities() {
        return reasoner.getUnsatisfiableClasses().getEntities();
    }

    public String toString() {
        return "reasoner " + num;
    }

    public OWLOntologyManager getOWLOntologyManager() {
        return this.owlOntologyManager;
    }
}
