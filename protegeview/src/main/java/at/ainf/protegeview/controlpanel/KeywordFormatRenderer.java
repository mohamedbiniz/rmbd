package at.ainf.protegeview.controlpanel;

import at.ainf.protegeview.WorkspaceTab;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.07.11
 * Time: 14:25
 * To change this template use File | Settings | File Templates.
 */
public class KeywordFormatRenderer extends DefaultTableCellRenderer {

        WorkspaceTab workspaceTab;

        public KeywordFormatRenderer(WorkspaceTab workspaceTab) {

            this.workspaceTab = workspaceTab;

        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            //value = DecimalFormat.getPercentInstance().format(value);

            JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            renderedLabel.setHorizontalAlignment(SwingConstants.LEFT);

            Font keywordfont =
                    new Font(workspaceTab.getOWLEditorKit().getWorkspace().getFont().getName(), Font.BOLD, 14);
            renderedLabel.setFont(keywordfont);

            Color color =
            workspaceTab.getOWLEditorKit().getWorkspace().getKeyWordColorMap().get(renderedLabel.getText());
            if (color == null) {
                color = Color.BLACK;
            }
            renderedLabel.setForeground(color);


            return renderedLabel;
        }
    }
