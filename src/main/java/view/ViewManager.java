package view;

import interface_adapter.ViewManagerModel;

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

/** Shows the active view's card in a CardLayout, as named by the ViewManagerModel. */
public class ViewManager implements PropertyChangeListener {

    private final JPanel views;
    private final CardLayout cardLayout;

    /**
     * Creates a view manager and starts listening to the view manager model.
     *
     * @param views the panel holding one card per view, added under each view's name
     * @param cardLayout the layout of views
     * @param viewManagerModel the model naming the active view
     */
    public ViewManager(JPanel views, CardLayout cardLayout, ViewManagerModel viewManagerModel) {
        this.views = views;
        this.cardLayout = cardLayout;
        viewManagerModel.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ViewManagerModel.STATE_PROPERTY.equals(evt.getPropertyName())) {
            String viewName = (String) evt.getNewValue();
            cardLayout.show(views, viewName);
        }
    }
}
