package at.ainf.protegeview.gui.axiomsetviews;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.protegeview.gui.buttons.ResetButton;
import at.ainf.protegeview.gui.buttons.StartButton;
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

    private StartButton startButton;

    @Override
    protected void initialiseOWLView() throws Exception {
        super.initialiseOWLView();
        //add(createDiagnosesToolBar(), BorderLayout.NORTH);
    }

    protected JToolBar createDiagnosesToolBar() {
        JToolBar toolBar = new JToolBar();

        toolBar.setFloatable(false);
        startButton = new StartButton(this);
        toolBar.add(startButton);
        toolBar.add(new ResetButton(this));
        toolBar.add(Box.createHorizontalGlue());

        return toolBar;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        SearchCreator searchCreator = ((OntologyDiagnosisSearcher) e.getSource()).getSearchCreator();
        Set<AxiomSet<OWLLogicalAxiom>> setOfAxiomSets = searchCreator.getSearch().getDiagnoses();
        /*if (((OntologyDiagnosisSearcher)e.getSource()).getSearchStatus().equals(OntologyDiagnosisSearcher.SearchStatus.RUNNING))
            startButton.setEnabled(false);
        else
            startButton.setEnabled(true);*/
        updateList(setOfAxiomSets);

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
