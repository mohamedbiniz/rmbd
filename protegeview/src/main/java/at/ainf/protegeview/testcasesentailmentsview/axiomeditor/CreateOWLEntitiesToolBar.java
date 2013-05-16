package at.ainf.protegeview.testcasesentailmentsview.axiomeditor;

import at.ainf.protegeview.controlpanel.DebugIconsLoader;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.03.11
 * Time: 12:57
 * To change this template use File | Settings | File Templates.
 */
public class CreateOWLEntitiesToolBar extends JPanel {


    private JToolBar toolBar;


    public CreateOWLEntitiesToolBar(OWLEditorKit eKit) {
        toolBar = new JToolBar();
        add(toolBar);
        toolBar.setOpaque(false);
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false);
        toolBar.setBorder(null);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.addAction(new CreateOWLEntityAction<OWLClass>("Add subclass", eKit, DebugIconsLoader.getIcon(DebugIconsLoader.CLASSADD), "Please enter a class name", OWLClass.class));
        this.addAction(new CreateOWLEntityAction<OWLObjectProperty>("Add object property", eKit, DebugIconsLoader.getIcon(DebugIconsLoader.PROPERTYOBJECT), "Please enter an object property name", OWLObjectProperty.class));
        this.addAction(new CreateOWLEntityAction<OWLDataProperty>("Add data property", eKit, DebugIconsLoader.getIcon(DebugIconsLoader.PROPERTYDATA), "Please enter a data property name", OWLDataProperty.class));
        this.addAction(new CreateOWLEntityAction<OWLNamedIndividual>("Add individual", eKit, DebugIconsLoader.getIcon(DebugIconsLoader.INDIVIDUALADD), "Please enter an individual name", OWLNamedIndividual.class));
        this.addAction(new CreateOWLEntityAction<OWLDatatype>("Add datatype", eKit, DebugIconsLoader.getIcon(DebugIconsLoader.DATARANGEADD), "Please enter a datatype name", OWLDatatype.class));


    }

    public void addAction(Action action) {

        JButton button = toolBar.add(action);
        // button.setToolTipText ((String) action.getValue(Action.NAME));
        button.setFocusable(false);


    }
}
