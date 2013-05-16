package at.ainf.owlapi3.module.iterative.diag;

import at.ainf.diagnosis.Speed4JMeasurement;
import at.ainf.owlapi3.module.iterative.ModuleDiagSearcher;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
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
public class RootModuleDiagnosis extends AbstractRootModuleDiagnosis {



    private static Logger logger = LoggerFactory.getLogger(RootModuleDiagnosis.class.getName());

    private Set<OWLClass> repaired = new LinkedHashSet<OWLClass>();

    private Map<OWLClass,Integer> moduleSizes = new HashMap<OWLClass, Integer>();

    public RootModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms,
                                    OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {
        super(mappings,ontoAxioms,factory,moduleDiagSearcher);

    }

    private List<OWLClass> computeUnsatClasses(Set<OWLClass> repaired) {
        List<OWLClass> result = new LinkedList<OWLClass>(getModuleCalculator().getInitialUnsatClasses());
        result.removeAll(repaired);
        return result;
    }

    private List<OWLClass> computeNvClasses(Map<Integer,Set<OWLClass>> table, Set<OWLClass> repaired) {
        List<OWLClass> result = computeUnsatClasses(repaired);
        result.removeAll(unionOf(table.values()));
        return result;
    }

    private List<OWLClass> computeVClasses(Map<Integer,Set<OWLClass>> table, Set<OWLClass> repaired) {
        List<OWLClass> result = computeUnsatClasses(repaired);
        result.retainAll(unionOf(table.values()));
        return result;
    }

    protected Set<OWLLogicalAxiom> computeModule(OWLClass unsatClass, Set<OWLLogicalAxiom> targetDiagnosis) {
        Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>();
        axioms.addAll(getMappings());
        axioms.addAll(getOntoAxioms());
        axioms.removeAll(targetDiagnosis);
        return getModuleCalculator().extractModule(createOntology(axioms),Collections.singleton((OWLEntity)unsatClass));
    }


    protected boolean isSatisfiable (OWLClass possibleUnsat, Set<OWLLogicalAxiom> module) {
        OWLReasoner reasoner = getReasonerFactory().createNonBufferingReasoner(createOntology(module));
        return reasoner.isSatisfiable(possibleUnsat);
    }

    protected boolean isAlreadyConsistent(Set<OWLLogicalAxiom> targetDiagnosis) {
        Set<OWLLogicalAxiom> axioms = new LinkedHashSet<OWLLogicalAxiom>(getOntoAxioms());
        axioms.addAll(getMappings());
        axioms.removeAll(targetDiagnosis);
        return getReasonerFactory().createNonBufferingReasoner(createOntology(axioms)).getUnsatisfiableClasses().getEntitiesMinusBottom().isEmpty();
    }

