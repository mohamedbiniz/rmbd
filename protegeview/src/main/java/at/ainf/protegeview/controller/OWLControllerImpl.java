package at.ainf.protegeview.controller;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.protegeview.model.configuration.ConfigFileManager;
import at.ainf.protegeview.model.configuration.SearchConfiguration;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.12.11
 * Time: 09:25
 * To change this template use File | Settings | File Templates.
 */
public class OWLControllerImpl  {

    private SearchConfiguration config;

    private SearchConfiguration readConfiguration() {
        SearchConfiguration config = ConfigFileManager.readConfiguration();

        if (config == null) {
            //ConfigFileManager.writeConfiguration(ConfigFileManager.getDefaultConfig());
            config = ConfigFileManager.readConfiguration();
        }

        return config;
    }

    /*private SimpleStorage<OWLLogicalAxiom> createStorage() {
        switch (config.treeType) {
            case REITER:
                return new SimpleStorage<OWLLogicalAxiom>();
            case DUAL:
                return new SimpleStorage<OWLLogicalAxiom>();
            default:
                return null;
        }
    } */

    private Searcher<OWLLogicalAxiom> createSearcher() {
        switch (config.treeType) {
            case REITER:
                return new QuickXplain<OWLLogicalAxiom>();
            case DUAL:
                return new DirectDiagnosis<OWLLogicalAxiom>();
            default:
                return null;
        }
    }

    private OWLTheory createTheory (OWLOntology ontology) {
        return null;
    }

    private TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> createSearch(Searcher<OWLLogicalAxiom> searcher, OWLTheory theory) {
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = null;

        if (config.treeType == SearchConfiguration.TreeType.REITER ) {
            search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        }
        else {
            search = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
        }

        if (config.searchType == SearchConfiguration.SearchType.BREATHFIRST) {
            search.setSearchStrategy(new BreadthFirstSearchStrategy<OWLLogicalAxiom>());
            search.setCostsEstimator(new SimpleCostsEstimator<OWLLogicalAxiom>());
        }
        else if (config.searchType == SearchConfiguration.SearchType.UNIFORM_COST) {
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
            search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        }

        search.setSearcher(searcher);



        search.setSearchable(theory);

        return search;
    }

    public OWLControllerImpl(OWLReasonerFactory factory, OWLOntology ontology) {
        config = readConfiguration();



        if (config.treeType.equals(SearchConfiguration.TreeType.REITER)) {

        }


    }









}
