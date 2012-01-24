package at.ainf.protegeview.backgroundsearch;

import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.ScoringFunction;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.storage.Partition;
import at.ainf.theory.model.SolverException;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.theory.storage.AxiomSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.protegeview.controlpanel.QueryDebuggerPreference;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import java.util.List;
import java.util.Set;
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

    private ScoringFunction<OWLLogicalAxiom> func;

    TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> search;

    EntailmentSearchTask(TreeSearch<? extends AxiomSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> search,
                         JTextArea area,
                         List<AxiomSet<OWLLogicalAxiom>> diags,
                         boolean isQueryMinimizerActive,
                         ScoringFunction<OWLLogicalAxiom> scoringFunc) {
        this.area = area;
        this.search = search;
        this.isQueryMinimizerActive = isQueryMinimizerActive;
        this.func = scoringFunc;
        this.diags = diags;
    }

    @Override
    public Partition<OWLLogicalAxiom> doInBackground() {
        CKK<OWLLogicalAxiom> ckk = new CKK<OWLLogicalAxiom>(search.getTheory(), func);
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
        QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(query, search.getTheory());
        NewQuickXplain<OWLLogicalAxiom> q = new NewQuickXplain<OWLLogicalAxiom>();
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
            if (!hs.getEntailments().containsAll(query.partition) && !search.getTheory().diagnosisEntails(hs, query.partition))
                throw new IllegalStateException("DX diagnosis is not entailing a query");
        }


        for (AxiomSet<OWLLogicalAxiom> hs : query.dnx) {
            if (search.getTheory().diagnosisConsistent(hs, query.partition))
                throw new IllegalStateException("DNX diagnosis might entail a query");
        }

        for (AxiomSet<OWLLogicalAxiom> hs : query.dz) {
            if (search.getTheory().diagnosisEntails(hs, query.partition) || hs.getEntailments().containsAll(query.partition))
                throw new IllegalStateException("DZ diagnosis entails a query");
            if (!search.getTheory().diagnosisConsistent(hs, query.partition))
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
