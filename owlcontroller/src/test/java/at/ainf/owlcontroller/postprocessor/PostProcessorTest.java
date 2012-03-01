package at.ainf.owlcontroller.postprocessor;

import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.EntropyScoringFunction;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.postprocessor.MinScoreQSS;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlcontroller.Utils;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.theory.storage.Partition;
import at.ainf.theory.storage.SimpleStorage;
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
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void simplePostprocessorTest() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);
        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        OWLTheory th = createTheory(manager, "queryontologies/Univ.owl", false);
        search.setTheory(th);
        search.setAxiomRenderer(new MyOWLRendererParser(null));
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(th);
        es.updateKeywordProb(map);
        search.setCostsEstimator(es);

        CKK<OWLLogicalAxiom> ckk = new CKK<OWLLogicalAxiom>(th, new EntropyScoringFunction<OWLLogicalAxiom>());
        ckk.setThreshold(0.01);
        ckk.setPostprocessor(new MinScoreQSS<OWLLogicalAxiom>());

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
