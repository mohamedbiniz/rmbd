package at.ainf.protegeview.gui.axiomsetviews.axiomslist;

import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.owl.ui.view.Copyable;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.event.ChangeListener;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: pfleiss
* Date: 05.09.12
* Time: 16:40
* To change this template use File | Settings | File Templates.
*/
public class AxiomListItem implements MListItem {

    private OWLLogicalAxiom axiom;

    private OWLOntology ontology;

    public OWLLogicalAxiom getAxiom() {
        return axiom;
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public AxiomListItem(OWLLogicalAxiom axiom, OWLOntology ontology) {
        this.axiom = axiom;
        this.ontology = ontology;
    }


    public boolean isEditable() {
        return false;
    }


    public void handleEdit() {
    }


    public boolean isDeleteable() {
        return false;
    }


    public boolean handleDelete() {
        return false;
    }


    public String getTooltip() {
        // ontology.getOntologyID()
        return "Simple Axiom" ;
    }

}
