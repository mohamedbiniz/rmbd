package at.ainf.protegeview.gui.buttons;

import at.ainf.protegeview.gui.axiomsetviews.DiagnosesView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.09.12
 * Time: 09:02
 * To change this template use File | Settings | File Templates.
 */
public class ResetButton extends AbstractGuiButton {

    public ResetButton(final DiagnosesView toolboxView) {
        super("Reset","Reset","clear.png",KeyEvent.VK_R,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!toolboxView.getEditorKitHook().getActiveOntologyDiagnosisSearcher().isTestcasesEmpty()) {
                            int answer = JOptionPane.showConfirmDialog(null, "Do you also want to delete testcases?", "Reset Type", JOptionPane.YES_NO_CANCEL_OPTION);
                            if (answer == JOptionPane.YES_OPTION)
                                toolboxView.getEditorKitHook().getActiveOntologyDiagnosisSearcher().doFullReset();
                            else if (answer == JOptionPane.NO_OPTION)
                                toolboxView.getEditorKitHook().getActiveOntologyDiagnosisSearcher().doReset();
                        }
                        else
                            toolboxView.getEditorKitHook().getActiveOntologyDiagnosisSearcher().doFullReset();

                    }
                }
        );

    }
}
