package at.ainf.protegeview.views.diagnosistreeview;

import at.ainf.theory.storage.HittingSet;
import at.ainf.diagnosis.tree.Node;
import at.ainf.protegeview.debugmanager.DebugManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.tree.OWLLinkedObjectTree;
import org.protege.editor.owl.ui.usage.UsageByEntityTreeModel;
import org.protege.editor.owl.ui.usage.UsagePreferences;
import org.protege.editor.owl.ui.usage.UsageTreeModel;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 20.08.11
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class DiagnosesTree extends OWLLinkedObjectTree {

    /**
     *
     */
    private static final long serialVersionUID = 2978742855867968571L;

    private OWLEditorKit owlEditorKit;

    private OWLEntity entity;

    DiagnosesTreeModel model;

    public DiagnosesTree(OWLEditorKit owlEditorKit) {
        super(owlEditorKit);
        this.owlEditorKit = owlEditorKit;
        model = new DiagnosesTreeModel(owlEditorKit);
        model.setHittingSets(null);
        setModel(model);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new DiagnosesTreeCellRenderer(owlEditorKit));
        addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DiagnosesTreeNode node = (DiagnosesTreeNode) getLastSelectedPathComponent();
                if (node==null)
                    return;
                String label = node.getLabel();
                if (!label.equals("")) {
                    DebugManager.getInstance().setConflictSetSelected(node.getConflictNumber());
                    DebugManager.getInstance().notifyConflictSetSelected();
                }
            }
        });
    }

    public void hittingSets(Set<? extends HittingSet<OWLLogicalAxiom>> hs) {
        //final DiagnosesTreeModel model = new DiagnosesTreeModel(owlEditorKit);
        model.setHittingSets(hs);
        for (int i = getRowCount() -1; i > 0; i--)
            collapseRow(i);
        expandRow(0);
    }

    public void setOWLEntity(OWLEntity entity) {
        this.entity = entity;

        final UsagePreferences p = UsagePreferences.getInstance();
        final UsageTreeModel model = new UsageByEntityTreeModel(owlEditorKit);
        model.addFilters(p.getActiveFilters());
        model.setOWLEntity(entity);
        setModel(model);

        /*for (int i = 0; i < getRowCount(); i++) {
            expandRow(i);
            if (i > 100) {
                break;
            }
        } */
    }

    private TreePath path;

    public void setPath (TreePath path) {
        this.path = path;
    }

    public void setDisplayNodeChanged(Node<OWLLogicalAxiom> treenode) {
        if (treenode==null)
            path = null;
        else
            setPath(new TreePath(((DiagnosesTreeModel) getModel()).getNode(treenode).getPath()));
        for (int i = getRowCount() -1; i > 0; i--)
            collapseRow(i);
        if (path != null)
          expandPath(path.getParentPath());
        else
            expandRow(0);

    }

    protected boolean isInPath (DiagnosesTreeNode node) {
        if (path==null) {
            return false;
        }
        if (node.equals(getModel().getRoot())) {
            return false;
        }
        Object[] nodes = path.getPath();
        for (Object obj : nodes) {
            DiagnosesTreeNode nod = (DiagnosesTreeNode) obj;
            if (node.equals(nod)) {
                return true;
            }
        }

        return false;
    }




    private class DiagnosesTreeCellRenderer extends OWLCellRenderer {


        public DiagnosesTreeCellRenderer(OWLEditorKit owlEditorKit) {
            super(owlEditorKit);
        }


        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            if (! (value instanceof DiagnosesTreeNode))
                return null;
            DiagnosesTreeNode node = (DiagnosesTreeNode) value;
            setOpaque(true);

            Object o = null;
            if (node.getNode() != null) {
              o = node.getNode().getArcLabel();
            }
            else if(node.getAx() != null) {
                o = node.getAx();
            }
            else {
              o = node.getLabel();
                }
            //setFocusedEntity(entity);
            JComponent c = (JComponent) super.getTreeCellRendererComponent(tree,
                                                                           o,
                                                                           sel,
                                                                           expanded,
                                                                           leaf,
                                                                           row,
                                                                           hasFocus);

            c.setToolTipText (node.getToolTipp());
            if (isInPath(node)) {
                  c.setBackground(new Color(173,255,47));
            }
            setHighlightKeywords(true);



            return c;
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color oldColor = g.getColor();
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < getRowCount(); i++) {
            Rectangle rowBounds = getRowBounds(i);
            if (g.getClipBounds().intersects(rowBounds)) {
                if (getPathForRow(i).getPathCount() == 2) {
                    g.drawLine(0, rowBounds.y, getWidth(), rowBounds.y);
                }
            }
        }
        g.setColor(oldColor);
    }
}
