package at.ainf.protegeview.gui.queryview;

import at.ainf.protegeview.gui.axiomsetviews.axiomslist.AxiomListItem;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.09.12
 * Time: 13:21
 * To change this template use File | Settings | File Templates.
 */
public class QueryAxiomListItem extends AxiomListItem {

    public QueryAxiomListItem(OWLLogicalAxiom axiom, OWLOntology ontology) {
        super(axiom, ontology);
    }

}
