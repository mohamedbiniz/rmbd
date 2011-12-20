package at.ainf.protegeview.backgroundsearch;

import at.ainf.theory.model.SolverException;
import at.ainf.theory.model.UnsatisfiableFormulasException;
import at.ainf.theory.storage.HittingSet;
import at.ainf.theory.storage.StorageItemAddedEvent;
import at.ainf.theory.storage.StorageItemListener;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 26.07.11
 * Time: 09:14
 * To change this template use File | Settings | File Templates.
 */
public class BackgroundSearcherTask extends SwingWorker<BackgroundSearcherTask.Result, BackgroundSearcherTask.BackgroundTaskDataExchange>
        implements StorageItemListener {

    private JTextArea area;

    private JProgressBar progressBar;

    public enum Result {
        FINISHED,
        CANCELED,
        SOLVER_EXCEPTION,
        NO_CONFLICT_EXCEPTION,
        UNSAT_TESTS_EXCEPTION
    }

    public class BackgroundTaskDataExchange {

        public int hittingSetNum;

        public int conflictSetNum;

        public int m;

        public BackgroundTaskDataExchange(int hittingSets, int conflictSets, int maxDg) {
            this.m = maxDg;
            this.hittingSetNum = hittingSets;
            this.conflictSetNum = conflictSets;
        }

    }

    TreeSearch<? extends HittingSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> search;

    BackgroundSearcherTask(TreeSearch<? extends HittingSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> search, JTextArea area, JProgressBar progressBar) {
        this.area = area;
        this.search = search;
        this.progressBar = progressBar;
    }

    public void elementAdded(StorageItemAddedEvent e) {
        int numOfHittingSets = search.getStorage().getValidHittingSets().size();
        int numOfConflictSets = search.getStorage().getConflictSets().size();
        int maxDiags = search.getMaxHittingSets();

        publish(new BackgroundTaskDataExchange(numOfHittingSets, numOfConflictSets,
                maxDiags));
    }


    @Override
    public Result doInBackground() {                      // what to do if maxhittingset is 0
        try {
            search.run(search.getMaxHittingSets());
        } catch (SolverException e) {
            return Result.SOLVER_EXCEPTION;
        } catch (NoConflictException e) {
            return Result.NO_CONFLICT_EXCEPTION;
        } catch (UnsatisfiableFormulasException e) {
            return Result.UNSAT_TESTS_EXCEPTION;
        }

        return Result.FINISHED;
    }

    @Override
    protected void process(List<BackgroundTaskDataExchange> chunks) {
        for (BackgroundTaskDataExchange backgroundDataExchange : chunks) {
            area.setText(" hitting sets: " + backgroundDataExchange.hittingSetNum + "\n conflict sets: "
                    + backgroundDataExchange.conflictSetNum + "\n");
            if (backgroundDataExchange.m > 0) {
                if (progressBar.isIndeterminate()) {

                    progressBar.setIndeterminate(false);
                    progressBar.setStringPainted(true);
                }
                progressBar.setValue(100 * backgroundDataExchange.hittingSetNum / backgroundDataExchange.m);
                progressBar.setString(backgroundDataExchange.hittingSetNum + "/" + backgroundDataExchange.m);

            }
        }
    }

}
