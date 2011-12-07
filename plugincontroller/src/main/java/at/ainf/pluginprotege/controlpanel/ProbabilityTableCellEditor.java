package at.ainf.pluginprotege.controlpanel;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 28.02.11
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
public class ProbabilityTableCellEditor extends DefaultCellEditor {
    JFormattedTextField ftf;
    NumberFormat integerFormat;
    private Double minimum, maximum;

    public ProbabilityTableCellEditor() {
        super(new JFormattedTextField());
        ftf = (JFormattedTextField) getComponent();
        minimum = new Double(0.0);
        maximum = new Double(1.0);

        //Set up the axiomeditor for the integer cells.
        integerFormat = NumberFormat.getNumberInstance();
        NumberFormatter intFormatter = new NumberFormatter(integerFormat);
        intFormatter.setFormat(integerFormat);
        intFormatter.setMinimum(minimum);
        intFormatter.setMaximum(maximum);

        ftf.setFormatterFactory(
                new DefaultFormatterFactory(intFormatter));
        ftf.setValue(minimum);
        ftf.setHorizontalAlignment(JTextField.RIGHT);
        ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);


        ftf.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, 0),
                "check");
        ftf.getActionMap().put("check", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!ftf.isEditValid()) {
                    if (userSaysRevert()) {
                        ftf.postActionEvent();
                    }

                } else try {
                    ftf.commitEdit();
                    ftf.postActionEvent();
                } catch (java.text.ParseException exc) {
                }
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value, boolean isSelected,
                                                 int row, int column) {
        JFormattedTextField ftf =
                (JFormattedTextField) super.getTableCellEditorComponent(
                        table, value, isSelected, row, column);
        ftf.setValue(value);
        return ftf;
    }

    public Object getCellEditorValue() {
        JFormattedTextField ftf = (JFormattedTextField) getComponent();
        Object o = ftf.getValue();
        if (o instanceof Double) {
            return o;
        } else if (o instanceof Number) {
            return new Double(((Number) o).doubleValue());
        } else {
            try {
                return integerFormat.parseObject(o.toString());
            } catch (ParseException exc) {
                System.err.println("getCellEditorValue: can't parse o: " + o);
                return null;
            }
        }
    }

    public boolean stopCellEditing() {
        JFormattedTextField ftf = (JFormattedTextField) getComponent();
        if (ftf.isEditValid()) {
            try {
                ftf.commitEdit();
            } catch (java.text.ParseException exc) {
            }

        } else {
            if (!userSaysRevert()) {
                return false;
            }
        }
        return super.stopCellEditing();
    }


    protected boolean userSaysRevert() {
        Toolkit.getDefaultToolkit().beep();
        ftf.selectAll();
        Object[] options = {"Edit",
                "Revert"};
        int answer = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(ftf),
                "The value must be a value between 0 and 100 percent.\n",
                "Invalid Text Entered",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[1]);

        if (answer == 1) {
            ftf.setValue(ftf.getValue());
            return true;
        }
        return false;
    }


}
