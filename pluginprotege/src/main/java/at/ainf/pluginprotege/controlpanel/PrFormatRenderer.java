package at.ainf.pluginprotege.controlpanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.07.11
 * Time: 14:24
 * To change this template use File | Settings | File Templates.
 */
public class PrFormatRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            NumberFormat nf = DecimalFormat.getNumberInstance();

            nf.setMinimumFractionDigits(4);
            nf.setGroupingUsed(true);
            value = nf.format(value);
            JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            renderedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            return renderedLabel;
        }
    }