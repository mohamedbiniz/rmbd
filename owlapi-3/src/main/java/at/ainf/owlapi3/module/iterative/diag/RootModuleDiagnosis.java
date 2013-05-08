package at.ainf.owlapi3.module.iterative.diag;

import at.ainf.owlapi3.module.iterative.ModuleDiagSearcher;
import at.ainf.owlapi3.reasoner.HornSatReasoner;
import org.semanticweb.owlapi.model.*;
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

    private Map<Integer,Set<OWLClass>> table = new HashMap<Integer, Set<OWLClass>>();

    public RootModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms,
                                    OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {
        super(mappings,ontoAxioms,factory,moduleDiagSearcher);

    }

    private List<OWLClass> computeUnsatClasses(List<OWLClass> repaired) {
        List<OWLClass> result = new LinkedList<OWLClass>(getModuleCalculator().getInitialUnsatClasses());
        result.removeAll(repaired);
        return result;
    }

    private List<OWLClass> computeNvClasses(Map<Integer,Set<OWLClass>> table, List<OWLClass> repaired) {
        List<OWLClass> result = computeUnsatClasses(repaired);
        result.removeAll(unionOf(table.values()));
        return result;
    }

    private List<OWLClass> computeVClasses(Map<Integer,Set<OWLClass>> table, List<OWLClass> repaired) {
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
        return getModuleCalculator().extractModule(createOntology(axioms),unsatClass);
    }

    private <X> Set<X> unionOf (Collection<Set<X>> sets) {
        Set<X> union = new HashSet<X>();
        for (Set<X> set : sets)
            union.addAll(set);
        return union;
    }


    private Map<OWLClass,Integer> table1 = new HashMap<OWLClass, Integer>();

    private Set<OWLClass> s = new LinkedHashSet<OWLClass>();

    private Set<OWLLogicalAxiom> rootModul = null;


    protected void updateTable (Map<OWLClass, Integer> table1) {
        for (Map.Entry<OWLClass,Integer> entry : table1.entrySet()) {
            if (!table.containsKey(entry.getValue()))
                table.put(entry.getValue(),new LinkedHashSet<OWLClass>());

            table.get(entry.getValue()).add(entry.getKey());
        }
    }

    protected OWLClass checkModuleForUnsatClassesAndUpdateRepaired(OWLClass unsatClass, Set<OWLLogicalAxiom> modul, Set<OWLLogicalAxiom> targetDiagnosis) {
        for (OWLClass cls : getClassesInModuleSignature(modul)) {
            if (cls.equals(unsatClass))
                continue;

            Set<OWLLogicalAxiom> testModule = computeModule(cls, targetDiagnosis);
            if (!testModule.isEmpty()) {
                return cls;
            }
            else {
                repaired.add(cls);
            }
        }

        return null;

    }

    public Set<OWLLogicalAxiom> calculateTargetDiagnosis() {
        Set<OWLLogicalAxiom> targetDiagnosis = new HashSet<OWLLogicalAxiom>();

        List<OWLClass> nvClasses = computeNvClasses(table,repaired);
        List<OWLClass> vClasses = computeVClasses(table,repaired);

        boolean bothEmpty = nvClasses.isEmpty() && vClasses.isEmpty();
        while (!bothEmpty) {

            Set<OWLLogicalAxiom> module = null;
            OWLClass actualUnsatClass = null;
            boolean rootModuleFound = false;
            for (OWLClass unsatClass : nvClasses) {
                module = computeModule(unsatClass, targetDiagnosis);

                OWLClass foundUnsatClass = checkModuleForUnsatClassesAndUpdateRepaired(unsatClass, module, targetDiagnosis);

                if (foundUnsatClass != null) {

                    actualUnsatClass = unsatClass;
                    break;
                }
                else {
                    if (module.isEmpty()) {
                        repaired.add(unsatClass);
                        nvClasses.remove(unsatClass);
                    }
                    else {
                        boolean satisfiable;
                        if (getReasonerFactory().getReasonerName().equals(HornSatReasoner.NAME)) {
                            Set<OWLClass> unsat = getReasonerFactory().createNonBufferingReasoner(createOntology(module)).getUnsatisfiableClasses().getEntitiesMinusBottom();
                            satisfiable = !unsat.contains(unsatClass);
                        }
                        else {
                            satisfiable = getReasonerFactory().createNonBufferingReasoner(createOntology(module)).isSatisfiable(unsatClass);
                        }
                        if (satisfiable) {
                            repaired.add(unsatClass);
                            nvClasses.remove(unsatClass);
                        }
                        else {
                            rootModuleFound = true;
                            actualUnsatClass = unsatClass;
                            break;
                        }
                    }

                }
            }
            if (actualUnsatClass == null) {
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
                for (OWLClass unsatClass : vClasses) {
                    module = computeModule(unsatClass, targetDiagnosis);

                    OWLClass foundUnsatClass = checkModuleForUnsatClassesAndUpdateRepaired(unsatClass, module, targetDiagnosis);

                    if (foundUnsatClass != null) {

                        actualUnsatClass = unsatClass;
                        break;
                    }
                    else {
                        if (module.isEmpty()) {
                            repaired.add(unsatClass);
                            vClasses.remove(unsatClass);
                        }
                        else {
                            boolean satisfiable;
                            if (getReasonerFactory().getReasonerName().equals(HornSatReasoner.NAME)) {
                                Set<OWLClass> unsat = getReasonerFactory().createNonBufferingReasoner(createOntology(module)).getUnsatisfiableClasses().getEntitiesMinusBottom();
                                satisfiable = !unsat.contains(unsatClass);
                            }
                            else {
                                satisfiable = getReasonerFactory().createNonBufferingReasoner(createOntology(module)).isSatisfiable(unsatClass);
                            }
                            if (satisfiable) {
                                repaired.add(unsatClass);
                                vClasses.remove(unsatClass);
                            }
                            else {
                                rootModuleFound = true;
                                actualUnsatClass = unsatClass;
                                break;
                            }
                        }

                    }
                }
            }

            boolean alreadyVisited = false;

            if (!rootModuleFound) {
                try {
                    if (nvClasses.contains(actualUnsatClass)) {
                        module = reduceClassToRootModule(actualUnsatClass, true, module);
                    }
                    else if (vClasses.contains(actualUnsatClass)) {
                        module = reduceClassToRootModule(actualUnsatClass, false, module);
                    }
                    else
                        throw new IllegalStateException("both sets cannot be total empty");
                }
                catch (AlreadyVisitedException e) {
                    alreadyVisited = true;
                }
                catch(FoundRootModuleException e) {
                    module = rootModul;
                    rootModul = null;
                }
            }

            if (!alreadyVisited && !module.isEmpty()) {
                Set<OWLLogicalAxiom> axioms = new LinkedHashSet<OWLLogicalAxiom>(module);
                Set<OWLLogicalAxiom> background = new LinkedHashSet<OWLLogicalAxiom>(axioms);
                background.retainAll(getOntoAxioms());

                Set<OWLLogicalAxiom> possibleFaulty = new LinkedHashSet<OWLLogicalAxiom>(axioms);
                possibleFaulty.removeAll(background);
                if (possibleFaulty.size() == 1) {
                    targetDiagnosis.addAll(possibleFaulty);
                }
                else {
                    targetDiagnosis.addAll(getDiagSearcher().calculateDiag(axioms, background));
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

    protected Set<OWLLogicalAxiom> reduceClassToRootModule(OWLClass unsatClass, boolean isNew, Set<OWLLogicalAxiom> initialModul)
        throws AlreadyVisitedException, FoundRootModuleException {

        Set<OWLClass> muv = getClassesInModuleSignature(initialModul);
        muv.removeAll(repaired);
        muv.remove(unsatClass);
        muv.removeAll(table1.keySet());
        muv.removeAll(s);
        muv.retainAll(getModuleCalculator().getInitialUnsatClasses());
        while (!muv.isEmpty()) {
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

                    throw new AlreadyVisitedException();
                }

                Set<OWLClass> classes = new LinkedHashSet<OWLClass>(muv);
                classes.removeAll(unionOf(table.values()));
                OWLClass modulClass = classes.iterator().next();

                Set<OWLLogicalAxiom> submodule = getModuleCalculator().extractModule(createOntology(initialModul),modulClass);
                if (initialModul.size() == submodule.size()) {
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



                reduceClassToRootModule(modulClass,isNew,submodule);
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
                Set<OWLLogicalAxiom> submodule = getModuleCalculator().extractModule(createOntology(initialModul),modulClass);

                if (initialModul.size() == submodule.size()) {
                    s.add(modulClass);
                }
                else {
                    s.clear();
                    s.add(modulClass);
                }

                reduceClassToRootModule(modulClass,isNew,submodule);
            }
        }


        rootModul = initialModul;

        throw new FoundRootModuleException();

    }

    class AlreadyVisitedException extends Exception {

    }

    class FoundRootModuleException extends Exception {

    }

}
