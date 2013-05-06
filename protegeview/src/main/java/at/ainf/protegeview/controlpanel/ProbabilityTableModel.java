package at.ainf.protegeview.controlpanel;

import at.ainf.protegeview.WorkspaceTab;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.02.11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
public class ProbabilityTableModel extends AbstractTableModel {


    private String[] columnNames = {"OWL keyword",
            "Error Probability",};

    LinkedList<ManchesterOWLSyntax> listKeywords = new LinkedList<ManchesterOWLSyntax>();

    LinkedList<ManchesterOWLSyntax> owlKeywords = new LinkedList<ManchesterOWLSyntax>();


    HashMap<ManchesterOWLSyntax, Double> map = new HashMap<ManchesterOWLSyntax, Double>();

    HashMap<ManchesterOWLSyntax, Double> calcMap = new HashMap<ManchesterOWLSyntax, Double>();

    private WorkspaceTab workspace;

    /*public Double getProbab(ManchesterOWLSyntax keyword) {
        return map.get(keyword);
    }

    public void setProbMap  (HashMap<ManchesterOWLSyntax, Double> m) {
        map = m;
    }*/

    public HashMap<ManchesterOWLSyntax, Double> getProbMap() {
        return map;
    }


    public void updateCalcMapWithMap() {
        calcMap.clear();
        for (ManchesterOWLSyntax k : map.keySet()) {
            calcMap.put(k,map.get(k));
        }



    }

    public HashMap<ManchesterOWLSyntax, Double> getCalcMap() {
        return calcMap;
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


    public ProbabilityTableModel() {
        // this.workspace = workspace;


        for (ManchesterOWLSyntax keyword : keywords) {
            owlKeywords.add(keyword);
            listKeywords.add(keyword);
            map.put(keyword, 0.01);
        }

        map.put(ManchesterOWLSyntax.SOME, 0.05);
        map.put(ManchesterOWLSyntax.ONLY, 0.05);
        map.put(ManchesterOWLSyntax.AND, 0.001);
        map.put(ManchesterOWLSyntax.OR, 0.001);
        map.put(ManchesterOWLSyntax.NOT, 0.01);

        updateCalcMapWithMap();
    }

    public ProbabilityTableModel(WorkspaceTab workspaceTab) {
        // this.workspace = workspace;


        for (ManchesterOWLSyntax keyword : keywords) {
            owlKeywords.add(keyword);
            listKeywords.add(keyword);
            map.put(keyword, 0.01);
        }

        map.put(ManchesterOWLSyntax.SOME, 0.05);
        map.put(ManchesterOWLSyntax.ONLY, 0.05);
        map.put(ManchesterOWLSyntax.AND, 0.001);
        map.put(ManchesterOWLSyntax.OR, 0.001);
        map.put(ManchesterOWLSyntax.NOT, 0.01);

        this.workspace = workspaceTab;

        updateCalcMapWithMap();
    }

    public LinkedList<ManchesterOWLSyntax> getOwlKeywords() {
        return owlKeywords;
    }

    public void setListKeywords(LinkedList<ManchesterOWLSyntax> kw) {
        listKeywords = kw;
        /*map.clear();

        for (ManchesterOWLSyntax keyword : listKeywords) {
            map.put(keyword, 0.01);
        }*/
    }

    /*private void normalizeProbabilites() {
        double sum = 0;

        for (ManchesterOWLSyntax keyword : map.keySet()) {
            sum += map.get(keyword);
        }
        for (ManchesterOWLSyntax keyword : map.keySet()) {
            map.put(keyword,map.get(keyword)/sum);
        }

    } */


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

    public void loadFromOntology() {
        workspace.loadProbabilites (map, keywords);
        updateCalcMapWithMap();
    }

    public void saveToTheOnt() {
        workspace.saveProbabilites(map,keywords);

    }

    public void loadFromCalcMap() {
        map.clear();
        for (ManchesterOWLSyntax k : calcMap.keySet()) {
            map.put(k,calcMap.get(k));
        }
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == 0) {
            listKeywords.set(row, (ManchesterOWLSyntax) value);
        } else {
            map.put(listKeywords.get(row), (Double) value);
        }


        fireTableCellUpdated(row, col);
    }
}
