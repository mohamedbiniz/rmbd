package at.ainf.owlapi3.model.intersection;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static at.ainf.owlapi3.util.SetUtils.createIntersection;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractOWLIntersectionExtractor implements OWLIntersectionExtractor {

    private static Logger logger = LoggerFactory.getLogger(AbstractOWLIntersectionExtractor.class.getName());

    @Override
    public Set<OWLLogicalAxiom> calculateMinModule(Set<OWLLogicalAxiom> axioms) {
        List<Set<OWLLogicalAxiom>> modules = calculateModules(axioms);
        Set<OWLLogicalAxiom> intersection = createIntersection(modules);

        logger.info ("size of module intersection: " + intersection.size());
        if (intersection.size() == axioms.size())
            return intersection;

        return calculateMinModule(intersection);
    }

    protected abstract List<Set<OWLLogicalAxiom>> calculateModules (Set<OWLLogicalAxiom> axioms);

}
