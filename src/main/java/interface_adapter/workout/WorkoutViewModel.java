package interface_adapter.workout;

import interface_adapter.ViewModel;

/** View model for the workout screen. */
public class WorkoutViewModel extends ViewModel<WorkoutState> {

    public static final String VIEW_NAME = "workout";

    /** Creates a workout view model with an empty state. */
    public WorkoutViewModel() {
        super(VIEW_NAME, new WorkoutState());
    }
}
