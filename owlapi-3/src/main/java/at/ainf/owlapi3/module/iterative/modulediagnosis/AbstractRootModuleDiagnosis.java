package at.ainf.owlapi3.module.iterative.modulediagnosis;

import at.ainf.owlapi3.module.iterative.diagsearcher.ModuleDiagSearcher;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.*;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractRootModuleDiagnosis extends AbstractModuleDiagnosis {

    protected static final int MIN_MODUL_SIZE = 100;

    private Map<Integer,Set<OWLClass>> table = new HashMap<Integer, Set<OWLClass>>();

    public AbstractRootModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms, OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {
        super(mappings, ontoAxioms, factory, moduleDiagSearcher);
    }

    protected Map<Integer,Set<OWLClass>> getTable() {
        return table;
    }

    protected <X> Set<X> unionOf (Collection<Set<X>> sets) {
        Set<X> union = new HashSet<X>();
        for (Set<X> set : sets)
            union.addAll(set);
        return union;
    }

    protected void updateTable (Map<OWLClass, Integer> table1) {
        for (Map.Entry<OWLClass,Integer> entry : table1.entrySet()) {
            if (!getTable().containsKey(entry.getValue()))
                getTable().put(entry.getValue(), new LinkedHashSet<OWLClass>());

            getTable().get(entry.getValue()).add(entry.getKey());
        }
    }

    protected Set<OWLClass> getClassesInModuleSignature(Set<OWLLogicalAxiom> module) {
        Set<OWLClass> classesInModule = new LinkedHashSet<OWLClass>();
        for (OWLLogicalAxiom axiom : module)
            classesInModule.addAll (axiom.getClassesInSignature());
        return classesInModule;
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
                Set<OWLClass> setOfT = new LinkedHashSet<OWLClass>(unionOf(getTable().values()));
                if (setOfT.containsAll(muv)) {
                    for (OWLClass key : table1.keySet())
                        table1.put(key, table1.get(key) + 1);
                    for (OWLClass cls : s)
                        table1.put(cls,1);
                    table1.put(unsatClass,0);

                    List<Integer> tKeys = new LinkedList<Integer>(getTable().keySet());
                    Collections.sort(tKeys);
                    //Collections.reverse(tKeys);
                    Integer min = -1;
                    for (Integer key : tKeys) {
                        Set<OWLClass> classes = new LinkedHashSet<OWLClass>(getTable().get(key));
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
                classes.removeAll(unionOf(getTable().values()));
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
                Integer minKey = Collections.min(getTable().keySet());
                if (!getTable().get(minKey).contains(unsatClass))
                    throw new IllegalStateException ("concept is not in min key set of table");
                if (!getTable().containsKey(minKey + 1))
                    throw new IllegalStateException ("set of min + 1 (key) not available in table");
                getTable().get(minKey).remove(unsatClass);
                getTable().get(minKey + 1).add(unsatClass);

                if (getTable().get(minKey).isEmpty())
                    throw new IllegalStateException("there must be another class with min key");


                Set<OWLClass> temp = new HashSet<OWLClass>(muv);
                temp.retainAll(getTable().get(minKey));
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
