package at.ainf.protegeview.views;

import at.ainf.protegeview.debugmanager.ConflictSetSelectedEvent;
import at.ainf.protegeview.debugmanager.ConflictSetSelectedListener;
import at.ainf.protegeview.debugmanager.DebugManager;
import org.protege.editor.core.ui.list.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 22.08.11
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */
public class BaseResultsList extends JList implements ConflictSetSelectedListener {
    private MListCellRenderer ren;
	private MListDeleteButton deleteButton;
	private MListEditButton editButton;
	private MListAddButton addButton;
	private static final Stroke BUTTON_STROKE = new BasicStroke(2.0f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private boolean mouseDown;
	private static final int BUTTON_DIMENSION = 16;
	private static final int BUTTON_MARGIN = 2;
	private static Font SECTION_HEADER_FONT = new Font("Lucida Grande",
			Font.PLAIN, 12);
	private static final Color itemBackgroundColor = new Color(240, 245, 240);
	private java.util.List<MListButton> editAndDeleteButtons;
	private java.util.List<MListButton> deleteButtonOnly;
	private ActionListener deleteActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			BaseResultsList.this.handleDelete();
		}
	};
	private ActionListener addActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			BaseResultsList.this.handleAdd();
		}
	};
	private ActionListener editActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			BaseResultsList.this.handleEdit();
		}
	};
	private MouseMotionListener mouseMovementListener = new MouseMotionAdapter() {
		public int lastIndex = 0;

		@Override
		public void mouseMoved(MouseEvent e) {
			if (BaseResultsList.this.getModel().getSize() > 0) {
				Point pt = BaseResultsList.this.getMousePosition();
				// more efficient than repainting the whole component every time
				// the mouse moves
				if (pt != null) {
					int index = BaseResultsList.this.locationToIndex(pt);
					// only repaint all the cells the mouse has moved over
					BaseResultsList.this.repaint(BaseResultsList.this.getCellBounds(Math.min(index,
							this.lastIndex), Math.max(index, this.lastIndex)));
					this.lastIndex = index;
				} else {
					BaseResultsList.this.repaint();
					this.lastIndex = 0;
				}
			}
		}
	};
	private MouseListener mouseButtonListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			BaseResultsList.this.mouseDown = true;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			BaseResultsList.this.handleMouseClick(e);
			BaseResultsList.this.mouseDown = false;
		}

		@Override
		public void mouseExited(MouseEvent event) {
			// leave the component cleanly
			BaseResultsList.this.repaint();
		}
	};

	public BaseResultsList() {
		ListCellRenderer renderer = this.getCellRenderer();
		this.ren = new MListCellRenderer();
		this.ren.setContentRenderer(renderer);
		super.setCellRenderer(this.ren);
		this.deleteButton = new MListDeleteButton(this.deleteActionListener) {
			@Override
			public String getName() {
				String name = "<html><body>" + super.getName();
				String rowName = BaseResultsList.this.getRowName(this.getRowObject());
				if (rowName != null) {
					name += " " + rowName.toLowerCase();
				}
				return name + "</body></html>";
			}
		};
		this.addButton = new MListAddButton(this.addActionListener);
		this.editButton = new MListEditButton(this.editActionListener);
		this.addMouseMotionListener(this.mouseMovementListener);
		this.addMouseListener(this.mouseButtonListener);
		this.setFixedCellHeight(-1);
		this.deleteButtonOnly = new ArrayList<MListButton>();
		this.deleteButtonOnly.add(this.deleteButton);
		this.editAndDeleteButtons = new ArrayList<MListButton>();
		this.editAndDeleteButtons.add(this.editButton);
		this.editAndDeleteButtons.add(this.deleteButton);
        DebugManager.getInstance().addConflictSetSelectedListener(this);

	}

	protected String getRowName(Object rowObject) {
		return null;
	}

	@Override
	public void setCellRenderer(ListCellRenderer cellRenderer) {
		if (this.ren == null) {
			super.setCellRenderer(cellRenderer);
		} else {
			this.ren.setContentRenderer(cellRenderer);
		}
	}

	protected void handleAdd() {
		if (this.getSelectedValue() instanceof MListItem) {
			MListItem item = (MListItem) this.getSelectedValue();
			item.handleEdit();
		}
	}

	protected void handleDelete() {
		if (this.getSelectedValue() instanceof MListItem) {
			MListItem item = (MListItem) this.getSelectedValue();
			item.handleDelete();
		}
	}

	protected void handleEdit() {
		if (this.getSelectedValue() instanceof MListItem) {
			MListItem item = (MListItem) this.getSelectedValue();
			item.handleEdit();
		}
	}

	private void handleMouseClick(MouseEvent e) {
		for (MListButton button : this.getButtons(this.locationToIndex(e
				.getPoint()))) {
			if (button.getBounds().contains(e.getPoint())) {
				button.getActionListener().actionPerformed(
						new ActionEvent(button, ActionEvent.ACTION_PERFORMED,
								button.getName()));
				return;
			}
		}
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	protected java.util.List<MListButton> getButtons(Object value) {
		if (value instanceof MListSectionHeader) {
			return this.getSectionButtons((MListSectionHeader) value);
		} else if (value instanceof MListItem) {
			return this.getListItemButtons((MListItem) value);
		} else {
			return Collections.emptyList();
		}
	}

	protected java.util.List<MListButton> getSectionButtons(MListSectionHeader header) {
		java.util.List<MListButton> buttons = new ArrayList<MListButton>();
		if (header.canAdd()) {
			buttons.add(this.addButton);
		}
		return buttons;
	}

	protected java.util.List<MListButton> getListItemButtons(MListItem item) {
		if (item.isDeleteable()) {
			if (item.isEditable()) {
				return this.editAndDeleteButtons;
			} else {
				return this.deleteButtonOnly;
			}
		}
		return Collections.emptyList();
	}

	protected Color getItemBackgroundColor(MListItem item) {
		return itemBackgroundColor;
	}

    boolean conflictSetLst = false;

    public boolean isConflictSetLst() {
        return conflictSetLst;
    }

    public void setConflictSetLst(boolean conflictSetLst) {
        this.conflictSetLst = conflictSetLst;
    }

    private int number = -1;

    public void conflictSetSelected(ConflictSetSelectedEvent e) {
        number = e.getConflictSetNumber();
    }

    public class MListCellRenderer implements ListCellRenderer {
		private ListCellRenderer contentRenderer;
		private DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			// We now modify the component so that it has a nice border and
			// background
			if (value instanceof MListSectionHeader) {
				JLabel label = (JLabel) this.defaultListCellRenderer
						.getListCellRendererComponent(list, " ", index,
								isSelected, cellHasFocus);
				label.setBorder(BorderFactory.createCompoundBorder(BaseResultsList.this
						.createPaddingBorder(list, " ", index, isSelected,
								cellHasFocus), BorderFactory.createEmptyBorder(
						2, 2, 2, 2)));
				return label;
			}
			JComponent component = (JComponent) this.contentRenderer
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);
			component.setOpaque(true);
			if (value instanceof MListItem) {
				Border border = BorderFactory.createCompoundBorder(BaseResultsList.this
						.createPaddingBorder(list, value, index, isSelected,
								cellHasFocus), BaseResultsList.this.createListItemBorder(
						list, value, index, isSelected, cellHasFocus));
				int buttonSpan = BaseResultsList.this.getButtons(value).size()
						* (BUTTON_DIMENSION + 2) + BUTTON_MARGIN * 2;
				border = BorderFactory.createCompoundBorder(border,
						BorderFactory.createEmptyBorder(1, 1, 1, buttonSpan));
				component.setBorder(border);
				if (!isSelected) {
					component.setBackground(BaseResultsList.this
							.getItemBackgroundColor((MListItem) value));
				}
			}
			if (isSelected) {
				component.setBackground(list.getSelectionBackground());
			}
			return component;
		}

		public void setContentRenderer(ListCellRenderer renderer) {
			this.contentRenderer = renderer;
		}
	}

	protected Border createPaddingBorder(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		int bottomMargin = 1;
		if (list.getFixedCellHeight() == -1) {
			if (this.getModel().getSize() > index + 1) {
				if (this.getModel().getElementAt(index + 1) instanceof MListSectionHeader) {
					bottomMargin = 20;
				}
			}
		}
		return BorderFactory.createMatteBorder(1, 1, bottomMargin, 1,
				Color.WHITE);
	}

	protected Border createListItemBorder(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		return BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
	}

	private java.util.List<MListButton> getButtons(int index) {
		if (index < 0) {
			return Collections.emptyList();
		}
		Object obj = this.getModel().getElementAt(index);
		java.util.List<MListButton> buttons = this.getButtons(obj);
		Rectangle rowBounds = this.getCellBounds(index, index);
		if (obj instanceof MListSectionHeader) {
			MListSectionHeader section = (MListSectionHeader) obj;
			Rectangle nameBounds = this.getGraphics().getFontMetrics(
					SECTION_HEADER_FONT).getStringBounds(section.getName(),
					this.getGraphics()).getBounds();
			int x = 7 + nameBounds.width + 2;
			for (MListButton button : buttons) {
				button.setBounds(new Rectangle(x, rowBounds.y + 2,
						BUTTON_DIMENSION, BUTTON_DIMENSION));
				x += BUTTON_DIMENSION;
				x += 2;
				button.setRowObject(obj);
			}
		} else if (obj instanceof MListItem) {
			int x = rowBounds.width - 2;
			for (MListButton button : buttons) {
				x -= BUTTON_DIMENSION;
				x -= 2;
				button.setBounds(new Rectangle(x, rowBounds.y + 2,
						BUTTON_DIMENSION, BUTTON_DIMENSION));
				button.setRowObject(obj);
			}
		}
		return buttons;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		Point mousePos = this.getMousePosition();
		if (mousePos == null) {
			return null;
		}
		for (MListButton button : this.getButtons(this
				.locationToIndex(mousePos))) {
			if (button.getBounds().contains(mousePos)) {
				return button.getName();
			}
		}
		int index = this.locationToIndex(event.getPoint());
		if (index == -1) {
			return null;
		}
		Object val = this.getModel().getElementAt(index);
		if (val instanceof MListItem) {
			return ((MListItem) val).getTooltip();
		}
		return null;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Color oldColor = g.getColor();
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// Paint buttons
		Stroke oldStroke = g2.getStroke();
		Rectangle clipBound = g.getClipBounds();
		boolean paintedSomeRows = false;
		boolean useQuartz = Boolean.getBoolean(System
				.getProperty("-Dapple.awt.graphics.UseQuartz"));
		for (int index = 0; index < this.getModel().getSize(); index++) {
			Rectangle rowBounds = this.getCellBounds(index, index);
			if (!rowBounds.intersects(clipBound)) {
				if (paintedSomeRows) {
					break;
				}
				continue;
			}
			paintedSomeRows = true;
			java.util.List<MListButton> buttons = this.getButtons(index);
			int endOfButtonRun = -1;
			for (MListButton button : buttons) {
				Rectangle buttonBounds = button.getBounds();
				if (buttonBounds.intersects(clipBound)) {
					g2.setColor(this.getButtonColor(button));
					if (!useQuartz) {
						g2
								.fillOval(buttonBounds.x, buttonBounds.y,
										buttonBounds.width + 1,
										buttonBounds.height + 1);
					} else {
						g2.fillOval(buttonBounds.x, buttonBounds.y,
								buttonBounds.width, buttonBounds.height);
					}
					g2.setColor(Color.WHITE);
					Stroke curStroke = g2.getStroke();
					g2.setStroke(BUTTON_STROKE);
					button.paintButtonContent(g2);
					g2.setStroke(curStroke);
					// g2.translate(-buttonBounds.x, -buttonBounds.y);
				}
				endOfButtonRun = buttonBounds.x + buttonBounds.width
						+ BUTTON_MARGIN;
			}
			if (this.getModel().getElementAt(index) instanceof MListSectionHeader) {
				MListSectionHeader header = (MListSectionHeader) this
						.getModel().getElementAt(index);
				if (this.isSelectedIndex(index)) {
					g2.setColor(this.getSelectionForeground());
				} else {
					g2.setColor(Color.GRAY);
				}
				int indent = 4;
                if (header instanceof ResultsListSection && ((ResultsListSection)header).getNum() == number &&
                                                               conflictSetLst) {
                    Color old = g2.getColor();
                    g2.setColor (new Color(173,255,47));
                    g2.fillRect(rowBounds.x,rowBounds.y,rowBounds.width,rowBounds.height);
                    g2.setColor(old );
                }
				int baseLine = rowBounds.y
						+ (BUTTON_DIMENSION + BUTTON_MARGIN - g
								.getFontMetrics().getHeight()) / 2
						+ g.getFontMetrics().getAscent();
				Font oldFont = g2.getFont();
				g2.setFont(SECTION_HEADER_FONT);
				g2.drawString(header.getName(), 1 + indent, baseLine);
				g2.setFont(oldFont);
				if (endOfButtonRun == -1) {
					endOfButtonRun = g2.getFontMetrics(SECTION_HEADER_FONT)
							.getStringBounds(header.getName(), g2).getBounds().width
							+ BUTTON_MARGIN * 2;
				}
			}
		}
		g.setColor(oldColor);
		g2.setStroke(oldStroke);
	}

	private Color getButtonColor(MListButton button) {
		Point pt = this.getMousePosition();
		if (pt == null) {
			return button.getBackground();
		}
		if (button.getBounds().contains(pt)) {
			if (this.mouseDown) {
				return Color.DARK_GRAY;
			} else {
				return button.getRollOverColor();
			}
		}
		return button.getBackground();
	}



}
