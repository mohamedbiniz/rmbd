package at.ainf.owlapi3.model;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static _dev.TimeLog.start;
import static _dev.TimeLog.stop;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.12.12
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */
public class MultipleReasonersOWL extends ReasonerOWL {


    private List<OWLReasoner> reasoners;

    private List<OWLReasonerFactory> reasonerFactories;

    public MultipleReasonersOWL(OWLOntologyManager owlOntologyManager, List<OWLReasonerFactory> reasonerFactories) {
        super(owlOntologyManager);

        this.reasonerFactories = reasonerFactories;
        reasoners = new LinkedList<OWLReasoner>();
        for (OWLReasonerFactory reasonerFactory : reasonerFactories)
            reasoners.add(reasonerFactory.createReasoner(this.ontology));
    }

    @Override
    public boolean isConsistent() {
        sync();
        for (OWLReasoner reasoner : reasoners) {
            reasoner.flush();
            if (!reasoner.isConsistent())
                 return false;
        }

        return true;
    }

    @Override
    public boolean isCoherent() {
        sync();
        for (OWLReasoner reasoner : reasoners)  {
            reasoner.flush();
            if (!reasoner.getBottomClassNode().isSingleton())
                return false;
        }

        return true;
    }

    @Override
    public boolean isEntailed(Set<OWLLogicalAxiom> test) {
        sync();

        for (OWLReasoner reasoner : reasoners)  {
            reasoner.flush();
            if (reasoner.isEntailed(test))
                return true;
        }

        return false;
    }

    @Override
    public MultipleReasonersOWL newInstance() {
        return new MultipleReasonersOWL(ontology.getOWLOntologyManager(),reasonerFactories);
    }

    private OWLReasoner getCompleteReasoner() {
        return reasoners.get(reasoners.size()-1);
    }

    @Override
    public Set<OWLLogicalAxiom> getEntailments() {

        sync();
        OWLReasoner completeReasoner = getCompleteReasoner();
        completeReasoner.flush();
        InferenceType[] infType = new InferenceType[]{InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS,
                InferenceType.DISJOINT_CLASSES, InferenceType.DIFFERENT_INDIVIDUALS, InferenceType.SAME_INDIVIDUAL};
        if (!getAxiomGenerators().isEmpty())
            completeReasoner.precomputeInferences(infType);

        Set<OWLLogicalAxiom> entailments = new LinkedHashSet<OWLLogicalAxiom>();
        for (InferredAxiomGenerator<? extends OWLLogicalAxiom> axiomGenerator : getAxiomGenerators()) {
            for (OWLLogicalAxiom ax : axiomGenerator.createAxioms(ontology.getOWLOntologyManager(), completeReasoner)) {
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

    }

    public Set<OWLClass> getUnsatisfiableEntities() {
        OWLReasoner reasoner = getCompleteReasoner();
        reasoner.flush();

        return reasoner.getUnsatisfiableClasses().getEntities();
    }

    public String toString() {
        return "reasoner " + num;
    }

}
