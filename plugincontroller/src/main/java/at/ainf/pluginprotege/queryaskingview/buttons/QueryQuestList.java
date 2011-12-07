package at.ainf.pluginprotege.queryaskingview.buttons;

import at.ainf.pluginprotege.queryaskingview.QueryShowPanel;
import at.ainf.pluginprotege.views.ResultsList;
import org.protege.editor.core.ui.list.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.05.11
 * Time: 08:32
 * To change this template use File | Settings | File Templates.
 */
public class QueryQuestList extends ResultsList {

    private QueryShowPanel panel;

    public QueryQuestList(QueryShowPanel panel) {
        this.panel = panel;

        //this.owlEditorKit = owlEditorKit;
        /*setCellRenderer(new OWLCellRenderer(null) {

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
            Object renderableValue = value;
            if (value instanceof ResultsListSectionItem) {
                ResultsListSectionItem item = (ResultsListSectionItem) value;
                renderableValue = item.getOWLObject();
            }
            setHighlightKeywords(true);
            //setWrap(true);
            //setStrikeThrough(true);
            //setOpaque(true);
            setPreferredWidth(list.getWidth());
            return super.getListCellRendererComponent(list, renderableValue, index, isSelected, cellHasFocus);
            }
        });*/

        //mediator = new LinkedObjectComponentMediator(owlEditorKit, this);

        /*getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                ChangeEvent ev = new ChangeEvent(ResultsList.this);
                for (ChangeListener l : copyListeners) {
                    l.stateChanged(ev);
                }
            }
        });*/
    }


    protected List<MListButton> getButtons(Object value) {
		List<MListButton> buttons = new ArrayList<MListButton>();

        final QueryEntButton entButton = new QueryEntButton(null);
        entButton.setActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                panel.deactToolbarYesNoButton();
                handlEnt();
                panel.setButtonMarkedIfActiveAll();
                panel.touchModelElements();
            }
        });


		buttons.add(entButton);

        buttons.add(new QueryNotEntaiButton(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                panel.deactToolbarYesNoButton();
                handlNoEnt();
                panel.setButtonMarkedIfActiveAll();
                panel.touchModelElements();
            }
        }));

        buttons.add(new QueryUnknownButton(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                handlUnknownEnt();
            }
        }));

		return buttons;
	}

    protected void handlEnt() {
		if (this.getSelectedValue() instanceof QueryQuestListItem) {
			QueryQuestListItem item = (QueryQuestListItem) this.getSelectedValue();
            if (panel.isItemMarkedDifferent(item,true)) {
                int n = JOptionPane.showConfirmDialog(null, "You can not mix positive and negative answers to a query. Do you want to mark this axiom?", "Cannot Mix answers", JOptionPane.YES_NO_OPTION);
                if (n==1) {
                    return;
                  }
                else {
                   panel.unmarkAxioms(false);
                }
            }
			item.handleEntailed();
		}
	}

    protected void handlNoEnt() {
        if (this.getSelectedValue() instanceof QueryQuestListItem) {
			QueryQuestListItem item = (QueryQuestListItem) this.getSelectedValue();
            if (panel.isItemMarkedDifferent(item,false)) {
                int n = JOptionPane.showConfirmDialog(null, "You can not mix positive and negative answers to a query. Do you want to mark this axiom?", "Cannot Mix answers", JOptionPane.YES_NO_OPTION);
                if (n==1) {
                    return;
                  }
                else {
                   panel.unmarkAxioms(true);
                }
            }
			item.handleNotEntailed();
		}
    }

    protected void handlUnknownEnt() {
        if (this.getSelectedValue() instanceof QueryQuestListItem) {
			QueryQuestListItem item = (QueryQuestListItem) this.getSelectedValue();
			item.handleUnknownEntailed();
		}
    }

}
