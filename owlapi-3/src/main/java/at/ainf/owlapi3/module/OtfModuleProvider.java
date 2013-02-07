package at.ainf.owlapi3.module;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.02.13
 * Time: 15:30
 * To change this template use File | Settings | File Templates.
 */
public class OtfModuleProvider extends AbstractOWLModuleProvider {

    public OtfModuleProvider(OWLOntology ontology, OWLReasonerFactory factory, boolean isElOnto) {
        super(ontology, factory, isElOnto);
    }

    @Override
    public Set<OWLLogicalAxiom> getSmallerModule(Set<OWLLogicalAxiom> module) {
        OWLOntology ontology = createOntology(module);
        OWLReasoner reasoner = getReasonerFactory().createNonBufferingReasoner(ontology);
        SyntacticLocalityModuleExtractor extractor = createModuleExtractor(ontology);

        Set<OWLClass> unsat = reasoner.getUnsatisfiableClasses().getEntities();
        unsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());

        if (unsat.isEmpty())
            return Collections.emptySet();

        List<Set<OWLAxiom>> modules = new LinkedList<Set<OWLAxiom>>();
        for (OWLEntity entity : unsat)
            modules.add(extractor.extract(Collections.singleton(entity)));
        Set<OWLLogicalAxiom> min = convertAxiom2LogicalAxiom(Collections.min(modules,new SetComparator<OWLAxiom>()));

        if (min.size() == module.size())
            return min;

        return getSmallerModule(min);
    }

}
