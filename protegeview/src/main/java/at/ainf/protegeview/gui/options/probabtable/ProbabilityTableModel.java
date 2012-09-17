package at.ainf.protegeview.gui.options.probabtable;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.09.12
 * Time: 13:58
 * To change this template use File | Settings | File Templates.
 */
public class ProbabilityTableModel extends AbstractTableModel {


    private String[] columnNames = {"OWL keyword",
            "Error Probability",};

    LinkedList<ManchesterOWLSyntax> listKeywords = new LinkedList<ManchesterOWLSyntax>();


    HashMap<ManchesterOWLSyntax, BigDecimal> map = new HashMap<ManchesterOWLSyntax, BigDecimal>();








    public void updateCalcMapWithMap(Map<ManchesterOWLSyntax, BigDecimal> keywordProbabilities) {
        map.clear();
        for (ManchesterOWLSyntax k : keywordProbabilities.keySet()) {
            map.put(k,keywordProbabilities.get(k));
        }



    }



    ManchesterOWLSyntax[] keywords = {ManchesterOWLSyntax.SOME,
            ManchesterOWLSyntax.ONLY,
            ManchesterOWLSyntax.MIN,
            ManchesterOWLSyntax.MAX,
            ManchesterOWLSyntax.EXACTLY,
            ManchesterOWLSyntax.AND,
            ManchesterOWLSyntax.OR,
            ManchesterOWLSyntax.NOT,
            ManchesterOWLSyntax.VALUE,
            ManchesterOWLSyntax.INVERSE,
            ManchesterOWLSyntax.SUBCLASS_OF,
            ManchesterOWLSyntax.EQUIVALENT_TO,
            ManchesterOWLSyntax.DISJOINT_CLASSES,
            ManchesterOWLSyntax.DISJOINT_WITH,
            ManchesterOWLSyntax.FUNCTIONAL,
            ManchesterOWLSyntax.INVERSE_OF,
            ManchesterOWLSyntax.SUB_PROPERTY_OF,
            ManchesterOWLSyntax.SAME_AS,
            ManchesterOWLSyntax.DIFFERENT_FROM,
            ManchesterOWLSyntax.RANGE,
            ManchesterOWLSyntax.DOMAIN,
            ManchesterOWLSyntax.TYPE,
            ManchesterOWLSyntax.TRANSITIVE,
            ManchesterOWLSyntax.SYMMETRIC
    };

    public void updateUsedKw(Set<OWLLogicalAxiom> axioms) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

        String axiomStr = "";
        for (OWLLogicalAxiom axiom : axioms) {
            axiomStr += renderer.render(axiom) + "\t";
        }
        LinkedList<ManchesterOWLSyntax> keywords1 = new LinkedList<ManchesterOWLSyntax>();
        for (ManchesterOWLSyntax keyword : keywords) {
            if (axiomStr.contains(keyword.toString()))
                keywords1.add(keyword);
        }
        setListKeywords(keywords1);
    }

    public ProbabilityTableModel(Set<OWLLogicalAxiom> axioms, Map<ManchesterOWLSyntax, BigDecimal> keywordProbabilities) {
        // this.workspace = workspace;


        for (ManchesterOWLSyntax keyword : keywords) {
            listKeywords.add(keyword);
            this.map.put(keyword, BigDecimal.valueOf(0.01));
        }

        this.map.put(ManchesterOWLSyntax.SOME, BigDecimal.valueOf(0.05));
        this.map.put(ManchesterOWLSyntax.ONLY, BigDecimal.valueOf(0.05));
        this.map.put(ManchesterOWLSyntax.AND, BigDecimal.valueOf(0.001));
        this.map.put(ManchesterOWLSyntax.OR, BigDecimal.valueOf(0.001));
        this.map.put(ManchesterOWLSyntax.NOT, BigDecimal.valueOf(0.01));

        updateCalcMapWithMap(keywordProbabilities);
        updateUsedKw(axioms);

    }

    public HashMap<ManchesterOWLSyntax, BigDecimal> getMap() {
        return map;
    }

    public void setListKeywords(LinkedList<ManchesterOWLSyntax> kw) {
        listKeywords = kw;
    }




    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return listKeywords.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (col == 0)
            return listKeywords.get(row);
        else
            return map.get(listKeywords.get(row));

    }


    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return col >= 1;
    }



    public void setValueAt(Object value, int row, int col) {
        if (col == 0) {
            listKeywords.set(row, (ManchesterOWLSyntax) value);
        } else {
            map.put(listKeywords.get(row), BigDecimal.valueOf((Double) value));
        }


        fireTableCellUpdated(row, col);
    }
}
