package at.ainf.pluginprotege.debugmanager;

import at.ainf.diagnosis.storage.HittingSet;
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

    private HittingSet<OWLLogicalAxiom> treenode;

    public TreeNodeChangedEvent(Object source, HittingSet<OWLLogicalAxiom> treenode) {
        super(source);

        this.treenode = treenode;
    }

    public HittingSet<OWLLogicalAxiom> getTreenode(){
        return treenode;
    }
}
