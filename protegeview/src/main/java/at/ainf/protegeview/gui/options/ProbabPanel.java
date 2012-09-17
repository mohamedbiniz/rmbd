package at.ainf.protegeview.gui.options;

import at.ainf.protegeview.gui.options.probabtable.ProbabTable;
import at.ainf.protegeview.gui.options.probabtable.ProbabilityTableModel;
import at.ainf.protegeview.model.EditorKitHook;
import at.ainf.protegeview.model.OntologyDiagnosisSearcher;
import at.ainf.protegeview.model.configuration.SearchConfiguration;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;

import javax.swing.*;
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

    private ProbabTable table;

    private Map<ManchesterOWLSyntax,BigDecimal> map;

    public ProbabPanel(SearchConfiguration configuration, SearchConfiguration newConfiguration, EditorKitHook editorKitHook) {
        super(configuration,newConfiguration);
        this.map = new HashMap<ManchesterOWLSyntax, BigDecimal>();
        table = new ProbabTable(editorKitHook);
        add(new JScrollPane(table));

    }

    @Override
    public void saveChanges() {
        map = ((ProbabilityTableModel)table.getModel()).getMap();
    }

    public Map<ManchesterOWLSyntax, BigDecimal> getMap() {
        return map;
    }

}
