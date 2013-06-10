package at.ainf.owlapi3.model.intersection;

import at.ainf.owlapi3.model.OWLModuleExtractor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ainf.owlapi3.util.OWLUtils.calculateSignature;
import static at.ainf.owlapi3.util.SetUtils.createIntersection;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */
public class OWLEqualIntersectionExtractor extends AbstractOWLIntersectionExtractor {

    private static Logger logger = LoggerFactory.getLogger(OWLEqualIntersectionExtractor.class.getName());

    private int split;

    public OWLEqualIntersectionExtractor(int split) {
        this.split = split;
    }

    @Override
    protected List<Set<OWLLogicalAxiom>> calculateModules (Set<OWLLogicalAxiom> axioms) {
        List<OWLClass> signature = new LinkedList<OWLClass>(calculateSignature(axioms));
        OWLModuleExtractor extractor = new OWLModuleExtractor(axioms);

        List<List<OWLClass>> subsignatures = calculateSubSignatures(signature);

        List<Set<OWLLogicalAxiom>> submodules = new LinkedList<Set<OWLLogicalAxiom>>();
        for (List<OWLClass> s : subsignatures)
            submodules.add(extractor.calculateModule(s));

        return submodules;
    }

    protected List<List<OWLClass>> calculateSubSignatures(List<OWLClass> signature) {
        int size = signature.size() / split;
        List<List<OWLClass>> subsignatures = new LinkedList<List<OWLClass>>();
        for (int i = 0; i < split; i++) {
            LinkedList<OWLClass> s = new LinkedList<OWLClass>(signature.subList(size * i, size * (i + 1)));
            subsignatures.add(s);
        }
        return  subsignatures;
    }

}
