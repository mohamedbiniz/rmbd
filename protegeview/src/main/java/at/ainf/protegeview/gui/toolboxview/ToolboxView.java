package at.ainf.protegeview.gui.toolboxview;

import at.ainf.protegeview.gui.AbstractQueryViewComponent;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.09.12
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */
public class ToolboxView extends AbstractQueryViewComponent {

    private StartButton startButton;

    protected JToolBar createToolBar() {

        JToolBar toolBar = new JToolBar();

        toolBar.setOpaque(false);
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false);
        toolBar.setBorder(null);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        toolBar.add(new ResetButton(this));

        toolBar.addSeparator();

        startButton = new StartButton(this);
        toolBar.add(startButton);

        toolBar.addSeparator();

        toolBar.add(new OptionsButton(this));

        return toolBar;
    }

    protected void initialiseOWLView() throws Exception {
        super.initialiseOWLView();
        JToolBar toolBar = createToolBar();

        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);

    }


    @Override
    public void stateChanged(ChangeEvent e) {
        if (((OntologyDiagnosisSearcher)e.getSource()).getSearchStatus().equals(OntologyDiagnosisSearcher.SearchStatus.RUNNING))
            startButton.setEnabled(false);
        else
            startButton.setEnabled(true);

    }

}
