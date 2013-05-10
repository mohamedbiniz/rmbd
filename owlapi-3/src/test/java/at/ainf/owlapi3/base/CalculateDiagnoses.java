package at.ainf.owlapi3.base;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.08.12
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
public class CalculateDiagnoses {

    private Logger logger = LoggerFactory.getLogger(CalculateDiagnoses.class.getName());

    private ManchesterOWLSyntax[] keywords = {ManchesterOWLSyntax.SOME,
                ManchesterOWLSyntax.ONLY,
                ManchesterOWLSyntax.MIN,
                ManchesterOWLSyntax.MAX,
                ManchesterOWLSyntax.EXACTLY,
                ManchesterOWLSyntax.AND,
                ManchesterOWLSyntax.OR,
                ManchesterOWLSyntax.NOT,
                ManchesterOWLSyntax.VALUE,
                ManchesterOWLSyntax.INVERSE,
                ManchesterOWLSyntax.SUBCLASS_OF,
                ManchesterOWLSyntax.EQUIVALENT_TO,
                ManchesterOWLSyntax.DISJOINT_CLASSES,
                ManchesterOWLSyntax.DISJOINT_WITH,
                ManchesterOWLSyntax.FUNCTIONAL,
                ManchesterOWLSyntax.INVERSE_OF,
                ManchesterOWLSyntax.SUB_PROPERTY_OF,
                ManchesterOWLSyntax.SAME_AS,
                ManchesterOWLSyntax.DIFFERENT_FROM,
                ManchesterOWLSyntax.RANGE,
                ManchesterOWLSyntax.DOMAIN,
                ManchesterOWLSyntax.TYPE,
                ManchesterOWLSyntax.TRANSITIVE,
                ManchesterOWLSyntax.SYMMETRIC
        };

    public HashMap<ManchesterOWLSyntax, BigDecimal> getProbabMap() {
        HashMap<ManchesterOWLSyntax, BigDecimal> map = new HashMap<ManchesterOWLSyntax, BigDecimal>();

        for (ManchesterOWLSyntax keyword : keywords) {
            map.put(keyword, BigDecimal.valueOf(0.01));
        }

        map.put(ManchesterOWLSyntax.SOME, BigDecimal.valueOf(0.05));
        map.put(ManchesterOWLSyntax.ONLY, BigDecimal.valueOf(0.05));
        map.put(ManchesterOWLSyntax.AND, BigDecimal.valueOf(0.001));
        map.put(ManchesterOWLSyntax.OR, BigDecimal.valueOf(0.001));
        map.put(ManchesterOWLSyntax.NOT, BigDecimal.valueOf(0.01));

        return map;
    }

    public String getStringTime(long millis) {
        long timeInHours = TimeUnit.MILLISECONDS.toHours(millis);
        long timeInMinutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long timeInSec = TimeUnit.MILLISECONDS.toSeconds(millis);
        long timeInMillisec = TimeUnit.MILLISECONDS.toMillis(millis);

        long hours = timeInHours;
        long minutes = timeInMinutes - TimeUnit.HOURS.toMinutes(timeInHours);
        long seconds = timeInSec - TimeUnit.MINUTES.toSeconds(timeInMinutes);
        long milliseconds = timeInMillisec - TimeUnit.SECONDS.toMillis(timeInSec);

        return String.format("%d , (%d h %d m %d s %d ms)", millis, hours, minutes, seconds, milliseconds);
    }

    public static String renderAxioms(Collection<OWLLogicalAxiom> axioms) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        String result = "";

        for (OWLLogicalAxiom axiom : axioms) {
            result += renderer.render(axiom) + ", ";
        }
        if (result.length() > 2)
            result = (String) result.subSequence(0,result.length()-2);

        return result;
    }

    public TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> getUniformCostSearch(OWLTheory theory, boolean dual) {
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search;
        if (dual) {
            search = new InvHsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        } else {
            search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        }
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setSearchable(theory);

        return search;
    }

    public TreeSet<FormulaSet<OWLLogicalAxiom>> getDiagnoses(String file, int num) {

        OWLOntology ontology = getOntologySimple(file);
        ontology = extractModules(ontology);

        OWLTheory theory = getExtendTheory(ontology, false);
        TreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, false);
        setAxiomKeywordCostsEstimator(search);

        runSearch(search, num);
        return new TreeSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());

    }

    protected void runSearch(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, int num) {
        try {
            search.setMaxDiagnosesNumber(num);
            search.start();
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private OWLOntology extractModules(OWLOntology ontology) {
        return new OWLIncoherencyExtractor(getReasonerFactory()).getIncoherentPartAsOntology(ontology);

    }

    private void setAxiomKeywordCostsEstimator(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search) {
        HashMap<ManchesterOWLSyntax, BigDecimal> map = getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(search.getSearchable());
        es.updateKeywordProb(map);
        search.setCostsEstimator(es);
    }

    protected OWLTheory createTheory(OWLOntology ontology, boolean dual, Set<OWLLogicalAxiom> bax) {
        OWLReasonerFactory reasonerFactory = getReasonerFactory();
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

    private OWLReasonerFactory getReasonerFactory() {
        return new Reasoner.ReasonerFactory();
    }

    public OWLTheory getSimpleTheory(OWLOntology ontology, boolean dual) {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        return createTheory(ontology,dual,bax);
    }

    protected void configureEntailments(OWLTheory theory) {
        //theory.activateReduceToUns();
        theory.setIncludeTrivialEntailments(false);
        theory.setIncludeSubClassOfAxioms(false);
        theory.setIncludeClassAssertionAxioms(false);
        theory.setIncludeEquivalentClassAxioms(false);
        theory.setIncludeDisjointClassAxioms(false);
        theory.setIncludePropertyAssertAxioms(false);
        theory.setIncludeReferencingThingAxioms(false);
        theory.setIncludeOntologyAxioms(true);
    }

    public OWLTheory getExtendTheory(OWLOntology ontology, boolean dual) {
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

    public OWLOntology getOntologySimple (String name) {
        return getOntologyBase(new File(ClassLoader.getSystemResource(name).getPath()));
    }

    public OWLOntology getOntologyBase(File file) {
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
