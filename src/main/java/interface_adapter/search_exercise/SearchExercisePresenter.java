package interface_adapter.search_exercise;

import entities.Exercise;
import java.util.Objects;
import use_case.search_exercise.SearchExerciseOutputBoundary;
import use_case.search_exercise.SearchExerciseOutputData;

/**
 * Presenter for the Search Exercises use case.
 * Formats use case outputs into {@link SearchExerciseViewModel} state updates and triggers
 * property change notifications for observing views.
 */
public class SearchExercisePresenter implements SearchExerciseOutputBoundary {
  private final SearchExerciseViewModel viewModel;

  /**
   * Constructs a new {@code SearchExercisePresenter}.
   *
   * @param viewModel the view model observed by the UI; must not be null
   */
  public SearchExercisePresenter(SearchExerciseViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel, "ViewModel must not be null");
  }

  @Override
  public void prepareSuccessView(SearchExerciseOutputData outputData) {
    viewModel.setErrorMessage(null);
    viewModel.setDisplayedExercises(outputData.getMatchingExercises());
    viewModel.firePropertyChanged("displayedExercises");
  }

  @Override
  public void prepareDetailView(Exercise exercise) {
    viewModel.setErrorMessage(null);
    viewModel.setSelectedExercise(exercise);
    viewModel.firePropertyChanged("selectedExercise");
  }

  @Override
  public void prepareFailView(String errorMessage) {
    viewModel.setErrorMessage(errorMessage);
    viewModel.firePropertyChanged("errorMessage");
  }
}