package at.ainf.protegeview.gui.backgroundview;

import at.ainf.protegeview.gui.buttons.AbstractGuiButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.09.12
 * Time: 08:52
 * To change this template use File | Settings | File Templates.
 */
public class CreateBackgroundAxiomButton extends AbstractGuiButton {

    public CreateBackgroundAxiomButton(final BackgroundView backgroundView) {
        super("Create Axiom","Create Background Axiom","Button-New-icon.png", -1,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        CreateAxiomEditor editor = new CreateAxiomEditor(backgroundView.getEditorKitHook());
                        editor.show();
                    }
                }
        );

    }

}
