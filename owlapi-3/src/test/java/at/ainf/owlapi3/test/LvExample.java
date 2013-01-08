package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.EqualCostsEstimator;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.12.12
 * Time: 17:07
 * To change this template use File | Settings | File Templates.
 */
public class LvExample {

    private static Logger logger = LoggerFactory.getLogger(LvExample.class.getName());

    @Test
    public void RunLVExample() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        String ontologyString = "ontologies/lvExample201212.owl";
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(ontologyString);
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);
        //bax.add(parser.parse("B1 EquivalentTo anerk or engOk or oecBakk"));
        //bax.add(parser.parse("B2 EquivalentTo kandFuerEngMaster and notenOk"));

        OWLTheory theory = new OWLTheory(Collections.<OWLReasonerFactory>singletonList(new Reasoner.ReasonerFactory()), ontology, bax);
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search.setCostsEstimator(new EqualCostsEstimator<OWLLogicalAxiom>(theory.getKnowledgeBase().getFaultyFormulas(),
                BigDecimal.valueOf(0.01)));
        search.setSearchable(theory);
        long time = 0;
        try {
            search.setMaxDiagnosesNumber(9);
            time = System.currentTimeMillis();
            search.start();
            time = System.currentTimeMillis() - time;
        } catch (NoConflictException e) {
            logger.info("no conflict found ");
        }

        for (FormulaSet<OWLLogicalAxiom> conflict : search.getConflicts())
            logger.info("conflict " + CalculateDiagnoses.renderAxioms(conflict));
        for (FormulaSet<OWLLogicalAxiom> diagnosis : search.getDiagnoses())
            logger.info("diagnosis " + CalculateDiagnoses.renderAxioms(diagnosis) + ", " + diagnosis.getMeasure()
            + ", " + search.getCostsEstimator().getFormulaSetCosts(diagnosis));

        String axiom = "kandFuerEngMaster EquivalentTo anerk or engOk or oecBakk";
        logger.info("entropy of " + axiom + " " + search.getCostsEstimator().getFormulaCosts(parser.parse(axiom)));

        axiom = "qui Type oecBakk";
        logger.info("entropy of " + axiom + " " + search.getCostsEstimator().getFormulaCosts(parser.parse(axiom)));
        axiom = "qui Type anerk";
        logger.info("entropy of " + axiom + " " + search.getCostsEstimator().getFormulaCosts(parser.parse(axiom)));
        axiom = "qui Type kandFuerEngMaster";
        logger.info("entropy of " + axiom + " " + search.getCostsEstimator().getFormulaCosts(parser.parse(axiom)));
        axiom = "qui Type engOk";
        logger.info("entropy of " + axiom + " " + search.getCostsEstimator().getFormulaCosts(parser.parse(axiom)));


    }

}
