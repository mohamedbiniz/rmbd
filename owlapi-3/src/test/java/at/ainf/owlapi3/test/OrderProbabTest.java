package at.ainf.owlapi3.test;

import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.base.CalculateDiagnoses;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 24.06.11
 * Time: 10:28
 * To change this template use File | Settings | File Templates.
 */
public class OrderProbabTest {


    private static Logger logger = LoggerFactory.getLogger(SimpleQueryTest.class.getName());


    //private OWLDebugger debugger = new SimpleDebugger();
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    /*@BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    } */

    @Test
    public void probabTest() throws OWLException, InconsistentTheoryException, SolverException, NoConflictException {
        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(ClassLoader.getSystemResourceAsStream("ontologies/ecai2010.owl"));

        Random r = new Random();

        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<ManchesterOWLSyntax, BigDecimal> map = new CalculateDiagnoses().getProbabMap();

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);

        for (int i = 0; i < 100 / 4; i++) {
            //storage.resetStorage();
            map.put(ManchesterOWLSyntax.SOME, BigDecimal.valueOf(r.nextDouble() / 2));
            map.put(ManchesterOWLSyntax.ONLY, BigDecimal.valueOf(r.nextDouble() / 2));
            map.put(ManchesterOWLSyntax.NOT, BigDecimal.valueOf(r.nextDouble() / 2));
            map.put(ManchesterOWLSyntax.AND, BigDecimal.valueOf(r.nextDouble() / 2));
            map.put(ManchesterOWLSyntax.OR, BigDecimal.valueOf(r.nextDouble() / 2));
            map.put(ManchesterOWLSyntax.EQUIVALENT_TO, BigDecimal.valueOf(r.nextDouble() / 2));
            map.put(ManchesterOWLSyntax.SUBCLASS_OF, BigDecimal.valueOf(r.nextDouble() / 2));

            HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

            search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
            //start.setNormalize_keywords(false);
            //start.setNormalize_axioms (false);
            //start.setNormalize_diagnoses (true);
            search.setSearchable(theory);
            search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

            ((OWLAxiomKeywordCostsEstimator)search.getCostsEstimator()).setKeywordProbabilities(map, null);

            search.setMaxDiagnosesNumber(9);
            Collection<? extends FormulaSet<OWLLogicalAxiom>> res = new TreeSet<FormulaSet<OWLLogicalAxiom>>(search.start());
            TreeSet<FormulaSet<OWLLogicalAxiom>> result = new TreeSet<FormulaSet<OWLLogicalAxiom>>();
            BigDecimal measure = new BigDecimal("0.0");
            for (FormulaSet<OWLLogicalAxiom> hs : res) {
                assertTrue(measure.compareTo(hs.getMeasure()) <= 0);
                measure = ((FormulaSet<OWLLogicalAxiom>) hs).getMeasure();
                result.add((FormulaSet<OWLLogicalAxiom>) hs);
            }

            TreeSet<FormulaSet<OWLLogicalAxiom>> copyResult = new TreeSet<FormulaSet<OWLLogicalAxiom>>(result);

            map.put(ManchesterOWLSyntax.SOME, map.get(ManchesterOWLSyntax.SOME).add(new BigDecimal("0.01")));
            map.put(ManchesterOWLSyntax.ONLY, map.get(ManchesterOWLSyntax.ONLY).add(new BigDecimal("0.01")));
            map.put(ManchesterOWLSyntax.NOT, map.get(ManchesterOWLSyntax.NOT).add(new BigDecimal("0.01")));
            map.put(ManchesterOWLSyntax.AND, map.get(ManchesterOWLSyntax.AND).add(new BigDecimal("0.01")));
            map.put(ManchesterOWLSyntax.OR, map.get(ManchesterOWLSyntax.OR).add(new BigDecimal("0.01")));
            map.put(ManchesterOWLSyntax.EQUIVALENT_TO, map.get(ManchesterOWLSyntax.EQUIVALENT_TO).add(new BigDecimal("0.01")));
            map.put(ManchesterOWLSyntax.SUBCLASS_OF, map.get(ManchesterOWLSyntax.SUBCLASS_OF).add(new BigDecimal("0.01")));
            ((OWLAxiomKeywordCostsEstimator)search.getCostsEstimator()).setKeywordProbabilities(map, result);
            result = sortDiagnoses(result);
            copyResult = sortDiagnoses(copyResult);

            Iterator<FormulaSet<OWLLogicalAxiom>> iterRes = result.iterator();
            Iterator<FormulaSet<OWLLogicalAxiom>> iterCopy = copyResult.iterator();
            while (iterRes.hasNext()) {
                FormulaSet<OWLLogicalAxiom> hsResult = iterRes.next();
                FormulaSet<OWLLogicalAxiom> hsResultCopy = iterCopy.next();

                assertTrue(hsResult.equals(hsResultCopy));
                BigDecimal d = hsResult.getMeasure().subtract(hsResultCopy.getMeasure()).abs();
                assertTrue(d.compareTo(BigDecimal.valueOf(0.00001)) < 0);

            }

        }

    }

    private TreeSet<FormulaSet<OWLLogicalAxiom>> sortDiagnoses(TreeSet<FormulaSet<OWLLogicalAxiom>> formulaSets) {
        TreeSet<FormulaSet<OWLLogicalAxiom>> phs = new TreeSet<FormulaSet<OWLLogicalAxiom>>();
        for (FormulaSet<OWLLogicalAxiom> hs : formulaSets)
            phs.add(hs);
        return (phs);
    }


}
