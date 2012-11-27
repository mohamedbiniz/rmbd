package at.ainf.protegeview.views.diagnosistreeview;

import at.ainf.diagnosis.tree.Node;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.08.11
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class DiagnosesTreeNode extends DefaultMutableTreeNode {

    private String label = "";

    private Node<OWLLogicalAxiom> node = null;

    private boolean conflictSetHeaderAdded = false;

    private int conflictNumber;

    private Collection<OWLLogicalAxiom> conflictSet;

    private OWLLogicalAxiom ax;

    public int getConflictNumber() {
        return conflictNumber;
    }

    public OWLLogicalAxiom getAx() {
        return ax;
    }

    public void setConflictNumber(int conflictNumber) {
        this.conflictNumber = conflictNumber;
    }

    public boolean isConflictSetHeaderAdded() {
        return conflictSetHeaderAdded;
    }

    public void setConflictSetHeaderAdded(boolean conflictSetHeaderAdded) {
        this.conflictSetHeaderAdded = conflictSetHeaderAdded;
    }

    public String getToolTipp() {
        if (node == null) {
            return null;
        }
        else {
            String result = "";
            ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

            for (OWLLogicalAxiom axiom : node.getParent().getAxiomSet()) {
              result = result + renderer.render(axiom) + "\n";
            }


            return result;
        }

    }

    public DiagnosesTreeNode(String label) {
        super(label);
        this.label = label;
    }

    public DiagnosesTreeNode(OWLLogicalAxiom axiom) {
        super(axiom);
        this.ax = axiom;
    }

    public DiagnosesTreeNode(Node<OWLLogicalAxiom> node) {
        super(node);
        this.node = node;
    }

    public boolean equals (Object o) {

        DiagnosesTreeNode other = (DiagnosesTreeNode)o;
        if (node != null)
            return node.equals(other.getNode());
        else
            return label.equals(other.getLabel());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Node<OWLLogicalAxiom> getNode() {
        return node;
    }

    public void setNode(Node<OWLLogicalAxiom> node) {
        this.node = node;
    }

    public void setConflictSet(Collection<OWLLogicalAxiom> c) {
        this.conflictSet = c;

    }

    public Collection<OWLLogicalAxiom> getConflictSet() {
        return conflictSet;
    }

    public boolean isHeader() {
        return conflictSet != null;
    }
}
