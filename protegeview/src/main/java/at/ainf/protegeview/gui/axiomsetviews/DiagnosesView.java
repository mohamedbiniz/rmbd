package at.ainf.protegeview.gui.axiomsetviews;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import at.ainf.protegeview.model.configuration.SearchCreator;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.09.12
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */
public class DiagnosesView extends AbstractAxiomSetView {

    @Override
    public void stateChanged(ChangeEvent e) {
        SearchCreator searchCreator = ((OntologyDiagnosisSearcher) e.getSource()).getSearchCreator();
        Set<AxiomSet<OWLLogicalAxiom>> setOfAxiomSets = searchCreator.getSearch().getDiagnoses();
        updateList(setOfAxiomSets);

        JComponent statusBar = getOWLEditorKit().getOWLWorkspace().getStatusArea();
        statusBar.add(new JLabel("Diagnoses: " + searchCreator.getSearch().getDiagnoses().size()));
        statusBar.add(Box.createHorizontalStrut(20));

    }

    @Override
    protected Color getHeaderColor() {
        return new Color(85, 255, 97, 174);
    }

    @Override
    protected String getHeaderPrefix() {
        return "Diagnosis ";
    }

    @Override
    protected boolean isIncludeMeasure() {
        return true;
    }

}
