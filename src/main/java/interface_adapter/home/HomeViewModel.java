package interface_adapter.home;

import interface_adapter.ViewModel;

/** View model for the home screen. */
public class HomeViewModel extends ViewModel<HomeState> {

    public static final String VIEW_NAME = "home";

    public static final String TITLE_LABEL = "Templates";
    public static final String START_WORKOUT_BUTTON_LABEL = "Start Workout";
    public static final String HISTORY_BUTTON_LABEL = "History";
    public static final String IMPORT_BUTTON_LABEL = "Import";
    public static final String EXPORT_BUTTON_LABEL = "Export";

    /** Creates a home view model with an empty state. */
    public HomeViewModel() {
        super(VIEW_NAME, new HomeState());
    }
}
