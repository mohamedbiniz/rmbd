package at.ainf.owlapi3.test.modules;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 01.03.13
 * Time: 09:00
 * To change this template use File | Settings | File Templates.
 */
public class DistanceToTopComparator implements OWLClassComparator {

    private static final OWLClass TOP_CLASS = OWLManager.getOWLDataFactory().getOWLThing();

    Map<OWLClass,Integer> map;

    public DistanceToTopComparator(OWLOntology ontology) {
        map = getDistancesFromTop(ontology);
    }

    protected Map<OWLClass,Integer> getDistancesFromTop(OWLOntology ontology) {
        StructuralReasoner structuralReasoner = new StructuralReasoner (ontology,
                new SimpleConfiguration(), BufferingMode.NON_BUFFERING);

        Set<OWLClass> toClassify = new HashSet<OWLClass>(ontology.getClassesInSignature());
        toClassify.remove(TOP_CLASS);
        Map<OWLClass, Integer> result = new HashMap<OWLClass, Integer>();

        Set<OWLClass> actualParents = Collections.singleton(TOP_CLASS);
        int distance = 1;
        while(!toClassify.isEmpty()) {
            Set<OWLClass> childs = getSubClasses(structuralReasoner,actualParents);
            childs.retainAll(toClassify);
            for (OWLClass child : childs) {
                toClassify.remove(child);
                result.put(child,distance);
            }

            actualParents = childs;
            distance++;
        }


        return result;
    }

    public Set<OWLClass> getSubClasses (StructuralReasoner reasoner, Set<OWLClass> unsatClasses) {
        Set<OWLClass> result = new HashSet<OWLClass>();

        for (OWLClass unsatClass : unsatClasses)
            result.addAll(reasoner.getSubClasses(unsatClass,true).getFlattened());

        return result;
    }

    @Override
    public Integer getMeasure(OWLClass unsatClass) {
        return map.get(unsatClass);
    }

    @Override
    public int compare(OWLClass o1, OWLClass o2) {
        return map.get(o1).compareTo(map.get(o2));
    }
}
