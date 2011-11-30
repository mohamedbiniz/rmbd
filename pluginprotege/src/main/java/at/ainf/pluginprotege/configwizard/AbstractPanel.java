package at.ainf.pluginprotege.configwizard;

import org.protege.editor.core.ModelManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.wizard.WizardPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 04.08.11
 * Time: 13:17
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractPanel extends WizardPanel {

    /**
     *
     */
    private static final long serialVersionUID = 8512811313733522394L;

    private JTextArea instructionArea;

    private JPanel marginPanel;

    private JLabel marginLabel;

    private Icon backgroundImage;

    private boolean notifyDisplaying;

    private String title;

    private EditorKit editorKit;


    public OWLModelManager getOWLModelManager() {
        return (OWLModelManager) getModelManager();
    }


    public OWLEditorKit getOWLEditorKit() {
        return (OWLEditorKit) getEditorKit();
    }

    public AbstractPanel(Object id, String title, EditorKit editorKit, int number) {
        super(id);
        this.editorKit = editorKit;
        this.title = title;
        this.number = number;
        notifyDisplaying = true;
        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if (isShowing()) {
                    if (notifyDisplaying) {
                        displayingPanel();
                    }
                    else {
                        notifyDisplaying = false;
                    }
                }
                else {
                    notifyDisplaying = true;
                }
            }
        });
        createUI();
    }


    public ModelManager getModelManager() {
        return editorKit.getModelManager();
    }


    public EditorKit getEditorKit() {
        return editorKit;
    }

    public void setBackgroundImage() {
        backgroundImage = null;
    }

    protected String[] names = {"Welcome",
                      "Background Knowledge",
                      "Search Type",
                      "Error Probabilities",
                      "Number Leading Diagnoses",
                      "Scoring Function",
                      "Query Minimizer",
                      "Reduce Incoherency",
                      "Include Entailments",
                      "Ontology Axioms",
                      "Finish "};

    private int number = 0;

    public JPanel createNamePanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        Font normFont = new Font("Sans Serif ", Font.PLAIN, 14);

        for (int i = 0; i < names.length; i++) {
            JLabel label = new JLabel((i+1) + ". " + names[i] + "\n\n");
            if (number == i + 1)
                label.setFont(normFont.deriveFont(Font.BOLD));
            else
                label.setFont(normFont);

            panel.add(new JLabel(" "));

            panel.add(label);

        }

        return panel;

    }

    final protected void createUI() {
        backgroundImage = Icons.getIcon("logo.wizard.png");
        setLayout(new BorderLayout(7, 7));
        marginPanel = new JPanel(new BorderLayout());
        marginPanel.setPreferredSize(new Dimension(200, 400));
        add(marginPanel, BorderLayout.WEST);
        marginPanel.setOpaque(false);
        marginPanel.setEnabled(false);
        marginLabel = new JLabel();
        marginPanel.add(createNamePanel(), BorderLayout.CENTER);
        marginPanel.add(marginLabel, BorderLayout.NORTH);
        marginLabel.setBorder(BorderFactory.createEmptyBorder(30, 8, 0, 0));
        instructionArea = new JTextArea("");
        instructionArea.setOpaque(false);
        instructionArea.setWrapStyleWord(true);
        instructionArea.setLineWrap(true);
        instructionArea.setEditable(false);
        instructionArea.setFont(instructionArea.getFont().deriveFont(Font.PLAIN, 14.0f) );

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel containerPanel = new JPanel(new BorderLayout(7, 7));
        add(containerPanel);
        containerPanel.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setOpaque(false);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14.0f));
        containerPanel.add(label, BorderLayout.NORTH);

        JPanel contentAndInstructionHolder = new HolderPanel();
        contentAndInstructionHolder.add(instructionArea, BorderLayout.NORTH);
        JPanel contentBorderPanel = new JPanel(new BorderLayout());
        contentBorderPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 0));
        JPanel content = new JPanel();
        contentBorderPanel.add(content);
        contentBorderPanel.setOpaque(false);
        content.setOpaque(false);
        contentAndInstructionHolder.add(contentBorderPanel, BorderLayout.CENTER);
        containerPanel.add(contentAndInstructionHolder);
        createUI(content);
        setComponentTransparency(content);
    }


    private static Set<Class> nonTransparentComponents;


    static {
        nonTransparentComponents = new HashSet<Class>();
        nonTransparentComponents.add(JTextComponent.class);
        nonTransparentComponents.add(JList.class);
        nonTransparentComponents.add(JTree.class);
        nonTransparentComponents.add(JTable.class);
        nonTransparentComponents.add(JScrollPane.class);
        nonTransparentComponents.add(JComboBox.class);
    }


    protected void setComponentTransparency(Component component) {
        if (component instanceof JComponent) {
            for (Class c : nonTransparentComponents) {
                if (c.isInstance(component)) {
                    return;
                }
            }
            ((JComponent) component).setOpaque(false);
        }
        if (component instanceof Container) {
            Container container = (Container) component;
            Component [] components = container.getComponents();
            for (int i = 0; i < components.length; i++) {
                setComponentTransparency(components[i]);
            }
        }
    }


    public void setBackgroundImage(Icon imageIcon) {
        backgroundImage = imageIcon;
    }


    public void setInstructions(String instructions) {
        instructionArea.setText(instructions);
    }


    public Dimension getPreferredSize() {
        return new Dimension(800, 550);
    }


    protected abstract void createUI(JComponent parent);


    private class HolderPanel extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = 5476717953072456842L;
        private Color color;


        public HolderPanel() {
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
                                                         BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            setLayout(new BorderLayout(7, 20));
            setOpaque(false);
            color = new Color(255, 255, 255, 230);
        }


        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle r = g.getClipBounds();
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
    }


    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            backgroundImage.paintIcon(this, g, 0, 0);
        }
    }
}
