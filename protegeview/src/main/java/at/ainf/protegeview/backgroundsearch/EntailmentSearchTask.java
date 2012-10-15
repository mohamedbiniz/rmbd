package at.ainf.protegeview.backgroundsearch;

import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.Scoring;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.protegeview.controlpanel.QueryDebuggerPreference;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.07.11
 * Time: 11:35
 * To change this template use File | Settings | File Templates.
 */
public class EntailmentSearchTask extends SwingWorker<Partition<OWLLogicalAxiom>, String> {

    private JTextArea area;

    private boolean isQueryMinimizerActive;

    private List<AxiomSet<OWLLogicalAxiom>> diags;

    private Scoring<OWLLogicalAxiom> func;

    TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search;

    EntailmentSearchTask(TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search,
                         JTextArea area,
                         List<AxiomSet<OWLLogicalAxiom>> diags,
                         boolean isQueryMinimizerActive,
                         Scoring<OWLLogicalAxiom> scoringFunc) {
        this.area = area;
        this.search = search;
        this.isQueryMinimizerActive = isQueryMinimizerActive;
        this.func = scoringFunc;
        this.diags = diags;
    }

    @Override
    public Partition<OWLLogicalAxiom> doInBackground() {
        CKK<OWLLogicalAxiom> ckk = new CKK<OWLLogicalAxiom>(search.getSearchable(), func);
        ckk.setThreshold(QueryDebuggerPreference.getInstance().getPartitioningThres());

        TreeSet<AxiomSet<OWLLogicalAxiom>> set = new TreeSet<AxiomSet<OWLLogicalAxiom>>(diags);

        Partition<OWLLogicalAxiom> best = null;
        publish("Calculating Entailments ...");
        try {
            best = ckk.generatePartition(set);
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (isQueryMinimizerActive) {
            publish("Minimizing Partition ...");
            minimizePartitionAx(best);
        }

        return best;
    }

    private void minimizePartitionAx(Partition<OWLLogicalAxiom> query) {
        if (query.partition == null) return;
        QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(query, search.getSearchable());
        QuickXplain<OWLLogicalAxiom> q = new QuickXplain<OWLLogicalAxiom>();
        try {
            query.partition = q.search(mnz, query.partition, null);
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        for (AxiomSet<OWLLogicalAxiom> hs : query.dx) {
            if (!hs.getEntailments().containsAll(query.partition) && !search.getSearchable().diagnosisEntails(hs, query.partition))
                throw new IllegalStateException("DX diagnosis is not entailing a query");
        }


        for (AxiomSet<OWLLogicalAxiom> hs : query.dnx) {
            if (search.getSearchable().diagnosisConsistent(hs, query.partition))
                throw new IllegalStateException("DNX diagnosis might entail a query");
        }

        for (AxiomSet<OWLLogicalAxiom> hs : query.dz) {
            if (search.getSearchable().diagnosisEntails(hs, query.partition) || hs.getEntailments().containsAll(query.partition))
                throw new IllegalStateException("DZ diagnosis entails a query");
            if (!search.getSearchable().diagnosisConsistent(hs, query.partition))
                throw new IllegalStateException("DZ diagnosis entails a query complement");
        }
    }

    @Override
    protected void process(List<String> messages) {
        for (String message : messages) {
            area.append(message + " \n");

        }
    }
}
