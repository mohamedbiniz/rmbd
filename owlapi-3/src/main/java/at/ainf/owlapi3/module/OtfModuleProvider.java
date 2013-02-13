package at.ainf.owlapi3.module;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

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

    public OtfModuleProvider(OWLOntology ontology, OWLReasonerFactory factory, boolean isElOnto) {
        super(ontology, factory, isElOnto);
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

    @Override
    public Set<OWLLogicalAxiom> getSmallerModule(Set<OWLLogicalAxiom> axioms) {

        LinkedList<OWLClass> topUnsatClasses = new LinkedList<OWLClass>(getUnsatClasses().keySet());
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
    }



}
