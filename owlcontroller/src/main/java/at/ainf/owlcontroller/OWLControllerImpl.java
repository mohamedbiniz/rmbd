package at.ainf.owlcontroller;

import at.ainf.diagnosis.debugger.ProbabilityQueryDebugger;
import at.ainf.diagnosis.debugger.QueryDebugger;
import at.ainf.diagnosis.debugger.QueryDebuggerListener;
import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.diagnosis.quickxplain.FastDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.DualTreeLogic;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlcontroller.listeners.OWLControllerConflictSetListener;
import at.ainf.owlcontroller.listeners.OWLControllerHittingSetListener;
import at.ainf.owlcontroller.listeners.OWLControllerListener;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.DualStorage;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.theory.storage.Storage;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import static at.ainf.owlcontroller.SearchConfiguration.TreeType.*;
import static at.ainf.owlcontroller.SearchConfiguration.QSS.*;
import static at.ainf.owlcontroller.SearchConfiguration.SearchType.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.12.11
 * Time: 09:25
 * To change this template use File | Settings | File Templates.
 */
public class OWLControllerImpl implements OWLController {

    private SearchConfiguration config;

    private SearchConfiguration readConfiguration() {
        SearchConfiguration config = ConfigFileManager.readConfiguration();

        if (config == null) {
            ConfigFileManager.writeConfiguration(ConfigFileManager.getDefaultConfig());
            config = ConfigFileManager.readConfiguration();
        }

        return config;
    }

    private Storage<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> createStorage() {
        if (config.treeType == REITER)
            return new SimpleStorage<OWLLogicalAxiom>();
        else if (config.treeType == DUAL)
            return new DualStorage<OWLLogicalAxiom>();
        else
            return null;
    }

    private TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> createSearch(Storage<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> storage, OWLTheory theory) {
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = null;

        if (config.searchType == BREATHFIRST)
            search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        else if (config.searchType == UNIFORM_COST) {
            search = new UniformCostSearch<OWLLogicalAxiom>((SimpleStorage<OWLLogicalAxiom>)storage);
            ((UniformCostSearch<OWLLogicalAxiom>)search).setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        }

        if (config.treeType == REITER)
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        else if (config.treeType == DUAL) {
            search.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
            search.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());
        }

        search.setTheory(theory);

        return search;
    }

    public OWLControllerImpl(OWLReasonerFactory factory, OWLOntology ontology) {
        config = readConfiguration();



        if (config.treeType.equals(SearchConfiguration.TreeType.REITER)) {

        }


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
