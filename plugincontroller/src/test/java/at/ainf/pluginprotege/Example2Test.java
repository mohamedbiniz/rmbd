package at.ainf.pluginprotege;

import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLAxiomNodeCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.diagnosis.tree.UniformCostSearch;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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

    enum Diag {
        D1(new Axiom[]{Axiom.AX1}),
        D2(new Axiom[]{Axiom.AX3}),
        D3(new Axiom[]{Axiom.AX4, Axiom.AX5}),
        D4(new Axiom[]{Axiom.AX4, Axiom.AX2});

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
    public static void setUp() throws UnsatisfiableFormulasException, OWLOntologyCreationException, SolverException {

        file = new File(ClassLoader.getSystemResource("ecai2010.owl").getFile());
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
        Query.X4.addToDx(new Diag[]{Diag.D2, Diag.D3, Diag.D4});
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
        Query.X5.addToD0(new Diag[]{Diag.D1});
        Query.X6.addToD0(new Diag[]{Diag.D3, Diag.D4});
        Query.X7.addToD0(new Diag[]{Diag.D1, Diag.D2, Diag.D3});

    }

    @Test
    public void testResume() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<Query, Boolean> result =
                new HashMap<Query, Boolean>();

        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);

        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());

        if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
        theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory,map));

        search.setTheory(theory);
        search.setMaxHittingSets(0);

        TreeSet<Diag> set = new TreeSet<Diag>();
        for (HittingSet<OWLLogicalAxiom> col : search.run(2)) {
            set.add(Diag.getDiagnosis(col));
        }
        TreeSet<Diag> expectedRes = new TreeSet<Diag>();
        expectedRes.add(Diag.D2);
        expectedRes.add(Diag.D4);
        assertTrue(set.equals(expectedRes));
        set = new TreeSet<Diag>();
        for (HittingSet<OWLLogicalAxiom> col : search.run(3)) {
            set.add(Diag.getDiagnosis(col));
        }
        expectedRes.add(Diag.D1);
        assertTrue(set.equals(expectedRes));
        for (HittingSet<OWLLogicalAxiom> col : search.run(4)) {
            set.add(Diag.getDiagnosis(col));
        }
        expectedRes.add(Diag.D3);
        assertTrue(set.equals(expectedRes));
    }

    @Test
    public void testAllQueryEntailed() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<Query, Boolean> result =
                new HashMap<Query, Boolean>();
        for (Query query : Query.values()) {

            UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);

            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
            if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
            theory = new OWLTheory(reasonerFactory, ontology, bax);
            search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory,map));

            search.setTheory(theory);
            search.setMaxHittingSets(0);

            search.run();

            theory.addEntailedTest(query.getAxioms());
            try {
                search.run(-1);
            }
            catch(NoConflictException e) {
            }
            Collection<Diag> res = new TreeSet<Diag>();
            for (Collection<OWLLogicalAxiom> col : search.getStorage().getValidHittingSets()) {
                res.add(Diag.getDiagnosis(col));
            }
            TreeSet<Diag> d_xPlus0;
            d_xPlus0 = new TreeSet<Diag>();
            d_xPlus0.addAll(query.getDx());
            d_xPlus0.addAll(query.getD_0());

            //assertTrue(query.getDx().equals(res));
            result.put(query, d_xPlus0.equals(res));

            theory.removeEntailedTest(query.getAxioms());
        }
        for (Query query : result.keySet())
            System.out.println(query + " " + result.get(query));

    }

    @Test
    public void testSomeQueryNotEntailed() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<Query, Boolean> result =
                new HashMap<Query, Boolean>();
        for (Query query : new Query[]{Query.X5, Query.X4, Query.X2, Query.X7}) {

            UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);

            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
            if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
            theory = new OWLTheory(reasonerFactory, ontology, bax);
            search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory,map));

            search.setTheory(theory);
            search.setMaxHittingSets(0);

            theory.addNonEntailedTest(query.getAxioms());
            search.run();
            Collection<Diag> res = new TreeSet<Diag>();
            for (Collection<OWLLogicalAxiom> col : search.getStorage().getValidHittingSets()) {
                res.add(Diag.getDiagnosis(col));
            }
            TreeSet<Diag> d_nxPlus0;
            d_nxPlus0 = new TreeSet<Diag>();
            d_nxPlus0.addAll(query.getD_nx());
            d_nxPlus0.addAll(query.getD_0());
            result.put(query, d_nxPlus0.equals(res));
            assertTrue(d_nxPlus0.equals(res));
            theory.removeNonEntailedTest(query.getAxioms());
        }
        for (Query query : result.keySet())
            System.out.println(query + " " + result.get(query));

    }

    @Test
    public void testQuery5NotEntailed() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);

        search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
        theory = new OWLTheory(reasonerFactory, ontology, bax);
        search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory,map));

        search.setTheory(theory);
        search.setMaxHittingSets(0);


        theory.addNonEntailedTest(Query.X5.getAxioms());
        search.run();
        Collection<Diag> res = new TreeSet<Diag>();
        for (Collection<OWLLogicalAxiom> col : search.getStorage().getValidHittingSets()) {
            res.add(Diag.getDiagnosis(col));
        }
        TreeSet<Diag> d_nxPlus0;
        System.out.println(res);
        d_nxPlus0 = new TreeSet<Diag>();
        d_nxPlus0.addAll(Query.X5.getD_nx());
        d_nxPlus0.addAll(Query.X5.getD_0());
        assertTrue(d_nxPlus0.equals(res));

    }

}
