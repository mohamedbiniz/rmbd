package at.ainf.owlapi3.module;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.02.13
 * Time: 15:30
 * To change this template use File | Settings | File Templates.
 */
public class OtfModuleProvider extends AbstractOWLModuleProvider {

    private static Logger logger = LoggerFactory.getLogger(OtfModuleProvider.class.getName());

    OWLReasoner reasoner;

    OWLOntology reasonerOntology;

    public OtfModuleProvider(OWLOntology ontology, OWLReasonerFactory factory, boolean isElOnto) {
        super(ontology, factory, isElOnto);
        reasonerOntology = createOntology(new HashSet<OWLAxiom>());
        reasoner = getReasonerFactory().createNonBufferingReasoner(reasonerOntology);
    }

    private OWLClass unsatClassOfSmallestModule = null;

    public OWLClass getUnsatClass() {
        return unsatClassOfSmallestModule;
    }

    protected void setUnsatClass(OWLClass unsatClassOfSmallestModule) {
        this.unsatClassOfSmallestModule = unsatClassOfSmallestModule;
    }

    private String getUnsatClassesAsString(List<OWLClass> unsatClasses) {
        String res = "";
        for (OWLClass unsatClass : unsatClasses)
            res += getNumber(unsatClass) + ", ";
        return res;
    }

    List<OWLClass> knowUnsatClasses = new LinkedList<OWLClass>();

    private int getNumber(OWLClass unsatClass) {
        if (!knowUnsatClasses.contains(unsatClass))
            knowUnsatClasses.add(unsatClass);
        return knowUnsatClasses.indexOf(unsatClass);
    }

    List<OWLClass> usedUnsatClasses = new LinkedList<OWLClass>();

    static long timeOverall = 0;

    @Override
    public Set<OWLLogicalAxiom> getSmallerModule(Set<OWLLogicalAxiom> axioms) {

        List<OWLClass> possibleUnsatClasses = new LinkedList<OWLClass>(getUnsatClasses().keySet());
        //possibleUnsatClasses.removeAll(usedUnsatClasses);

        // if we have not soo much axioms it could be we have already coherent set
        /*if (axioms.size()<100) {
            reasonerOntology.getOWLOntologyManager().removeAxioms(reasonerOntology,reasonerOntology.getLogicalAxioms());
            reasonerOntology.getOWLOntologyManager().addAxioms(reasonerOntology,axioms);
            if (reasoner.getUnsatisfiableClasses().getEntities().size()==1) {
                return Collections.emptySet();
            }
        }*/

        long time = System.currentTimeMillis();
        List<Set<OWLLogicalAxiom>> modulesForStillUnsat = new LinkedList<Set<OWLLogicalAxiom>>();
        Map<Set<OWLLogicalAxiom>,OWLClass> owlClassForModule = new HashMap<Set<OWLLogicalAxiom>, OWLClass>();
        for (OWLClass unsatClass : possibleUnsatClasses) {
            Set<OWLLogicalAxiom> setForUnsatClass = new HashSet<OWLLogicalAxiom>(getUnsatClasses().get(unsatClass));
            setForUnsatClass.retainAll(axioms);
            modulesForStillUnsat.add(setForUnsatClass);
            owlClassForModule.put(setForUnsatClass,unsatClass);
        }
        Collections.sort(modulesForStillUnsat,new SetComparator<OWLLogicalAxiom>());

        Set<OWLLogicalAxiom> result = null;
        for (Set<OWLLogicalAxiom> reducedModule : modulesForStillUnsat) {
            OWLClass unsatClassInModule = owlClassForModule.get(reducedModule);
            reasonerOntology.getOWLOntologyManager().removeAxioms(reasonerOntology,reasonerOntology.getLogicalAxioms());
            reasonerOntology.getOWLOntologyManager().addAxioms(reasonerOntology,reducedModule);
            if (!reasoner.isSatisfiable(unsatClassInModule)) {
                usedUnsatClasses.add(unsatClassInModule);
                setUnsatClass(unsatClassInModule);
                result = reducedModule;
                break;
            }
        }
        time = System.currentTimeMillis() - time;
        timeOverall += time;
        logger.info("timeOverall needed to search smaller module: " + timeOverall);

        if (result == null)
            return Collections.emptySet();

        return result;
    }

    /*@Override
    public Set<OWLLogicalAxiom> getSmallerModule(Set<OWLLogicalAxiom> axioms) {

        LinkedList<OWLClass> topUnsatClasses = new LinkedList<OWLClass>(getUnsatClasses().keySet());
        // is expensive, alternative?
        List<OWLClass> stillUnsat = getStillUnsat(topUnsatClasses, axioms);

        List<Set<OWLLogicalAxiom>> modulesForStillUnsat = new LinkedList<Set<OWLLogicalAxiom>>();
        Map<Set<OWLLogicalAxiom>,OWLClass> owlClassForModule = new HashMap<Set<OWLLogicalAxiom>, OWLClass>();
        for (OWLClass unsatClass : stillUnsat) {
            modulesForStillUnsat.add(getUnsatClasses().get(unsatClass));
            owlClassForModule.put(getUnsatClasses().get(unsatClass),unsatClass);
        }
        Set<OWLLogicalAxiom> smallest = Collections.min(modulesForStillUnsat,new SetComparator<OWLLogicalAxiom>());
        //setUnsatClass(owlClassForModule.get(smallest));

        Set<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>(smallest);
        result.retainAll(axioms);

        List<OWLClass> stillUnsat2 = getStillUnsat(topUnsatClasses, result);
        logger.info("still unsat size: " + stillUnsat2.size());

        return result;
    }*/



}
