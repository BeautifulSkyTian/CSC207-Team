package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/** Keeps a scrolling form within the viewport's width. */
final class VerticalScrollPanel extends JPanel implements Scrollable {
    VerticalScrollPanel(JPanel content) {
        super(new BorderLayout());
        add(content, BorderLayout.NORTH);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(420, 350);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
        return getFontMetrics(getFont()).getHeight();
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        int span = orientation == SwingConstants.VERTICAL ? visible.height : visible.width;
        return Math.max(1, span - getScrollableUnitIncrement(visible, orientation, direction));
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
