package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Base class for view models: holds a state and notifies listeners when it changes.
 *
 * @param <T> the type of state this view model holds
 */
public abstract class ViewModel<T> {

    /** Property name fired when the whole state changes. */
    public static final String STATE_PROPERTY = "state";

    private final String viewName;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private T state;

    /**
     * Creates a view model.
     *
     * @param viewName the name of the view this model belongs to
     * @param initialState the starting state
     */
    protected ViewModel(String viewName, T initialState) {
        this.viewName = viewName;
        this.state = initialState;
    }

    /** @return the name of the view this model belongs to */
    public String getViewName() {
        return viewName;
    }

    /** @return the current state */
    public T getState() {
        return state;
    }

    /**
     * Replaces the state. Listeners are not notified until a firePropertyChange method is called.
     *
     * @param state the new state
     */
    public void setState(T state) {
        this.state = state;
    }

    /** Tells listeners that the state has changed. */
    public void firePropertyChange() {
        firePropertyChange(STATE_PROPERTY);
    }

    /**
     * Tells listeners that the given property has changed. The new value is the current state.
     *
     * @param propertyName the name of the property that changed
     */
    public void firePropertyChange(String propertyName) {
        // Old value is null so the event always fires, even when the same state object was mutated.
        support.firePropertyChange(propertyName, null, state);
    }

    /**
     * Registers a listener for state changes.
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
