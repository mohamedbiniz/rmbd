package at.ainf.protegeview.gui.options;

import at.ainf.protegeview.gui.options.probabpane.ProbabPane;
import at.ainf.protegeview.model.EditorKitHook;
import at.ainf.protegeview.model.configuration.SearchConfiguration;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.09.12
 * Time: 09:50
 * To change this template use File | Settings | File Templates.
 */
public class ProbabPanel extends AbstractOptPanel {

    private ProbabPane probabPane;

    public ProbabPanel(SearchConfiguration configuration, SearchConfiguration newConfiguration, EditorKitHook editorKitHook) {
        super(configuration,newConfiguration);
        setLayout(new BorderLayout());
        probabPane = new ProbabPane(editorKitHook);
        add(new JScrollPane(probabPane),BorderLayout.CENTER);
        JEditorPane helpArea = createHelpEditorPane();
        helpArea.setText("<html>Here you can specifiy the fault probabilities for the OWL keywords<html>");
        add(helpArea, BorderLayout.SOUTH);

    }

    @Override
    public void saveChanges() {

    }

    public Map<ManchesterOWLSyntax, BigDecimal> getMap() {
        return probabPane.getProbabMap();
    }

}
