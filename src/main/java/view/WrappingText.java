package view;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JTextArea;

/** Read-only text styled like a label with a height that adapts to the available width. */
final class WrappingText extends JTextArea {
    WrappingText(String text) {
        super(text, 1, 1);
        setEditable(false);
        setFocusable(false);
        setOpaque(false);
        setFont(new JLabel().getFont());
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        // The parent sets the width. A long name must not widen the window at pack time.
        size.width = getColumnWidth();
        return size;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        int oldWidth = getWidth();
        super.setBounds(x, y, width, height);
        if (width != oldWidth) {
            revalidate();
        }
    }
}
