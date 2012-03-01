package at.ainf.protegeview.backgroundsearch;

import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.partitioning.scoring.Scoring;
import at.ainf.theory.storage.Partition;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.protegeview.controlpanel.QueryDebuggerPreference;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.07.11
 * Time: 11:35
 * To change this template use File | Settings | File Templates.
 */
public class EntailmentSearch {


    private TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> trSearch;

    private List<AxiomSet<OWLLogicalAxiom>> diags;

    private Scoring<OWLLogicalAxiom> func;

    private boolean isQueryMinimizerActive;


    public EntailmentSearch(
            TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search,
            List<AxiomSet<OWLLogicalAxiom>> diags, boolean isQueryMinimizerActive) {
        this.trSearch = search;
        this.diags = diags;
        String fun = QueryDebuggerPreference.getInstance().getScoringFunction();
        if (fun.equals("Entropy"))
            this.func = QSSFactory.createMinScoreQSS();
        else if (fun.equals("Split"))
            this.func = QSSFactory.createSplitInHalfQSS();
        this.isQueryMinimizerActive = isQueryMinimizerActive;
    }

    public Partition<OWLLogicalAxiom> doBackgroundSearch() {

        final EntailmentSearchDialog dialog = new EntailmentSearchDialog(null);

        EntailmentSearchTask task = new EntailmentSearchTask(trSearch,
                dialog.getTextArea(), diags, isQueryMinimizerActive, func);
        dialog.setWorker(task);
        task.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    }
                });
        task.execute();
        dialog.setVisible(true);

        Partition<OWLLogicalAxiom> bestPr = null;
        try {
            bestPr = task.get();
        } catch (CancellationException e) {

        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return bestPr;
    }


}