    public Set<OWLLogicalAxiom> calculateTargetDiagnosis() {
        Set<OWLLogicalAxiom> targetDiagnosis = new HashSet<OWLLogicalAxiom>();

        List<OWLClass> nvClasses = computeNvClasses(getTable(), repaired);
        List<OWLClass> vClasses = computeVClasses(getTable(),repaired);

        boolean bothEmpty = nvClasses.isEmpty() && vClasses.isEmpty();

        while (!bothEmpty) {

            Set<OWLLogicalAxiom> module = null;
            OWLClass actualUnsatClass = null;
            boolean rootModuleFound = false;
            boolean foundActualUnsatClass = false;
            Set<OWLClass> muv = new LinkedHashSet<OWLClass>();
            Speed4JMeasurement.start("for-nv");

            int counter = 0;
            boolean alreadyCheckedConsistency = false;
            int max_search_before_test = nvClasses.size() * 10 / 100;

            for (OWLClass unsatClass : nvClasses) {
                counter++;
                if ( counter > max_search_before_test && !alreadyCheckedConsistency) {
                    alreadyCheckedConsistency = true;
                    if (isAlreadyConsistent(targetDiagnosis))
                        return targetDiagnosis;
                }

                module = computeModule(unsatClass, targetDiagnosis);

                //if (module.size() > SUITABLE_MODULE_SIZE)
                //    continue;

                Set<OWLClass> possiblyUnsat = getClassesInModuleSignature(module);
                possiblyUnsat.retainAll(getModuleCalculator().getInitialUnsatClasses());
                possiblyUnsat.removeAll(repaired);
                for (OWLClass cls : possiblyUnsat) {
                    if (cls.equals(unsatClass))
                        continue;

                    Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>(module);
                    axioms.removeAll(targetDiagnosis);
                    Set<OWLLogicalAxiom> submodule = getModuleCalculator().extractModule(createOntology(axioms),Collections.singleton((OWLEntity)cls));


                    if (submodule.isEmpty()) {
                        repaired.add(cls);

                    }
                    else {
                        if (moduleSizes.containsKey(cls) && moduleSizes.get(cls).equals(submodule.size())) {
                            actualUnsatClass = cls;
                            module = submodule;
                            foundActualUnsatClass = true;
                            break;
                        }
                        else {
                            if (isSatisfiable(cls,module)) {
                                repaired.add(cls);

                            }
                            else {
                                moduleSizes.put(cls,submodule.size());

                                actualUnsatClass = cls;
                                module = submodule;
                                foundActualUnsatClass = true;
                                break;
                            }
                        }
                    }

                }

                if (foundActualUnsatClass)
                    break;


            }
            Speed4JMeasurement.stop();
            Speed4JMeasurement.start("for-v");
            if (!foundActualUnsatClass) {
                Collections.sort(vClasses, new Comparator<OWLClass>() {
                    @Override
                    public int compare(OWLClass o1, OWLClass o2) {
                        Integer o1Key = -1;
                        Integer o2Key = -1;
                        for (Map.Entry<Integer, Set<OWLClass>> entry : getTable().entrySet()) {
                            if (entry.getValue().contains(o1))
                                o1Key = entry.getKey();
                            else if (entry.getValue().contains(o2))
                                o2Key = entry.getKey();

                            if (o1Key != -1 && o2Key != -1)
                                break;
                        }

                        return o1Key.compareTo(o2Key);
                    }
                });

                int counterV = 0;
                boolean alreadyCheckedConsistencyV = false;
                int max_search_before_testV = vClasses.size() * 15 / 100;

                for (OWLClass unsatClass : vClasses) {
                    counterV++;
                    if ( counterV > max_search_before_testV && !alreadyCheckedConsistencyV) {
                        alreadyCheckedConsistencyV = true;
                        if (isAlreadyConsistent(targetDiagnosis))
                            return targetDiagnosis;
                    }

                    module = computeModule(unsatClass, targetDiagnosis);

                    //if (module.size() > SUITABLE_MODULE_SIZE)
                    //    continue;

                    Set<OWLClass> possiblyUnsat = getClassesInModuleSignature(module);
                    possiblyUnsat.retainAll(getModuleCalculator().getInitialUnsatClasses());
                    possiblyUnsat.removeAll(repaired);
                    for (OWLClass cls : possiblyUnsat) {
                        if (cls.equals(unsatClass))
                            continue;

                        Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>(module);
                        axioms.removeAll(targetDiagnosis);
                        Set<OWLLogicalAxiom> submodule = getModuleCalculator().extractModule(createOntology(axioms),Collections.singleton((OWLEntity)cls));


                        if (submodule.isEmpty()) {
                            repaired.add(cls);

                        }
                        else {
                            if (moduleSizes.containsKey(cls) && moduleSizes.get(cls).equals(submodule.size())) {
                                actualUnsatClass = cls;
                                module = submodule;
                                break;
                            }
                            else {
                                if (isSatisfiable(cls,module)) {
                                    repaired.add(cls);

                                }
                                else {
                                    moduleSizes.put(cls,submodule.size());

                                    actualUnsatClass = cls;
                                    module = submodule;
                                    break;
                                }
                            }
                        }
                    }

                    if (foundActualUnsatClass)
                        break;

                }
            }
            Speed4JMeasurement.stop();


            if (actualUnsatClass == null)
                return targetDiagnosis;

            muv = getClassesInModuleSignature(module);
            muv.retainAll(getModuleCalculator().getInitialUnsatClasses());
            muv.removeAll(repaired);
            muv.remove(actualUnsatClass);


            Map<OWLClass,Integer> table1 = new HashMap<OWLClass, Integer>();

            Set<OWLClass> s = new LinkedHashSet<OWLClass>();

            if (!rootModuleFound) {

                    //Set<OWLClass> muv = getClassesInModuleSignature(module);
                    //muv.retainAll(getModuleCalculator().getInitialUnsatClasses());
                    muv.removeAll(repaired);
                    muv.remove(actualUnsatClass);

                    if (nvClasses.contains(actualUnsatClass)) {
                        Speed4JMeasurement.start("reducetounsat");
                        module = reduceToRootModule(actualUnsatClass, true, module, table1, s, muv);
                        Speed4JMeasurement.stop();
                    }
                    else if (vClasses.contains(actualUnsatClass)) {
                        Speed4JMeasurement.start("reducetounsat1");
                        module = reduceToRootModule(actualUnsatClass, false, module, table1, s, muv);
                        Speed4JMeasurement.stop();
                    }
                    else
                        throw new IllegalStateException("both sets cannot be total empty");



            }

            if (!module.isEmpty()) {
                Set<OWLLogicalAxiom> axioms = new LinkedHashSet<OWLLogicalAxiom>(module);
                Set<OWLLogicalAxiom> background = new LinkedHashSet<OWLLogicalAxiom>(axioms);
                background.retainAll(getOntoAxioms());

                Set<OWLLogicalAxiom> possibleFaulty = new LinkedHashSet<OWLLogicalAxiom>(axioms);
                possibleFaulty.removeAll(background);
                if (possibleFaulty.size() == 1) {
                    targetDiagnosis.addAll(possibleFaulty);
                }
                else {
                    Speed4JMeasurement.start("diagnosisspeed");
                    targetDiagnosis.addAll(getDiagSearcher().calculateDiag(axioms, background));
                    Speed4JMeasurement.stop();
                }
                repaired.addAll(s);
            }

            if (!rootModuleFound) {
                updateTable(table1);

                table1.clear();
                s.clear();
            }

            nvClasses = computeNvClasses(getTable(),repaired);
            vClasses = computeVClasses(getTable(),repaired);

            bothEmpty = nvClasses.isEmpty() && vClasses.isEmpty();
        }

        return targetDiagnosis;
    }

    /*protected boolean isOntoSat(Set<OWLLogicalAxiom> targetDiagnosis) {
        Set<OWLLogicalAxiom> ontology = new LinkedHashSet<OWLLogicalAxiom>();
        ontology.addAll(getOntoAxioms());
        ontology.addAll(getMappings());
        ontology.removeAll(targetDiagnosis);
        return getReasonerFactory().createNonBufferingReasoner(createOntology(ontology)).getUnsatisfiableClasses().getEntitiesMinusBottom().size() == 0;
    }*/

    protected Set<OWLClass> computeReallyUnsatClassesAndUpdateRepaired(Set<OWLLogicalAxiom> module) {
        OWLReasoner reasoner = getReasonerFactory().createNonBufferingReasoner(createOntology(module));

        Set<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();

        Set<OWLClass> alreadyRepairedClasses = new LinkedHashSet<OWLClass>(getClassesInModuleSignature(module));
        alreadyRepairedClasses.retainAll(getModuleCalculator().getInitialUnsatClasses());
        alreadyRepairedClasses.removeAll(unsatClasses);

        repaired.addAll(alreadyRepairedClasses);

        return unsatClasses;

    }


}
