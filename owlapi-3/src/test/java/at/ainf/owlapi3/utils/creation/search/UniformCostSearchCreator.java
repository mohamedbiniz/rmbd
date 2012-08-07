package at.ainf.owlapi3.utils.creation.search;

import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.InvHsTreeSearch;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.model.OWLTheory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.08.12
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */
public class UniformCostSearchCreator implements SearchCreator {

    private OWLTheory theory;
    private boolean dual;

    public UniformCostSearchCreator(OWLTheory th, boolean dualSearch) {
        theory = th;
        dual = dualSearch;

    }

    @Override
    public TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch() {
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search;
        if (dual) {
            search = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
        } else {
            search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        }
        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        search.setTheory(theory);

        return search;
    }

}
