package at.ainf.owlapi3.module.iterative.modulecalc;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import at.ainf.owlapi3.reasoner.HornSatReasonerFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.06.13
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class HornModuleCalc extends ModuleCalc {

    public HornModuleCalc(OWLOntology ontology) {
        super(ontology, new HornSatReasonerFactory());
    }

    public List<OWLClass> getInitialUnsatClasses(Collection<OWLClass> excludeClasses, int maxClasses) {
        if (maxClasses > 0)
            return getHornReasoner().getSortedUnsatisfiableClasses(excludeClasses, maxClasses);
        else
            return getHornReasoner().getSortedUnsatisfiableClasses();

    }

    protected HornSatReasoner getHornReasoner() {
        return (HornSatReasoner) getReasoner();
    }

    @Override
    protected void refillActualUnsatClasses(List<OWLClass> actualUnsat, List<OWLClass> allUnsat, int maxClasses) {
        final HashSet<OWLClass> exclude = new HashSet<OWLClass>(actualUnsat);
        List<OWLClass> additionalUnsatClasses;
        do {
            additionalUnsatClasses = getInitialUnsatClasses(exclude, maxClasses - actualUnsat.size());
            exclude.addAll(additionalUnsatClasses);
            Collection<OWLClass> owlClasses = calculateModules(additionalUnsatClasses);
            actualUnsat.addAll(owlClasses);
            allUnsat.addAll(owlClasses);
        }
        while (actualUnsat.size() < maxClasses && !additionalUnsatClasses.isEmpty());
    }
}
