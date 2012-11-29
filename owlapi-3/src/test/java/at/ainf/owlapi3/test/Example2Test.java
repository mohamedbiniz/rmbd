package at.ainf.owlapi3.test;

import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.tree.BinaryTreeSearch;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.BreadthFirstSearchStrategy;
import at.ainf.diagnosis.tree.searchstrategy.SearchStrategy;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.FormulaSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.04.11
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public class Example2Test extends AbstractExample {

    enum Axiom {
        AX1, AX2, AX3, AX4, AX5;

        private static ManchesterOWLSyntaxOWLObjectRendererImpl renderer =
                new ManchesterOWLSyntaxOWLObjectRendererImpl();

        private OWLLogicalAxiom axiom;

        public void setLogicalAxiom(OWLLogicalAxiom axiom) {
            this.axiom = axiom;
        }

        public static Axiom getAxiom(OWLLogicalAxiom a) {
            for (Axiom ax : values()) {
                if (renderer.render(ax.getLogicalAxiom()).equals(renderer.render(a))) {
                    return ax;
                }
            }

            return null;
        }

        public OWLLogicalAxiom getLogicalAxiom() {
            return axiom;
        }

        public String toString() {
            return super.toString() + ": " + renderer.render(axiom);
        }
    }

    private static Logger logger = LoggerFactory.getLogger(Example2Test.class.getName());

    enum Diag {
        D1(new Axiom[]{Axiom.AX1}),
        D2(new Axiom[]{Axiom.AX3}),
        D3(new Axiom[]{Axiom.AX4, Axiom.AX5}),
        D4(new Axiom[]{Axiom.AX4, Axiom.AX2}),
        D5(new Axiom[]{Axiom.AX4, Axiom.AX3}),
        // these diagnoses are minimal because of test cases
        // making D2 invalid diagnosis
        D6(new Axiom[]{Axiom.AX3, Axiom.AX2}),
        D7(new Axiom[]{Axiom.AX3, Axiom.AX5}),
        D8(new Axiom[]{Axiom.AX1, Axiom.AX4});

        private TreeSet<Axiom> set = new TreeSet<Axiom>();

        private Diag(Axiom[] axioms) {
            for (Axiom a : axioms) {
                set.add(a);
            }
        }

        public Set<Axiom> getAxioms() {
            return set;
        }

        public static Diag getDiagnosis(Collection<OWLLogicalAxiom> axioms) {
            TreeSet<Axiom> col = new TreeSet<Axiom>();

            for (OWLLogicalAxiom a : axioms) {
                col.add(Axiom.getAxiom(a));
            }
            for (Diag d : values()) {
                if (d.getAxioms().equals(col)) {
                    return d;
                }
            }

            return null;

        }

        /* public String toString() {
            String result = "";

            TreeSet<String> r = new TreeSet<String>();
            for (Axiom a : set) {
                r.add (a.name());
            }
            result = r.toString();

            return result;
        }  */

    }

    enum Query {
        X1, X2, X3, X4, X5, X6, X7;

        private TreeSet<OWLLogicalAxiom> set = new TreeSet<OWLLogicalAxiom>();

        private TreeSet<Diag> d_x = new TreeSet<Diag>();
        private TreeSet<Diag> d_nx = new TreeSet<Diag>();
        private TreeSet<Diag> d_0 = new TreeSet<Diag>();

        public Set<OWLLogicalAxiom> getAxioms() {
            return set;
        }

        public Collection<Diag> getDx() {
            return d_x;
        }

        public TreeSet<Diag> getD_nx() {
            return d_nx;
        }

        public TreeSet<Diag> getD_0() {
            return d_0;
        }

        public void addAx(OWLLogicalAxiom axiom) {
            set.add(axiom);
        }

        public void addToDx(Diag d[]) {
            for (Diag diagnosis : d) {
                d_x.add(diagnosis);
            }
        }

        public void addToDnx(Diag[] d) {
            for (Diag diagnosis : d) {
                d_nx.add(diagnosis);
            }
        }

        public void addToD0(Diag[] d) {
            for (Diag diagnosis : d) {
                d_0.add(diagnosis);
            }
        }

    }

    @BeforeClass
    public static void setUp() throws InconsistentTheoryException, OWLOntologyCreationException, SolverException {

        file = new File(ClassLoader.getSystemResource("ontologies/ecai2010.owl").getFile());
        createOntology();
        namAxioms();
        setupQueries();

    }

    public static void namAxioms() {
        Axiom.AX1.setLogicalAxiom(parser.parse("A1 SubClassOf A2 and M1 and M2"));
        Axiom.AX2.setLogicalAxiom(parser.parse("A2 SubClassOf (not (s some M3)) and (s some M2)"));
        Axiom.AX3.setLogicalAxiom(parser.parse("M1 SubClassOf B and (not (A))"));
        Axiom.AX4.setLogicalAxiom(parser.parse("M2 SubClassOf D and (s only A)"));
        Axiom.AX5.setLogicalAxiom(parser.parse("M3 EquivalentTo B or C"));
    }

    public static void setupQueries() {

        Query.X1.addAx(parser.parse("B SubClassOf M3"));
        Query.X2.addAx(parser.parse("w type B"));
        Query.X3.addAx(parser.parse("M1 SubClassOf B"));
        Query.X4.addAx(parser.parse("w type M1"));
        Query.X4.addAx(parser.parse("u type M2"));
        Query.X5.addAx(parser.parse("w type A"));
        Query.X6.addAx(parser.parse("M2 SubClassOf D"));
        Query.X7.addAx(parser.parse("u type M3"));

        Query.X1.addToDx(new Diag[]{Diag.D1, Diag.D2, Diag.D4});
        Query.X2.addToDx(new Diag[]{Diag.D3, Diag.D4});
        Query.X3.addToDx(new Diag[]{Diag.D1, Diag.D3, Diag.D4});
        Query.X4.addToDx(new Diag[]{Diag.D2, Diag.D3, Diag.D4, Diag.D8 });
        Query.X5.addToDx(new Diag[]{Diag.D2});
        Query.X6.addToDx(new Diag[]{Diag.D1, Diag.D2});
        Query.X7.addToDx(new Diag[]{Diag.D4});

        Query.X1.addToDnx(new Diag[]{Diag.D3});
        Query.X2.addToDnx(new Diag[]{Diag.D2});
        Query.X3.addToDnx(new Diag[]{Diag.D2});
        Query.X4.addToDnx(new Diag[]{Diag.D1});
        Query.X5.addToDnx(new Diag[]{Diag.D3, Diag.D4});
        Query.X6.addToDnx(new Diag[]{});
        Query.X7.addToDnx(new Diag[]{});

        Query.X1.addToD0(new Diag[]{});
        Query.X2.addToD0(new Diag[]{Diag.D1});
        Query.X3.addToD0(new Diag[]{});
        Query.X4.addToD0(new Diag[]{});
        Query.X5.addToD0(new Diag[]{Diag.D1,Diag.D5});
        Query.X6.addToD0(new Diag[]{Diag.D3, Diag.D4});
        Query.X7.addToD0(new Diag[]{Diag.D1, Diag.D2, Diag.D3});

    }

    @Test
    public void testResumeHS() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        resumeDiagnosis(search, new UniformCostSearchStrategy<OWLLogicalAxiom>());
    }

    @Test
    public void testResumeBHS() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        resumeDiagnosis(search, new UniformCostSearchStrategy<OWLLogicalAxiom>());
    }

    private void resumeDiagnosis(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, SearchStrategy<OWLLogicalAxiom> owlLogicalAxiomUniformCostSearchStrategy) throws InconsistentTheoryException, SolverException, NoConflictException {
        HashMap<Query, Boolean> result = new HashMap<Query, Boolean>();

        search.setSearchStrategy(owlLogicalAxiomUniformCostSearchStrategy);

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());

        if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
        theory = new OWLTheory(reasonerFactory, ontology, bax);
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
        es.updateKeywordProb(map);
        search.setCostsEstimator(es);

        search.setSearchable(theory);
        search.setMaxDiagnosesNumber(2);

        TreeSet<Diag> set = new TreeSet<Diag>();
        for (FormulaSet<OWLLogicalAxiom> col : search.start()) {
            set.add(Diag.getDiagnosis(col));
        }
        TreeSet<Diag> expectedRes = new TreeSet<Diag>();

       expectedRes.add(Diag.D2);
        expectedRes.add(Diag.D4);
      /*  expectedRes.add(Diag.D1);
        expectedRes.add(Diag.D3);
        expectedRes.add(Diag.D5);
        expectedRes.add(Diag.D6);
        expectedRes.add(Diag.D7);
        expectedRes.add(Diag.D8); */
        //EDITED

        /*int count=0;
        for(Diag d : expectedRes){
                 if(set.contains(d))
                     count++;
        }  */

        assertTrue(set.equals(expectedRes));

        set = new TreeSet<Diag>();
        search.setMaxDiagnosesNumber(3);
        for (FormulaSet<OWLLogicalAxiom> col : search.resume()) {
            set.add(Diag.getDiagnosis(col));
        }
        expectedRes.add(Diag.D1);

        /* count=0;
        for(Diag d : expectedRes){
            if(set.contains(d))
                count++;
        }    */


        assertTrue(set.equals(expectedRes));
        search.setMaxDiagnosesNumber(4);
        for (FormulaSet<OWLLogicalAxiom> col : search.resume()) {
            set.add(Diag.getDiagnosis(col));
        }
        expectedRes.add(Diag.D3);

        /*count=0;
        for(Diag d : expectedRes){
            if(set.contains(d))
                count++;
        }  */

        assertTrue(set.equals(expectedRes));
    }

    @Test
    public void testAllQueryEntailed() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<Query, Boolean> result =
                new HashMap<Query, Boolean>();
        for (Query query : Query.values()) {

            HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

            search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
            if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
            theory = new OWLTheory(reasonerFactory, ontology, bax);
            search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

            search.setSearchable(theory);
            //search.setMaxDiagnosesNumber(0);

            search.start();

            search.setMaxDiagnosesNumber(-1);
            theory.getKnowledgeBase().addEntailedTest(query.getAxioms());
            try {
                search.start();
            }
            catch(NoConflictException e) {
            }
            Collection<Diag> res = new TreeSet<Diag>();
            for (Collection<OWLLogicalAxiom> col : search.getDiagnoses()) {
                res.add(Diag.getDiagnosis(col));
            }
            TreeSet<Diag> d_xPlus0;
            d_xPlus0 = new TreeSet<Diag>();
            d_xPlus0.addAll(query.getDx());
            d_xPlus0.addAll(query.getD_0());

            //assertTrue(query.getDx().equals(res));
            result.put(query, d_xPlus0.equals(res));

            theory.getKnowledgeBase().removeEntailedTest(query.getAxioms());
        }
        for (Query query : result.keySet())
            logger.info(query + " " + result.get(query));

    }

    @Test
    public void testSomeQueryNotEntailed() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<Query, Boolean> result =
                new HashMap<Query, Boolean>();
        for (Query query : new Query[]{Query.X5, Query.X4, Query.X2, Query.X7}) {

            HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

            search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
            if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
            theory = new OWLTheory(reasonerFactory, ontology, bax);
            search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

            search.setSearchable(theory);
            search.setMaxDiagnosesNumber(-1);

            theory.getKnowledgeBase().addNonEntailedTest(query.getAxioms());
            search.start();
            Collection<Diag> res = new TreeSet<Diag>();
            for (Collection<OWLLogicalAxiom> col : search.getDiagnoses()) {
                res.add(Diag.getDiagnosis(col));
            }
            TreeSet<Diag> d_nxPlus0;
            d_nxPlus0 = new TreeSet<Diag>();
            d_nxPlus0.addAll(query.getD_nx());
            d_nxPlus0.addAll(query.getD_0());
            result.put(query, d_nxPlus0.equals(res));
            assertTrue(d_nxPlus0.equals(res));
            theory.getKnowledgeBase().removeNonEntailedTest(query.getAxioms());
        }
        for (Query query : result.keySet())
            logger.info(query + " " + result.get(query));

    }

    @Test
    public void testQuery4Entailed() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
        theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        search.setSearchable(theory);
        search.setMaxDiagnosesNumber(-1);


        theory.getKnowledgeBase().addEntailedTest(Query.X4.getAxioms());
        search.start();
        Collection<Diag> res = new TreeSet<Diag>();
        for (Collection<OWLLogicalAxiom> col : search.getDiagnoses()) {
            res.add(Diag.getDiagnosis(col));
        }
        TreeSet<Diag> dxPlus0;
        logger.info(res.toString());
        dxPlus0 = new TreeSet<Diag>();
        dxPlus0.addAll(Query.X4.getDx());
        dxPlus0.addAll(Query.X4.getD_0());
        assertTrue(dxPlus0.equals(res));

    }

    @Test
    public void testQuery5NotEntailed() throws OWLOntologyCreationException, InconsistentTheoryException, SolverException, NoConflictException {
        //SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
        theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        search.setSearchable(theory);
        search.setMaxDiagnosesNumber(-1);


        theory.getKnowledgeBase().addNonEntailedTest(Query.X5.getAxioms());
        search.start();
        Collection<Diag> res = new TreeSet<Diag>();
        for (Collection<OWLLogicalAxiom> col : search.getDiagnoses()) {
            res.add(Diag.getDiagnosis(col));
        }
        TreeSet<Diag> d_nxPlus0;
        logger.info(res.toString());
        d_nxPlus0 = new TreeSet<Diag>();
        d_nxPlus0.addAll(Query.X5.getD_nx());
        d_nxPlus0.addAll(Query.X5.getD_0());
        assertTrue(d_nxPlus0.equals(res));

    }

}
