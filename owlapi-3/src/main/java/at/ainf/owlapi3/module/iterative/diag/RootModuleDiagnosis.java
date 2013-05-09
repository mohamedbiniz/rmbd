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
public class RootModuleDiagnosis extends AbstractModuleDiagnosis {

    private static final int MIN_MODUL_SIZE = 100;

    private static Logger logger = LoggerFactory.getLogger(RootModuleDiagnosis.class.getName());

    private Set<OWLClass> repaired = new LinkedHashSet<OWLClass>();

    private Map<Integer,Set<OWLClass>> table = new HashMap<Integer, Set<OWLClass>>();

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

    protected Set<OWLClass> getClassesInModuleSignature(Set<OWLLogicalAxiom> module) {
        Set<OWLClass> classesInModule = new LinkedHashSet<OWLClass>();
        for (OWLLogicalAxiom axiom : module)
            classesInModule.addAll (axiom.getClassesInSignature());
        return classesInModule;
    }

    protected Set<OWLLogicalAxiom> computeModule(OWLClass unsatClass, Set<OWLLogicalAxiom> targetDiagnosis) {
        Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>();
        axioms.addAll(getMappings());
        axioms.addAll(getOntoAxioms());
        axioms.removeAll(targetDiagnosis);
        return getModuleCalculator().extractModule(createOntology(axioms),Collections.singleton((OWLEntity)unsatClass));
    }

    private <X> Set<X> unionOf (Collection<Set<X>> sets) {
        Set<X> union = new HashSet<X>();
        for (Set<X> set : sets)
            union.addAll(set);
        return union;
    }


    protected void updateTable (Map<OWLClass, Integer> table1) {
        for (Map.Entry<OWLClass,Integer> entry : table1.entrySet()) {
            if (!table.containsKey(entry.getValue()))
                table.put(entry.getValue(),new LinkedHashSet<OWLClass>());

            table.get(entry.getValue()).add(entry.getKey());
        }
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

        List<OWLClass> nvClasses = computeNvClasses(table, repaired);
        List<OWLClass> vClasses = computeVClasses(table,repaired);

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
                        for (Map.Entry<Integer, Set<OWLClass>> entry : table.entrySet()) {
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

            nvClasses = computeNvClasses(table,repaired);
            vClasses = computeVClasses(table,repaired);

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


    protected Set<OWLLogicalAxiom> reduceToRootModule(OWLClass unsatClass,
                                                      boolean isNew, Set<OWLLogicalAxiom> modul, Map<OWLClass, Integer> table1, Set<OWLClass> s, Set<OWLClass> muv)
                      {

        /*Set<OWLClass> muv = getClassesInModuleSignature(modul);
        muv.removeAll(repaired);
        muv.remove(unsatClass);
        muv.removeAll(table1.keySet());
        muv.removeAll(s);
        muv.retainAll(getModuleCalculator().getInitialUnsatClasses());*/

        //muv = calculateStillUnsatClass (muv, modul);

        if (muv.isEmpty())
            return modul;

            if (isNew) {
                Set<OWLClass> setOfT = new LinkedHashSet<OWLClass>(unionOf(table.values()));
                if (setOfT.containsAll(muv)) {
                    for (OWLClass key : table1.keySet())
                        table1.put(key, table1.get(key) + 1);
                    for (OWLClass cls : s)
                        table1.put(cls,1);
                    table1.put(unsatClass,0);

                    List<Integer> tKeys = new LinkedList<Integer>(table.keySet());
                    Collections.sort(tKeys);
                    //Collections.reverse(tKeys);
                    Integer min = -1;
                    for (Integer key : tKeys) {
                        Set<OWLClass> classes = new LinkedHashSet<OWLClass>(table.get(key));
                        classes.retainAll(muv);
                        if (!classes.isEmpty()) {
                            min = key;
                            break;
                        }
                    }

                    if (min == -1)
                        throw new IllegalStateException("there is no min key in table for element in muv");

                    for (OWLClass key : table1.keySet())
                        table1.put(key, table1.get(key) + min);

                    updateTable(table1);

                    return Collections.emptySet();
                }

                Set<OWLClass> classes = new LinkedHashSet<OWLClass>(muv);
                classes.removeAll(unionOf(table.values()));
                OWLClass modulClass = classes.iterator().next();

                Set<OWLLogicalAxiom> submodule = getModuleCalculator().extractModule(createOntology(modul),Collections.singleton((OWLEntity)modulClass));
                if (modul.size() == submodule.size()) {
                    s.add(modulClass);
                }
                else {
                    for (OWLClass key : table1.keySet())
                        table1.put(key, table1.get(key) + 1);
                    for (OWLClass cls : s)
                        table1.put(cls,1);
                    s.clear();
                    s.add(modulClass);
                }

                muv.remove(modulClass);
                muv.removeAll(table1.keySet());
                muv.removeAll(s);
                muv.retainAll(getClassesInModuleSignature(submodule));

                if (submodule.size() < MIN_MODUL_SIZE)
                    return submodule;

                return reduceToRootModule(modulClass, isNew, submodule, table1, s, muv);
            }
            else {
                Integer minKey = Collections.min(table.keySet());
                if (!table.get(minKey).contains(unsatClass))
                    throw new IllegalStateException ("concept is not in min key set of table");
                if (!table.containsKey(minKey + 1))
                    throw new IllegalStateException ("set of min + 1 (key) not available in table");
                table.get(minKey).remove(unsatClass);
                table.get(minKey+1).add(unsatClass);

                if (table.get(minKey).isEmpty())
                    throw new IllegalStateException("there must be another class with min key");


                Set<OWLClass> temp = new HashSet<OWLClass>(muv);
                temp.retainAll(table.get(minKey));
                OWLClass modulClass = temp.iterator().next();
                Set<OWLLogicalAxiom> submodule = getModuleCalculator().extractModule(createOntology(modul),Collections.singleton((OWLEntity)modulClass));

                if (modul.size() == submodule.size()) {
                    s.add(modulClass);
                }
                else {
                    s.clear();
                    s.add(modulClass);
                }

                muv.remove(modulClass);
                muv.removeAll(table1.keySet());
                muv.removeAll(s);
                muv.retainAll(getClassesInModuleSignature(submodule));

                if (submodule.size() < MIN_MODUL_SIZE)
                    return submodule;

                return reduceToRootModule(modulClass, isNew, submodule, table1, s, muv);
            }




    }



}
