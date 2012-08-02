package at.ainf.owlapi3.utils.creation;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
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
public class OntologyUtils extends CommonUtils {
    public static double avg2(List<Double> nqueries) {
        double res = 0;
        for (Double qs : nqueries) {
            res += qs;
        }
        return res / nqueries.size();
    }

    public static TreeSet<AxiomSet<OWLLogicalAxiom>> getAllD(String o) {
        OWLOntology ontology = createOwlOntology("ontologies", o + ".owl");
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        OWLTheory theory = createOWLTheory(ontology, false);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = getProbabMap();
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
