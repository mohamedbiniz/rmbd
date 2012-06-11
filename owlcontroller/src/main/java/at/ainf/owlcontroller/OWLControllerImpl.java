package at.ainf.owlcontroller;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.DualTreeLogic;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlcontroller.listeners.OWLControllerListener;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.DualStorage;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.theory.storage.Storage;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import static at.ainf.owlcontroller.SearchConfiguration.TreeType.*;
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

    private SimpleStorage<OWLLogicalAxiom> createStorage() {
        switch (config.treeType) {
            case REITER:
                return new SimpleStorage<OWLLogicalAxiom>();
            case DUAL:
                return new DualStorage<OWLLogicalAxiom>();
            default:
                return null;
        }
    }

    private Searcher<OWLLogicalAxiom> createSearcher() {
        switch (config.treeType) {
            case REITER:
                return new NewQuickXplain<OWLLogicalAxiom>();
            case DUAL:
                return new DirectDiagnosis<OWLLogicalAxiom>();
            default:
                return null;
        }
    }

    private OWLTheory createTheory (OWLOntology ontology) {
        return null;
    }

    private TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> createSearch(Storage<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> storage, Searcher<OWLLogicalAxiom> searcher, OWLTheory theory) {
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = null;

        if (config.searchType == BREATHFIRST)
            search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        else if (config.searchType == UNIFORM_COST) {
            search = new UniformCostSearch<OWLLogicalAxiom>((SimpleStorage<OWLLogicalAxiom>)storage);
            ((UniformCostSearch<OWLLogicalAxiom>)search).setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        }

        search.setSearcher(searcher);

        if (config.treeType == DUAL) {
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
