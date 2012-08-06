package at.ainf.owlapi3.utils.creation;

import at.ainf.diagnosis.Searcher;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 02.08.12
 * Time: 12:45
 * To change this template use File | Settings | File Templates.
 */
public class CreationUtils {
    public static OWLTheory loadTheory(OWLOntologyManager manager, String path) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
        InputStream st = ClassLoader.getSystemResourceAsStream(path);
        return createTheory(manager.loadOntologyFromOntologyDocument(st));
    }

    public static OWLTheory createTheory(OWLOntology ontology) throws SolverException, InconsistentTheoryException {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        assert (theory.verifyRequirements());

        return theory;
    }

    public static OWLOntology createOwlOntology(String path, String name) {
        String directory = ClassLoader.getSystemResource(path).getPath();
        return createOwlOntology(new File(directory + "/" + name));
    }

    public static OWLOntology createOwlOntology(File file) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }

    public static Set<OWLLogicalAxiom> createBackgroundAxioms(OWLOntology ontology) {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }
        return bax;
    }

    public static Set<OWLLogicalAxiom> createExtendedBackgroundAxioms(OWLOntology ontology) {
        Set<OWLLogicalAxiom> bax = createBackgroundAxioms(ontology);
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }
        return bax;
    }

    public static OWLTheory createTheory(OWLOntology ontology, boolean dual, Set<OWLLogicalAxiom> bax) {
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = null;
        try {
            if (dual)
                theory = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
            else
                theory = new OWLTheory(reasonerFactory, ontology, bax);
        }
        catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return theory;
    }

    public static OWLTheory createTheory(OWLOntology ontology, boolean dual) {
        Set<OWLLogicalAxiom> bax = createBackgroundAxioms(ontology);
        return createTheory(ontology,dual,bax);

    }

    public static OWLTheory createOWLTheory2(OWLOntology ontology, boolean dual) {
        Set<OWLLogicalAxiom> bax = createExtendedBackgroundAxioms(ontology);
        OWLTheory theory = createTheory(ontology, dual, bax);

        theory.activateReduceToUns();
        theory.setIncludeTrivialEntailments(false);
        theory.setIncludeSubClassOfAxioms(false);
        theory.setIncludeClassAssertionAxioms(false);
        theory.setIncludeEquivalentClassAxioms(false);
        theory.setIncludeDisjointClassAxioms(false);
        theory.setIncludePropertyAssertAxioms(false);
        theory.setIncludeReferencingThingAxioms(false);
        theory.setIncludeOntologyAxioms(true);

        return theory;
    }

    public static TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> createUniformCostSearch2(OWLTheory th, boolean dual) {

        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search;
        Searcher<OWLLogicalAxiom> searcher;
        if (dual) {
            search = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            searcher = new DirectDiagnosis<OWLLogicalAxiom>();
        }
        else {
            search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            searcher = new NewQuickXplain<OWLLogicalAxiom>();
        }

        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearcher(searcher);
        search.setTheory(th);

        return search;
    }

    public static OWLTheory createOWLTheory(OWLOntology ontology, boolean dual) {
        OWLTheory result = null;

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
            bax.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }



        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        try {
            //ProbabilityTableModel mo = new ProbabilityTableModel();
            //HashMap<ManchesterOWLSyntax, Double> map = mo.getProbMap();

            if (dual)
                result = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
            else
                result = new OWLTheory(reasonerFactory, ontology, bax);
            result.activateReduceToUns();

            result.setIncludeTrivialEntailments(false);
            // QueryDebuggerPreference.getInstance().setTestIncoherencyToInconsistency(true);

            result.setIncludeSubClassOfAxioms(false);
            result.setIncludeClassAssertionAxioms(false);
            result.setIncludeEquivalentClassAxioms(false);
            result.setIncludeDisjointClassAxioms(false);
            result.setIncludePropertyAssertAxioms(false);
            result.setIncludeReferencingThingAxioms(false);
            result.setIncludeOntologyAxioms(true);
            //  result.setIncludeTrivialEntailments(true);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    public static TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> createUniformCostSearch(OWLTheory th, boolean dual) {

        /*SimpleStorage<OWLLogicalAxiom> storage;
        if (dual)
            storage = new SimpleStorage<OWLLogicalAxiom>();
        else
            storage = new SimpleStorage<OWLLogicalAxiom>();*/
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search;
        if (dual) {
            search = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        } else {
            search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        }
        search.setTheory(th);

        return search;
    }

    public static OWLTheory createTheoryOAEI(OWLOntology ontology, boolean dual, boolean reduceIncoherency) {
        OWLTheory result = null;

        //ontology = new OWLIncoherencyExtractor(
        //        new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();


        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
            bax.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }

        /*String iri = "http://ainf.at/testiri#";

        for (OWLClass ind : ontology.getClassesInSignature()) {
            OWLDataFactory fac = OWLManager.getOWLDataFactory();
            OWLIndividual test_individual = fac.getOWLNamedIndividual(IRI.create(iri + "{"+ind.getIRI().getFragment()+"}"));

            bax.add(fac.getOWLClassAssertionAxiom (ind,test_individual));
        }*/

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        try {
            //ProbabilityTableModel mo = new ProbabilityTableModel();
            //HashMap<ManchesterOWLSyntax, Double> map = mo.getProbMap();

            if (dual)
                result = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
            else
                result = new OWLTheory(reasonerFactory, ontology, bax);
            if (reduceIncoherency)
                result.activateReduceToUns();

            result.setIncludeTrivialEntailments(false);
            // QueryDebuggerPreference.getInstance().setTestIncoherencyToInconsistency(true);

            result.setIncludeSubClassOfAxioms(false);
            result.setIncludeClassAssertionAxioms(false);
            result.setIncludeEquivalentClassAxioms(false);
            result.setIncludeDisjointClassAxioms(false);
            result.setIncludePropertyAssertAxioms(false);
            result.setIncludeReferencingThingAxioms(false);
            result.setIncludeOntologyAxioms(true);
            //  result.setIncludeTrivialEntailments(true);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    public static TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch(OWLOntology ontology, boolean dual) throws SolverException, InconsistentTheoryException {

        OWLOntology extracted = new OWLIncoherencyExtractor(
                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        OWLTheory theory = createTheoryOAEI(extracted, dual, true);
        TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);

        search.setCostsEstimator(es);
        search.reset();

        return search;
    }
}
