package at.ainf.pluginprotege;

import at.ainf.diagnosis.debugger.ProbabilityQueryDebugger;
import at.ainf.diagnosis.debugger.QueryDebugger;
import at.ainf.diagnosis.debugger.SimpleQueryDebugger;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.UnsatisfiableFormulasException;
import at.ainf.owlapi3.model.OWLAxiomNodeCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.11.11
 * Time: 14:35
 * To change this template use File | Settings | File Templates.
 */
public class OwlQueryDebuggerFactory {

    private static OWLTheory createTheory(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Set<OWLLogicalAxiom> backgroundAxioms, boolean reduce) {
        OWLTheory theory = null;

        try {
            theory = new OWLTheory(reasonerFactory,ontology,backgroundAxioms,reduce);
        } catch (UnsatisfiableFormulasException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return theory;
    }

    public static QueryDebugger<OWLLogicalAxiom> createQueryDebugger(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Set<OWLLogicalAxiom> backgroundAxioms, boolean reduce) {
        OWLTheory t = createTheory(reasonerFactory,ontology,backgroundAxioms,reduce);
        return new SimpleQueryDebugger<OWLLogicalAxiom>(t);
    }

    public static QueryDebugger<OWLLogicalAxiom> createQueryDebugger(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Set<OWLLogicalAxiom> backgroundAxioms ) {
        return createQueryDebugger(reasonerFactory,ontology,backgroundAxioms,false);
    }

    public static QueryDebugger<OWLLogicalAxiom> createQueryDebugger(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Set<OWLLogicalAxiom> backgroundAxioms, boolean reduce, Map<ManchesterOWLSyntax, Double> map) {
        OWLTheory t = createTheory(reasonerFactory,ontology,backgroundAxioms,reduce);
        return new ProbabilityQueryDebugger<OWLLogicalAxiom>(t,new OWLAxiomNodeCostsEstimator(t, map));
    }

    public static QueryDebugger<OWLLogicalAxiom> createQueryDebugger(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Set<OWLLogicalAxiom> backgroundAxioms, Map<ManchesterOWLSyntax, Double> map) {
        return createQueryDebugger(reasonerFactory,ontology,backgroundAxioms,false,map);
    }


}
