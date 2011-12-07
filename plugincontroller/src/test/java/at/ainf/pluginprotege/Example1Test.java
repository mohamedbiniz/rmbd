package at.ainf.pluginprotege;

import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.SimpleStorage;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLAxiomNodeCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.pluginprotege.testcasesentailmentsview.axiomeditor.owlparser.MyOWLRendererParser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
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
 * Time: 19:07
 * To change this template use File | Settings | File Templates.
 */
public class Example1Test extends AbstractExample {

    enum Axiom {
        AX1, AX2, AX3, AX4;

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


    enum Diagnosis {
        D1(new Axiom[]{Axiom.AX1}),
        D2(new Axiom[]{Axiom.AX2}),
        D3(new Axiom[]{Axiom.AX3}),
        D4(new Axiom[]{Axiom.AX4});

        private TreeSet<Axiom> set = new TreeSet<Axiom>();

        private Diagnosis(Axiom[] axioms) {
            for (Axiom a : axioms) {
                set.add(a);
            }
        }

        public Set<Axiom> getAxioms() {
            return set;
        }

        public static Diagnosis getDiagnosis(Collection<OWLLogicalAxiom> axioms) {
            TreeSet<Axiom> col = new TreeSet<Axiom>();

            for (OWLLogicalAxiom a : axioms) {
                col.add(Axiom.getAxiom(a));
            }
            for (Diagnosis d : values()) {
                if (d.getAxioms().equals(col)) {
                    return d;
                }
            }

            return null;

        }

    }

    enum Query {
        X1, X2, X3;

        private TreeSet<OWLLogicalAxiom> set = new TreeSet<OWLLogicalAxiom>();

        private TreeSet<Diagnosis> d_x = new TreeSet<Diagnosis>();
        private TreeSet<Diagnosis> d_nx = new TreeSet<Diagnosis>();
        private TreeSet<Diagnosis> d_0 = new TreeSet<Diagnosis>();

        public Set<OWLLogicalAxiom> getAxioms() {
            return set;
        }

        public Collection<Diagnosis> getDx() {
            return d_x;
        }

        public TreeSet<Diagnosis> getD_nx() {
            return d_nx;
        }

        public TreeSet<Diagnosis> getD_0() {
            return d_0;
        }

        public void addAx(OWLLogicalAxiom axiom) {
            set.add(axiom);
        }

        public void addToDx(Diagnosis d[]) {
            for (Diagnosis diagnosis : d) {
                d_x.add(diagnosis);
            }
        }

        public void addToDnx(Diagnosis[] d) {
            for (Diagnosis diagnosis : d) {
                d_nx.add(diagnosis);
            }
        }

        public void addToD0(Diagnosis[] d) {
            for (Diagnosis diagnosis : d) {
                d_0.add(diagnosis);
            }
        }

    }


    @BeforeClass
    public static void setUp() throws UnsatisfiableFormulasException, OWLOntologyCreationException, SolverException {

        file = new File(ClassLoader.getSystemResource("example1.owl").getFile());
        createOntology();
        namAxioms();
        setupQueries();

    }

    public static void namAxioms() {
        MyOWLRendererParser parser = new MyOWLRendererParser(ontology);

        Axiom.AX1.setLogicalAxiom(parser.parse("A SubClassOf B"));
        Axiom.AX2.setLogicalAxiom(parser.parse("B SubClassOf C"));
        Axiom.AX3.setLogicalAxiom(parser.parse("C SubClassOf Q"));
        Axiom.AX4.setLogicalAxiom(parser.parse("Q SubClassOf R"));

        /*for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
             if (axiom instanceof OWLSubClassOfAxiom) {
                 String className = ((OWLSubClassOfAxiom) axiom).getSubClass().asOWLClass().getIRI().getFragment();
                 if (className.equals("A")) {
                     Axiom.AX1.setLogicalAxiom (axiom);
                 }
                 if (className.equals("B")) {
                     Axiom.AX2.setLogicalAxiom (axiom);
                 }
                 if (className.equals("C")) {
                     Axiom.AX3.setLogicalAxiom (axiom);
                 }
                 if (className.equals("Q")) {
                     Axiom.AX4.setLogicalAxiom (axiom);
                 }
             }

        } */
    }

