package at.ainf.protegeview.gui.queryview;

import org.protege.editor.owl.ui.framelist.ExplainButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 26.09.12
 * Time: 16:29
 * To change this template use File | Settings | File Templates.
 */
public class DebugExplainButton extends ExplainButton {

    public DebugExplainButton(final QueryAxiomList list) {
        super(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                list.handleAxiomExplain();
            }
        });
    }

}
