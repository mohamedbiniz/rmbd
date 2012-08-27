package at.ainf.owlapi3.base;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.base.tools.ProbabMapCreator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.08.12
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
public class CalculateDiagnoses {

    private String file;

    public CalculateDiagnoses() {}

    public CalculateDiagnoses(String file) {
        this.file = file;
    }

    protected boolean init = false;

    protected TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search;

    public static <X> Set<X> getIntersection (Set<X> axioms1, Set<X> axioms2) {
        Set<X> intersection = new LinkedHashSet<X>();
        intersection.addAll(axioms1);
        intersection.retainAll(axioms2);

        return intersection;
    }

    public TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch(OWLTheory theory, boolean dual) {
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search;
        if (dual) {
            search = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        } else {
            search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        }
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setTheory(theory);

        return search;
    }

    protected void init() {

        OWLOntology ontology = getOntology();

        ontology = getExtractedOntology(ontology);

        OWLTheory theory = getTheory(ontology);

        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getSearch(theory, false);

        HashMap<ManchesterOWLSyntax, BigDecimal> map = ProbabMapCreator.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
        es.updateKeywordProb(map);
        search.setCostsEstimator(es);
        init=true;
        this.search = search;
    }

    protected OWLOntology getExtractedOntology(OWLOntology ontology) {
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(getOntology());
        return ontology;
    }

    protected OWLTheory getTheory(OWLOntology ontology) {
        return getExtendTheory(ontology, false);
    }

    protected OWLOntology getOntology() {
        return getOntologySimple(file);
    }

    public TreeSet<AxiomSet<OWLLogicalAxiom>> getDiagnoses(int number) {
        if(!init) init();

        try {
            search.run(number);
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());

    }






    protected static OWLTheory createTheory(OWLOntology ontology, boolean dual, Set<OWLLogicalAxiom> bax) {
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

    public static OWLTheory getSimpleTheory(OWLOntology ontology, boolean dual) {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }



        return createTheory(ontology,dual,bax);
    }

    protected static void configureEntailments(OWLTheory theory) {
        theory.activateReduceToUns();
        theory.setIncludeTrivialEntailments(false);
        theory.setIncludeSubClassOfAxioms(false);
        theory.setIncludeClassAssertionAxioms(false);
        theory.setIncludeEquivalentClassAxioms(false);
        theory.setIncludeDisjointClassAxioms(false);
        theory.setIncludePropertyAssertAxioms(false);
        theory.setIncludeReferencingThingAxioms(false);
        theory.setIncludeOntologyAxioms(true);
    }

    public static OWLTheory getExtendTheory(OWLOntology ontology, boolean dual) {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
            bax.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }

        OWLTheory theory = createTheory(ontology,dual,bax);

        configureEntailments(theory);
        return theory;
    }




    public OWLOntology getOntologySimple (String path, String name) {
        return getOntologySimple(path + "/" + name);
    }

    public static OWLOntology getOntologySimple (String name) {
        return getOntologyBase(new File(ClassLoader.getSystemResource(name).getPath()));
    }

    public OWLOntology getOntologySimple (File filename) {
        return getOntologyBase(filename);
    }

    public static OWLOntology getOntologyBase(File file) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }

}
