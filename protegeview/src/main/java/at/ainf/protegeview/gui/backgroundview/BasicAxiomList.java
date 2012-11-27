package at.ainf.protegeview.gui.backgroundview;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.protegeview.gui.AbstractAxiomList;
import at.ainf.protegeview.gui.axiomsetviews.axiomslist.AxiomListHeader;
import at.ainf.protegeview.gui.axiomsetviews.axiomslist.AxiomListItem;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 24.09.12
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
public class BasicAxiomList extends AbstractAxiomList {

    public BasicAxiomList(OWLEditorKit editorKit) {
        super(editorKit);
    }

    public void updateList(Set<OWLLogicalAxiom> backgroundAxioms, OWLOntology ontology) {
        List<Object> items = new ArrayList<Object>();
        for (OWLLogicalAxiom axiom : backgroundAxioms)
            items.add(new AxiomListItem(axiom,ontology));

        /*DefaultListModel model = (DefaultListModel) getModel();
        model.clear();
        for (Object item : items)
            model.addElement(item); */
        setListData(items.toArray());

        //setFixedCellHeight(24);
    }

}
