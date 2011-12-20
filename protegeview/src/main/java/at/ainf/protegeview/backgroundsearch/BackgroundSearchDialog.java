package at.ainf.protegeview.backgroundsearch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 26.07.11
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class BackgroundSearchDialog extends JDialog {

    private JTextArea textArea;

    private BackgroundSearcherTask worker = null;

    private JProgressBar progressBar = null;

    public void setWorker(BackgroundSearcherTask worker) {
        this.worker = worker;
    }

    public BackgroundSearchDialog(Frame parent) {
        super(parent);

        setPreferredSize(new Dimension(200, 130));
        progressBar = new JProgressBar(0,100);
        progressBar.setIndeterminate(true);
        JButton cancelButton = new JButton(new AbstractAction("cancel") {

            public void actionPerformed(ActionEvent e) {
                 worker.cancel(true);
                setVisible(false);
            }
        });
        textArea = new JTextArea();
        textArea.setText(" hitting sets: 0\n conflict sets: 0 \n");
        textArea.setEditable(false);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.add(progressBar, BorderLayout.NORTH);
        panel.add(textArea,BorderLayout.CENTER);
        panel.add(cancelButton, BorderLayout.SOUTH);
        setContentPane(panel);
        setModal(true);
        setLocationRelativeTo(getParent());
        pack();
        setTitle("Calculating ");

    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JTextArea getTextArea() {
        return textArea;
    }
}
