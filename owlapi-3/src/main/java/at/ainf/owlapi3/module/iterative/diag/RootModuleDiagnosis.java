package at.ainf.owlapi3.module.iterative.diag;

import at.ainf.owlapi3.module.iterative.ModuleDiagSearcher;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.04.13
 * Time: 08:46
 * To change this template use File | Settings | File Templates.
 */
public class RootModuleDiagnosis extends AbstractModuleDiagnosis {

    private static Logger logger = LoggerFactory.getLogger(RootModuleDiagnosis.class.getName());

    private List<OWLClass> repaired = new LinkedList<OWLClass>();

    private Map<Integer,Set<OWLClass>> dependencyTable = new HashMap<Integer, Set<OWLClass>>();

    public RootModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms,
                                    OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {
        super(mappings,ontoAxioms,factory,moduleDiagSearcher);

    }

    private List<OWLClass> computeNvClasses() {
        List<OWLClass> result = new LinkedList<OWLClass>(getModuleCalculator().getInitialUnsatClasses());
        result.removeAll(repaired);
        result.removeAll(unionOf(dependencyTable.values()));
        return result;
    }

    private List<OWLClass> computeVClasses() {
        List<OWLClass> result = new LinkedList<OWLClass>(getModuleCalculator().getInitialUnsatClasses());
        result.removeAll(repaired);
        result.retainAll(unionOf(dependencyTable.values()));
        return result;
    }

    public Set<OWLLogicalAxiom> calculateTargetDiagnosis() {
        Set<OWLLogicalAxiom> targetDiagnosis = new HashSet<OWLLogicalAxiom>();

        List<OWLClass> nvClasses = computeNvClasses();
        List<OWLClass> vClasses = computeVClasses();

        boolean bothEmpty = nvClasses.isEmpty() && vClasses.isEmpty();
        while (!bothEmpty) {

            Set<OWLLogicalAxiom> module = null;
            OWLClass actualUnsatClass = null;
            for (OWLClass unsatClass : nvClasses) {
                module = computeInitialModule(unsatClass,targetDiagnosis);
                if (!module.isEmpty()) {
                    actualUnsatClass = unsatClass;
                    break;
                }
                else {
                    repaired.add(unsatClass);
                    vClasses.remove(unsatClass);
                }
            }
            if (actualUnsatClass == null) {
                Collections.sort(vClasses, new MinDependentComparator(dependencyTable));
                for (OWLClass unsatClass : vClasses) {
                    module = computeInitialModule(unsatClass,targetDiagnosis);
                    if (!module.isEmpty()) {
                        actualUnsatClass = unsatClass;
                        break;
                    }
                    else {
                        repaired.add(unsatClass);
                        vClasses.remove(unsatClass);

                    }
                }
            }

            if (nvClasses.contains(actualUnsatClass)) {
                module = reduceClassToRootModule(actualUnsatClass, true, module);
            }
            else if (vClasses.contains(actualUnsatClass)) {
                module = reduceClassToRootModule(actualUnsatClass, false, module);
            }
            else
                throw new IllegalStateException("both sets cannot be total empty");

            Set<OWLLogicalAxiom> axioms = new LinkedHashSet<OWLLogicalAxiom>(module);
            Set<OWLLogicalAxiom> background = new LinkedHashSet<OWLLogicalAxiom>(axioms);
            background.retainAll(getOntoAxioms());
            targetDiagnosis.addAll(getDiagSearcher().calculateDiag(axioms, background));


            nvClasses = computeNvClasses();
            vClasses = computeVClasses();
            bothEmpty = nvClasses.isEmpty() && vClasses.isEmpty();
        }

        return targetDiagnosis;
    }

    protected Set<OWLLogicalAxiom> computeInitialModule (OWLClass unsatClass, Set<OWLLogicalAxiom> targetDiagnosis) {
        Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>();
        axioms.addAll(getMappings());
        axioms.addAll(getOntoAxioms());
        axioms.removeAll(targetDiagnosis);
        return getModuleCalculator().extractModule(createOntology(axioms),Collections.singleton((OWLEntity)unsatClass));
    }

    protected class MinDependentComparator<OWLClass> implements Comparator<OWLClass> {

        private Map<Integer, Set<OWLClass>> dependencyTable;

        public MinDependentComparator(Map<Integer, Set<OWLClass>> dependencyTable) {
            this.dependencyTable = dependencyTable;
        }

        @Override
        public int compare(OWLClass o1, OWLClass o2) {
            Integer o1Key = -1;
            Integer o2Key = -1;
            for (Map.Entry<Integer, Set<OWLClass>> entry : dependencyTable.entrySet()) {
                if (entry.getValue().contains(o1))
                    o1Key = entry.getKey();
                else if (entry.getValue().contains(o2))
                    o2Key = entry.getKey();

                if (o1Key != -1 && o2Key != -1)
                    break;
            }

            return o1Key.compareTo(o2Key);
        }

    }

    protected Set<OWLLogicalAxiom> reduceClassToRootModule(OWLClass unsatClass, boolean isNew, Set<OWLLogicalAxiom> initialModul) {



        return null;
    }

    private <X> Set<X> unionOf (Collection<Set<X>> sets) {
        Set<X> union = new HashSet<X>();
        for (Set<X> set : sets)
            union.addAll(set);
        return union;
    }

}
