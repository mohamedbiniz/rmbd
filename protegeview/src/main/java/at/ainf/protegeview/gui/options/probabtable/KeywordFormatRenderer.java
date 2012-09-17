package at.ainf.protegeview.gui.options.probabtable;



import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.09.12
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public class KeywordFormatRenderer extends DefaultTableCellRenderer {



    public KeywordFormatRenderer(OWLEditorKit editorKit) {
        this.editorKit = editorKit;

    }

    private OWLEditorKit editorKit;

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {


        JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        renderedLabel.setHorizontalAlignment(SwingConstants.LEFT);

        Font keywordfont =
                new Font(editorKit.getWorkspace().getFont().getName(), Font.BOLD, 14);
        renderedLabel.setFont(keywordfont);

        Color color = editorKit.getWorkspace().getKeyWordColorMap().get(renderedLabel.getText());
        if (color == null) {
            color = Color.BLACK;
        }
        renderedLabel.setForeground(color);


        return renderedLabel;
    }
}
