package at.ainf.owlapi3.utils.creation;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.utils.ProbabMapCreator;
import at.ainf.owlapi3.utils.creation.ontology.SimpleOntologyCreator;
import at.ainf.owlapi3.utils.creation.search.UniformCostSearchCreator;
import at.ainf.owlapi3.utils.creation.theory.BackgroundExtendedTheoryCreator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 02.08.12
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public class OntologyUtils {
    public static double avg2(List<Double> nqueries) {
        double res = 0;
        for (Double qs : nqueries) {
            res += qs;
        }
        return res / nqueries.size();
    }

    public static TreeSet<AxiomSet<OWLLogicalAxiom>> getAllD(String o) {
        OWLOntology ontology = new SimpleOntologyCreator("ontologies", o + ".owl").getOntology();
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        OWLTheory theory = new BackgroundExtendedTheoryCreator(ontology, false).getTheory();
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new UniformCostSearchCreator(theory, false).getSearch();
        HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
        es.updateKeywordProb(map);
        search.setCostsEstimator(es);
        try {
            search.run();
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
    }
}
