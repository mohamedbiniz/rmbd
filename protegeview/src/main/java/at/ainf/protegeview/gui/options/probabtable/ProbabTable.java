package at.ainf.protegeview.gui.options.probabtable;


import at.ainf.diagnosis.model.ITheory;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.protegeview.model.EditorKitHook;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import at.ainf.protegeview.model.configuration.SearchCreator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.09.12
 * Time: 13:53
 * To change this template use File | Settings | File Templates.
 */
public class ProbabTable extends JTable {

    public ProbabTable(EditorKitHook editorKitHook) {

        SearchCreator creator = editorKitHook.getActiveOntologyDiagnosisSearcher().getSearchCreator();
        OWLOntology ontology =  (OWLOntology) creator.getSearch().getTheory().getOriginalOntology();
        OWLAxiomKeywordCostsEstimator est = (OWLAxiomKeywordCostsEstimator) creator.getSearch().getCostsEstimator();
        Set<OWLLogicalAxiom> axioms = ontology.getLogicalAxioms();
        Map<ManchesterOWLSyntax, BigDecimal> probab = est.getKeywordProbabilities();
        setModel(new ProbabilityTableModel (axioms, probab));
        setPreferredScrollableViewportSize(new Dimension(160, 180));
        setRowHeight(22);
        setFillsViewportHeight(true);
        getColumnModel().getColumn(1).setCellEditor(new ProbabilityTableCellEditor());
        getColumnModel().getColumn(0).setCellRenderer(new KeywordFormatRenderer(editorKitHook.getOWLEditorKit()));
        getColumnModel().getColumn(1).setCellRenderer(new PrFormatRenderer());
    }

}
