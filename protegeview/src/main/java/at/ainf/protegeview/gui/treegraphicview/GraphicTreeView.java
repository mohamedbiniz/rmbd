package at.ainf.protegeview.gui.treegraphicview;

import at.ainf.protegeview.gui.treegraphicview.common.GraphController;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.11.12
 * Time: 09:00
 * To change this template use File | Settings | File Templates.
 */
public class GraphicTreeView extends AbstractOWLClassViewComponent  {

    // reference to the graph controller object, gives access to graph model and graph functions
    private GraphController graphController;




    @Override
    public void initialiseClassView() throws Exception {
        setLayout(new BorderLayout());

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // TODO Auto-generated method stub
                super.componentResized(e);

            }
        });

        graphController = new GraphController(this, this.getOWLEditorKit());

        Dimension d = new Dimension(800, 600);
        setPreferredSize(d);
        setSize(d);
        setLocation(100, 50);

        setVisible(true);
    }


    @Override
    protected OWLClass updateView(OWLClass owlClass) {
        if(owlClass != null) {
            graphController.showOWLClass(owlClass);
        }

        return null;
    }

    @Override
    public void disposeView() {
        // TODO Auto-generated method stub

    }


}
