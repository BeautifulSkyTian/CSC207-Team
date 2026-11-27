package view;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Runs the same action for every change to a text field. */
interface TextChangeListener extends DocumentListener {
    void textChanged();

    @Override
    default void insertUpdate(DocumentEvent event) {
        textChanged();
    }

    @Override
    default void removeUpdate(DocumentEvent event) {
        textChanged();
    }

    @Override
    default void changedUpdate(DocumentEvent event) {
        textChanged();
    }
}
