package at.ainf.protegeview.gui.toolboxview;

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
public class OptionsButton extends AbstractGuiButton {

    public OptionsButton(final ToolboxView toolboxView) {
        super("Options","Options", "Option.png", KeyEvent.VK_O,
                new AbstractAction(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("test");
                    }
                }
        );

    }

}