    public static void setupQueries() {
        OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        IRI iri = ontology.getOntologyID().getOntologyIRI();

        OWLClass b = factory.getOWLClass(IRI.create(iri + "#B"));
        OWLClass c = factory.getOWLClass(IRI.create(iri + "#C"));
        OWLClass q = factory.getOWLClass(IRI.create(iri + "#Q"));

        OWLIndividual w = factory.getOWLNamedIndividual(IRI.create(iri + "#w"));

        Query.X1.addAx(factory.getOWLClassAssertionAxiom(b, w));
        Query.X2.addAx(factory.getOWLClassAssertionAxiom(c, w));
        Query.X3.addAx(factory.getOWLClassAssertionAxiom(q, w));

        Query.X1.addToDx(new Diagnosis[]{Diagnosis.D2, Diagnosis.D3, Diagnosis.D4});
        Query.X2.addToDx(new Diagnosis[]{Diagnosis.D3, Diagnosis.D4});
        Query.X3.addToDx(new Diagnosis[]{Diagnosis.D4});

        Query.X1.addToDnx(new Diagnosis[]{Diagnosis.D1});
        Query.X2.addToDnx(new Diagnosis[]{Diagnosis.D1, Diagnosis.D2});
        Query.X3.addToDnx(new Diagnosis[]{Diagnosis.D1, Diagnosis.D2, Diagnosis.D3});

        Query.X1.addToD0(new Diagnosis[]{});
        Query.X2.addToD0(new Diagnosis[]{});
        Query.X3.addToD0(new Diagnosis[]{});

    }

    @Test
    public void testQueryEntailed() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {
        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<Query, Boolean> result = new HashMap<Query, Boolean>();
        for (Query query : new Query[]{Query.X2}) {

            UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);

            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
            if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
            theory = new OWLTheory(reasonerFactory, ontology, bax);
            search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory,map));

            search.setTheory(theory);
            search.setMaxHittingSets(0);

            search.run();

            theory.addEntailedTest(query.getAxioms());
            search.run();
            Collection<Diagnosis> res = new TreeSet<Diagnosis>();
            for (Collection<OWLLogicalAxiom> col : search.getStorage().getValidHittingSets()) {
                res.add(Diagnosis.getDiagnosis(col));
            }
            assertTrue(query.getDx().equals(res));
            result.put(query, query.getDx().equals(res));

            theory.removeEntailedTest(query.getAxioms());
        }
        /* for (Query query : result.keySet())
            System.out.println(query + " " + result.get(query)); */

    }

    @Test
    public void testQueryNotEntailed() throws OWLOntologyCreationException, UnsatisfiableFormulasException, SolverException, NoConflictException {


        SimpleStorage<OWLLogicalAxiom> storage = new SimpleStorage<OWLLogicalAxiom>();
        HashMap<Query, Boolean> result = new HashMap<Query, Boolean>();
        for (Query query : Query.values()) {

            UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);

            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
            if (theory != null) theory.getOntology().getOWLOntologyManager().removeOntology(theory.getOntology());
            theory = new OWLTheory(reasonerFactory, ontology, bax);
            search.setNodeCostsEstimator(new OWLAxiomNodeCostsEstimator(theory,map));

            search.setTheory(theory);
            search.setMaxHittingSets(0);

            theory.addNonEntailedTest(query.getAxioms());
            search.run();
            Collection<Diagnosis> res = new TreeSet<Diagnosis>();
            for (Collection<OWLLogicalAxiom> col : search.getStorage().getValidHittingSets()) {
                res.add(Diagnosis.getDiagnosis(col));
            }
            TreeSet<Diagnosis> d_nxPlus0;
            d_nxPlus0 = new TreeSet<Diagnosis>();
            d_nxPlus0.addAll(query.getD_nx());
            d_nxPlus0.addAll(query.getD_0());
            result.put(query, d_nxPlus0.equals(res));
            assertTrue(d_nxPlus0.equals(res));
            theory.removeNonEntailedTest(query.getAxioms());
        }
        for (Query query : result.keySet())
            System.out.println(query + " " + result.get(query));

    }

}
