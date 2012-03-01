package at.ainf.owlcontroller;

import at.ainf.diagnosis.debugger.ProbabilityQueryDebugger;
import at.ainf.diagnosis.debugger.QueryDebugger;
import at.ainf.diagnosis.debugger.QueryDebuggerListener;
import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.listeners.OWLControllerConflictSetListener;
import at.ainf.owlcontroller.listeners.OWLControllerHittingSetListener;
import at.ainf.owlcontroller.listeners.OWLControllerListener;
import at.ainf.theory.storage.AxiomSet;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.12.11
 * Time: 09:25
 * To change this template use File | Settings | File Templates.
 */
public class OWLControllerImpl implements OWLController {

    private Map<OWLOntology,DefaultPicoContainer> containers;

    private OWLOntology activeOntology;

    public OWLControllerImpl  () {
        containers = new HashMap<OWLOntology, DefaultPicoContainer>();

    }

    public void updateActiveOntology(OWLOntology ontology, OWLReasonerFactory factory) {

        if (containers.get(ontology) != null) {
            activeOntology = ontology;
        }
        else {
            DefaultPicoContainer pico = new DefaultPicoContainer(new Caching());
            activeOntology = ontology;

            pico.addComponent(ProbabilityQueryDebugger.class);
            pico.addComponent(OWLTheory.class);
            pico.addComponent(OWLAxiomKeywordCostsEstimator.class);
            pico.addComponent(new TreeSet<OWLLogicalAxiom>());
            pico.addComponent(factory);
            pico.addComponent(ontology);
            containers.put(ontology, pico);
        }

    }

    public void activateSimpleDebugger() {
        containers.get(activeOntology).removeComponent(ProbabilityQueryDebugger.class);
        containers.get(activeOntology).addComponent(SimpleQueryDebugger.class);
    }

    public void activateProbDebugger() {
        containers.get(activeOntology).removeComponent(SimpleQueryDebugger.class);
        containers.get(activeOntology).addComponent(ProbabilityQueryDebugger.class);
    }

    protected OWLTheory getActualTheory() {
        if (activeOntology != null)
            return (OWLTheory) getActualQueryDebugger().getTheory();
        else
            throw new NullPointerException("There is no active Ontology");
    }

    protected QueryDebugger<OWLLogicalAxiom> getActualQueryDebugger() {
        if (activeOntology != null)
            return (QueryDebugger<OWLLogicalAxiom>) containers.get(activeOntology).getComponent(QueryDebugger.class);
        else
            throw new NullPointerException("There is no active Ontology");
    }

    public void doCalcHS() {
        /*Thread t = new Thread() {
            public void run() {*/
                QueryDebuggerListener<OWLLogicalAxiom> l = new QueryDebuggerListener<OWLLogicalAxiom>() {
                    public void conflictSetAdded(Set<? extends AxiomSet<OWLLogicalAxiom>> conflicts) {
                        for (OWLControllerListener listener : listeners.get(OWLControllerConflictSetListener.class))
                            ((OWLControllerConflictSetListener)listener).updateConflictSets(conflicts);
                    }

                    public void hittingSetAdded(Set<? extends AxiomSet<OWLLogicalAxiom>> hittingSets) {
                        for (OWLControllerListener listener : listeners.get(OWLControllerHittingSetListener.class))
                            ((OWLControllerHittingSetListener)listener).updateValidHittingSets(hittingSets);
                    }
                };
                getActualQueryDebugger().addQueryDebuggerListener(l);
                getActualQueryDebugger().resume();
                getActualQueryDebugger().removeQueryDebuggerListener(l);
                /*for (OWLControllerListener listener : listeners.get(OWLControllerConflictSetListener.class))
                     ((OWLControllerConflictSetListener)listener).updateConflictSets(getActualQueryDebugger().getConflictSets());
                for (OWLControllerListener listener : listeners.get(OWLControllerHittingSetListener.class))
                    ((OWLControllerHittingSetListener)listener).updateValidHittingSets(getActualQueryDebugger().getValidHittingSets());*/

            /*}
        };
        t.start();*/
    }

    Map<Class,List<OWLControllerListener>> listeners = new HashMap<Class,List<OWLControllerListener>>();

    public void addControllerListener(OWLControllerListener listener, Class listenerClass) {
        if (listeners.get(listenerClass) != null)
            listeners.get(listenerClass).add(listener);
        else {
            List<OWLControllerListener> list = new LinkedList<OWLControllerListener>();
            list.add(listener);
            listeners.put(listenerClass,list);
        }
    }

    public void removeControllerListener(OWLControllerListener listener, Class listenerClass) {
        listeners.get(listenerClass).remove(listener);
    }

}
