package at.ainf.protegeview.views;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.05.11
 * Time: 09:33
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractProtegeResultView  extends AbstractOWLViewComponent {

    protected ResultsList list;

    protected void initialiseOWLView() throws Exception {

        setLayout(new BorderLayout(10, 10));
        list = new ResultsList(getOWLEditorKit());

        list.setModel(new DefaultListModel());
        JComponent panel =  new JPanel(new BorderLayout(10, 10));
        panel.add(ComponentFactory.createScrollPane(list));

        add(panel, BorderLayout.CENTER);


    }


    protected void disposeOWLView() {

    }

}
