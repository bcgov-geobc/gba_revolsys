package com.revolsys.swing.logging;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class Log4jTabLabel extends JLabel implements MouseListener, TableModelListener {
  private static final long serialVersionUID = 1L;

  private static final Icon ANIMATED = Icons.getAnimatedIcon("error_animated.gif");

  private static final Icon STATIC = Icons.getIcon("error");

  private final JTabbedPane tabs;

  private final Log4jTableModel tableModel;

  public Log4jTabLabel(final JTabbedPane tabs, final Log4jTableModel tableModel) {
    this.tabs = tabs;
    this.tableModel = tableModel;
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 1));
    setOpaque(false);
    addMouseListener(this);
    updateLabel();
  }

  @Override
  public void addNotify() {
    super.addNotify();
    this.tableModel.addTableModelListener(this);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
      if (this.tableModel != null) {
        this.tableModel.clearHasNewErrors();
      }
      final int tabIndex = this.tabs.indexOfTabComponent(this);
      if (tabIndex != -1) {
        this.tabs.setSelectedIndex(tabIndex);
      }
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    this.tableModel.removeTableModelListener(this);
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    updateLabel();
    this.tabs.repaint();
  }

  private void updateLabel() {
    String text = null;
    final int messageCount = this.tableModel.getMessageCount();
    if (messageCount != 0) {
      text = Integer.toString(messageCount);
    }
    if (this.tableModel.isHasNewErrors()) {
      setFont(SwingUtil.BOLD_FONT);
      setForeground(WebColors.Red);
      setIcon(ANIMATED);
    } else {
      setFont(SwingUtil.FONT);
      setForeground(WebColors.Black);
      setIcon(STATIC);
    }
    setText(text);
  }
}
