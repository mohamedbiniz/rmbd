package at.ainf.protegeview.gui.options;

import at.ainf.protegeview.model.configuration.SearchConfiguration;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.09.12
 * Time: 09:59
 * To change this template use File | Settings | File Templates.
 */
public class AbstractOptPanel extends JPanel {

    private SearchConfiguration configuration;

    private SearchConfiguration newConfiguration;

    public AbstractOptPanel(SearchConfiguration configuration, SearchConfiguration newConfiguration) {
        this.newConfiguration = newConfiguration;
        this.configuration = configuration;

    }

    protected SearchConfiguration getConfiguration() {
        return configuration;
    }

    public SearchConfiguration getNewConfiguration() {
        return newConfiguration;
    }

    public void saveChanges() {

    }

}
