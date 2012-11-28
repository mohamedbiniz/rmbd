package at.ainf.protegeview.debugmanager;

import at.ainf.diagnosis.storage.FormulaSet;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.08.11
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public class TreeNodeChangedEvent extends EventObject {

    private FormulaSet<OWLLogicalAxiom> treenode;

    public TreeNodeChangedEvent(Object source, FormulaSet<OWLLogicalAxiom> treenode) {
        super(source);

        this.treenode = treenode;
    }

    public FormulaSet<OWLLogicalAxiom> getTreenode(){
        return treenode;
    }
}
