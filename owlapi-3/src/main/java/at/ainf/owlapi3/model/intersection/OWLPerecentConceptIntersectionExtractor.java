package at.ainf.owlapi3.model.intersection;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static at.ainf.owlapi3.util.OWLUtils.calculateSignature;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public class OWLPerecentConceptIntersectionExtractor extends OWLRestConceptIntersectionExtractor {

    private double percent;

    public OWLPerecentConceptIntersectionExtractor(double percent) {
        super(-1);
        this.percent = percent;
    }

    @Override
    protected List<Set<OWLLogicalAxiom>> calculateModules (Set<OWLLogicalAxiom> axioms) {
        size = (int)(new LinkedList<OWLClass>(calculateSignature(axioms)).size() * percent);
        if (size == 0)
            return Collections.singletonList(axioms);
        return super.calculateModules(axioms);
    }

}
