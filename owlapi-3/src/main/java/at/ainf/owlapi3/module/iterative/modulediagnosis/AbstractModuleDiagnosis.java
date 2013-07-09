package at.ainf.owlapi3.module.iterative.modulediagnosis;

import at.ainf.diagnosis.Debugger;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.owlapi3.module.iterative.modulecalc.HornModuleCalc;
import at.ainf.owlapi3.module.iterative.modulecalc.ModuleCalc;
import at.ainf.owlapi3.module.iterative.diagsearcher.ModuleDiagSearcher;
import at.ainf.owlapi3.reasoner.OWLSatReasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.math.BigDecimal;
import java.util.*;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.04.13
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractModuleDiagnosis implements Debugger<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> {

    private static Logger logger = LoggerFactory.getLogger(AbstractModuleDiagnosis.class.getName());

    protected static final OWLClass BOT_CLASS = OWLDataFactoryImpl.getInstance().getOWLNothing();

    private ModuleCalc moduleCalculator;

    private ModuleDiagSearcher diagSearcher;

    private Set<OWLLogicalAxiom> mappings;

    private final Set<OWLLogicalAxiom> ontoAxioms;

    private OWLReasonerFactory factory;

    public AbstractModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms,
                               OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {

        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(mappings);

        OWLOntology ontology = createOntology(allAxioms);
        moduleCalculator = createModuleCalc(factory, ontology);

        this.ontoAxioms = ontoAxioms;
        this.mappings = mappings;
        this.diagSearcher = moduleDiagSearcher;
        diagSearcher.setReasonerFactory(factory);
        this.factory = factory;

    }

    protected ModuleCalc createModuleCalc(OWLReasonerFactory factory, OWLOntology ontology) {
        if (factory.getReasonerName().equals(OWLSatReasoner.NAME))
            return new HornModuleCalc(ontology);
        else
            return new ModuleCalc(ontology, factory);

    }

    public AbstractModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms,
                                   OWLReasonerFactory factory) {

        this.ontoAxioms = ontoAxioms;
        this.mappings = mappings;
        this.factory = factory;

    }

    @Override
    public void setMaxDiagnosesNumber(int number) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxDiagnosesNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<FormulaSet<OWLLogicalAxiom>> getConflicts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<FormulaSet<OWLLogicalAxiom>> getDiagnoses() {
        throw new UnsupportedOperationException();
    }

    protected <X> FormulaSet<X> createFormulaSet(Set<X> set) {
        return new FormulaSetImpl<X>(BigDecimal.valueOf(-1),set, Collections.<X>emptySet());
    }

    public OWLReasonerFactory getReasonerFactory() {
        return factory;
    }

    protected ModuleCalc getModuleCalculator() {
        return moduleCalculator;
    }

    protected Set<OWLLogicalAxiom> getMappings() {
        return mappings;
    }

    protected Set<OWLLogicalAxiom> getOntoAxioms() {
        return ontoAxioms;
    }

    protected ModuleDiagSearcher getDiagSearcher() {
        return diagSearcher;
    }


}
