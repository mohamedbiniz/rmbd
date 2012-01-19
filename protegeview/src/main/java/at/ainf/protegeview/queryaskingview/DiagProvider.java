package at.ainf.protegeview.queryaskingview;

import at.ainf.theory.storage.Partition;
import at.ainf.theory.storage.HittingSet;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.protegeview.backgroundsearch.BackgroundSearcher;
import at.ainf.protegeview.backgroundsearch.EntailmentSearch;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 27.04.11
 * Time: 12:53
 * To change this template use File | Settings | File Templates.
 */
public class DiagProvider {

    private boolean isQueryMinimizerActive = true;

    private TreeSearch<? extends HittingSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> search = null;

    private int diagnos = 9;



    private LinkedList<HittingSet<OWLLogicalAxiom>> diagList = null;

    public DiagProvider(TreeSearch<? extends HittingSet<OWLLogicalAxiom>, Set<OWLLogicalAxiom>, OWLLogicalAxiom> s,
                        boolean isQueryMinimizerActive,
                        int number) {
        this.search = s;
        this.isQueryMinimizerActive = isQueryMinimizerActive;
        this.diagnos = number;
        this.search.setMaxHittingSets(0);
        calculateLead();

    }
    public void calculateLead() {

        diagList = new LinkedList<HittingSet<OWLLogicalAxiom>>();
        search.setMaxHittingSets(diagnos);
        BackgroundSearcher s = new BackgroundSearcher(search, null);
        s.doBackgroundSearch();
        Collection<? extends HittingSet<OWLLogicalAxiom>> res = search.getStorage().getValidHittingSets();

        for (HittingSet<OWLLogicalAxiom> hittingSet : res)
            diagList.add(hittingSet);
    }

    public Partition<OWLLogicalAxiom> getQuery() {
        //  if (search.getStorage().getValidHittingSets().size() == 1)
        //      throw new SingleDiagnosisLeftException("");
        if (diagList.size() == 0)
            return null;
        Partition<OWLLogicalAxiom> query = null;

            EntailmentSearch searcher = new EntailmentSearch(search, diagList, isQueryMinimizerActive);

            query = searcher.doBackgroundSearch();
        // if (query.partition == null)
        //    throw new NoFurtherQueryException();

        return query;
    }

}
