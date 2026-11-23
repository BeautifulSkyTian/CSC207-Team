package interface_adapter;

/** Holds the name of the active view. */
public class ViewManagerModel extends ViewModel<String> {

    /** Creates a view manager model with no active view. */
    public ViewManagerModel() {
        super("view manager", "");
    }
}
