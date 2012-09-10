package at.ainf.protegeview.gui.axiomsetviews;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import at.ainf.protegeview.model.configuration.SearchCreator;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

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
public class ConflictsView extends AbstractAxiomSetView {

    @Override
    public void stateChanged(ChangeEvent e) {
        SearchCreator searchCreator = ((OntologyDiagnosisSearcher) e.getSource()).getSearchCreator();
        Set<AxiomSet<OWLLogicalAxiom>> setOfAxiomSets = searchCreator.getSearch().getConflicts();
        updateList(setOfAxiomSets);

    }

    @Override
    protected boolean isIncludeMeasure() {
        return false;
    }

    @Override
    protected Color getHeaderColor() {
        return new Color(52, 79, 255, 139);
    }

    @Override
    protected String getHeaderPrefix() {
        return "Conflict ";
    }

}
