package at.ainf.owlapi3.test;

import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.MinScoreQSS;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.Utils;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.Partition;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.02.12
 * Time: 09:24
 * To change this template use File | Settings | File Templates.
 */
public class PostProcessorTest {
    private static Logger logger = Logger.getLogger(PostProcessorTest.class.getName());
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    public OWLTheory createTheory(OWLOntologyManager manager, String path, boolean dual) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        InputStream st = ClassLoader.getSystemResourceAsStream(path);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(st);
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = null;
        if(dual)
            theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
        else
            theory = new OWLTheory(reasonerFactory, ontology, bax);
        //assert (theory.verifyRequirements());

        return theory;
    }

    private void minimizePartitionAx(Partition<OWLLogicalAxiom> query, TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, ITheory<OWLLogicalAxiom> theory) {
        if (query.partition == null) return;
        QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(query, theory);
        NewQuickXplain<OWLLogicalAxiom> q = new NewQuickXplain<OWLLogicalAxiom>();
        try {
            query.partition = q.search(mnz, query.partition, null);
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        for (AxiomSet<OWLLogicalAxiom> hs : query.dx) {
            if (!hs.getEntailments().containsAll(query.partition) && !search.getTheory().diagnosisEntails(hs, query.partition))
                throw new IllegalStateException("DX diagnosis is not entailing a query");
        }


        for (
                AxiomSet<OWLLogicalAxiom> hs
                : query.dnx)

        {
            if (search.getTheory().diagnosisConsistent(hs, query.partition))
                throw new IllegalStateException("DNX diagnosis might entail a query");
        }

        for (
                AxiomSet<OWLLogicalAxiom> hs
                : query.dz)

        {
            if (search.getTheory().diagnosisEntails(hs, query.partition) || hs.getEntailments().containsAll(query.partition))
                throw new IllegalStateException("DZ diagnosis entails a query");
            if (!search.getTheory().diagnosisConsistent(hs, query.partition))
                throw new IllegalStateException("DZ diagnosis entails a query complement");
        }
    }

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void simplePostprocessorTest() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory th = createTheory(manager, "ontologies/Univ.owl", false);
        search.setTheory(th);
        search.setAxiomRenderer(new MyOWLRendererParser(null));
        HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(th);
        es.updateKeywordProb(map);
        search.setCostsEstimator(es);

        CKK<OWLLogicalAxiom> ckk = new CKK<OWLLogicalAxiom>(th, new MinScoreQSS<OWLLogicalAxiom>());
        ckk.setThreshold(0.01);

        Set<? extends AxiomSet<OWLLogicalAxiom>> diagnoses = search.run(9);

        Partition<OWLLogicalAxiom> best=null;
        try {
            best = ckk.generatePartition(diagnoses);
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        logger.info("Partition " + Utils.renderAxioms(best.partition));

    }

}
