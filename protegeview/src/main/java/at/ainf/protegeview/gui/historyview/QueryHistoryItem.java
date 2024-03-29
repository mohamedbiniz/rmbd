package at.ainf.protegeview.gui.historyview;

import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import org.protege.editor.core.ui.list.MListItem;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.11.12
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */
public class QueryHistoryItem implements MListItem {

    private Logger logger = Logger.getLogger(QueryHistoryItem.class.getName());

    private Set<OWLLogicalAxiom> testcase;

    private OntologyDiagnosisSearcher.TestCaseType type;

    private Integer num;

    public Set<OWLLogicalAxiom> getTestcase() {
        return testcase;
    }

    public QueryHistoryItem(Set<OWLLogicalAxiom> testcase, OntologyDiagnosisSearcher.TestCaseType type, int num) {
        this.testcase = testcase;
        this.num = num;
        this.type = type;
    }


    public void handleEdit() {
    }


    public boolean handleDelete() {
        return false;
    }


    public String getTooltip() {
        // ontology.getOntologyID()
        return "Simple Axiom" ;
    }

    public OntologyDiagnosisSearcher.TestCaseType getType() {
        return type;
    }

    public boolean isDeleteable() {
        return true;
    }

    public String toString() {

        String typeStr = "";
        switch (type) {
            case ENTAILED_TC:
                typeStr = "Entailed ";
                break;
            case NON_ENTAILED_TC:
                typeStr = "Nonentailed ";
                break;
        }

        return "Answer " + num + " " + typeStr + " Axioms";

    }

    public boolean isEditable() {
        return false;
    }

}
