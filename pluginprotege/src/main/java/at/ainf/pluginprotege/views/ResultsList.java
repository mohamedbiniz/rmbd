package at.ainf.pluginprotege.views;

import at.ainf.diagnosis.storage.HittingSet;
import at.ainf.diagnosis.tree.NodeCostsEstimator;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.view.Copyable;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.*;
import java.util.List;


public class ResultsList extends BaseResultsList implements LinkedObjectComponent, Copyable {

    private OWLEditorKit owlEditorKit;

    protected void handleEdit() {

        //
    }

    private LinkedObjectComponentMediator mediator;

    public LinkedObjectComponentMediator getMediator() {
        return mediator;
    }

    public void setMediator(LinkedObjectComponentMediator mediator) {
        this.mediator = mediator;
    }

    private List<ChangeListener> copyListeners = new ArrayList<ChangeListener>();


    public ResultsList() {
         /*getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                ChangeEvent ev = new ChangeEvent(ResultsList.this);
                for (ChangeListener l : copyListeners){
                    l.stateChanged(ev);
                }
            } });*/
    }


    class ResultsListCellRenderer extends OWLCellRenderer {

        public ResultsListCellRenderer(OWLEditorKit owlEditorKit) {
            super(owlEditorKit);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
            Object renderableValue = value;
            if (value instanceof ResultsListSectionItem) {
                ResultsListSectionItem item = (ResultsListSectionItem) value;
                renderableValue = item.getOWLObject();
            }
            setHighlightKeywords(true);
            //setWrap(true);
            //setStrikeThrough(true);
            //setOpaque(true);
            //setPreferredWidth(list.getWidth());
            return super.getListCellRendererComponent(list, renderableValue, index, isSelected, cellHasFocus);
            }
    }

    public ResultsList(OWLEditorKit owlEditorKit) {
        this();
        this.owlEditorKit = owlEditorKit;
        setCellRenderer(new ResultsListCellRenderer (owlEditorKit));

        mediator = new LinkedObjectComponentMediator(owlEditorKit, this);

        /*getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                ChangeEvent ev = new ChangeEvent(ResultsList.this);
                for (ChangeListener l : copyListeners){
                    l.stateChanged(ev);
                }
            }
        });*/
    }

    public void addAxToResultsLst2(HashMap<ResultsListSection,Set<OWLLogicalAxiom>> map) {

        DefaultListModel model = (DefaultListModel) getModel();
        if (!model.isEmpty()) {
            model.clear();
        }

        for (ResultsListSection section : map.keySet()) {
            model.addElement(new ResultsListSection("Entailments of Diagnosis",section.getNum(),map.get(section)));

            for (OWLLogicalAxiom axiom : map.get(section))
                model.addElement(new ResultsListSectionItem(axiom, axiom, null));

        }

    }

    public void addAxiomToResultsList(NodeCostsEstimator<OWLLogicalAxiom> es, String name, Collection<? extends Set<OWLLogicalAxiom>> axioms, Map<Set<OWLLogicalAxiom>, Integer> map2) {

        DefaultListModel model = (DefaultListModel) getModel();
        if (!model.isEmpty()) {
            model.clear();
        }
        int num = 0;
        for (Set<OWLLogicalAxiom> axiomsConf : axioms) {
            if (name.equals("Diagnosis")) {
                //NumberFormat nf = NumberFormat.getNumberInstance();
                double p = -1;
                if (es != null)
                    p = ((HittingSet<OWLLogicalAxiom>)axiomsConf).getMeasure(); // p = es.getNodeSetCosts(axiomsConf);
                num++;
                if (es != null) {
                    model.addElement(new ResultsListSection(name,num,p,axiomsConf));
                } else {
                    model.addElement(new ResultsListSection(name,num,axiomsConf));
                }

            }
            else if (name.equals("Conflict Set ")) {
                double p = -1;
                if (es != null)
                    p = es.getNodeSetCosts(axiomsConf);
                num++;
                if (es != null) {
                    model.addElement(new ResultsListSection(name,num,p,axiomsConf));
                } else {
                    model.addElement(new ResultsListSection(name,num,axiomsConf));
                }

            }
            else if (name.equals("Entailments of Diagnosis")) {
                double p = -1;
                if (es != null)
                    p = es.getNodeSetCosts(axiomsConf);

                num++;
                if (es != null) {
                    model.addElement(new ResultsListSection(name,map2.get(axiomsConf),p,axiomsConf));
                } else {
                    model.addElement(new ResultsListSection(name,map2.get(axiomsConf),axiomsConf));
                }

            }
            else {
                model.addElement(new ResultsListSection(name,num,axiomsConf));
            }
            for (OWLLogicalAxiom elem : axiomsConf) {
                //  System.out.println("axiom is: " + elem);
                model.addElement(new ResultsListSectionItem(elem, elem,es));
            }
        }
    }

    protected List<MListButton> getButtons(Object value) {

            return Collections.emptyList();

    }


    public JComponent getComponent() {
        return this;
    }


    public OWLObject getLinkedObject() {
        return mediator.getLinkedObject();
    }


    public Point getMouseCellLocation() {
        Rectangle r = getMouseCellRect();
        if (r == null) {
            return null;
        }
        Point mousePos = getMousePosition();
        if (mousePos == null) {
            return null;
        }
        return new Point(mousePos.x - r.x, mousePos.y - r.y);
    }


    public Rectangle getMouseCellRect() {
        Point mousePos = getMousePosition();
        if (mousePos == null) {
            return null;
        }
        int sel = locationToIndex(mousePos);
        if (sel == -1) {
            return null;
        }
        return getCellBounds(sel, sel);
    }


    public void setLinkedObject(OWLObject object) {
        mediator.setLinkedObject(object);
    }


    public boolean canCopy() {
        return false;
    }


    public List<OWLObject> getObjectsToCopy() {
        List<OWLObject> copyObjects = new ArrayList<OWLObject>();
        for (Object sel : getSelectedValues()){
            if (sel instanceof ResultsListSectionItem){
                copyObjects.add(((ResultsListSectionItem)sel).getOWLObject());
            }
        }
        return copyObjects;
    }


    public void addChangeListener(ChangeListener changeListener) {
        copyListeners.add(changeListener);
    }


    public void removeChangeListener(ChangeListener changeListener) {
        copyListeners.remove(changeListener);
    }
}
