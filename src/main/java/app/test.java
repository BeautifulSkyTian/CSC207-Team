package app;

import data_access.SearchExercise.ExternalExerciseApiGateway;
import interface_adapter.search_exercise.SearchExerciseController;
import interface_adapter.search_exercise.SearchExercisePresenter;
import interface_adapter.search_exercise.SearchExerciseViewModel;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import use_case.search_exercise.SearchExerciseDataAccessInterface;
import use_case.search_exercise.SearchExerciseInputBoundary;
import use_case.search_exercise.SearchExerciseInteractor;
import use_case.search_exercise.SearchExerciseOutputBoundary;
import view.SearchExerciseView;

public class test {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Data access
            SearchExerciseDataAccessInterface dataAccess = new ExternalExerciseApiGateway();

            // 2. ViewModel, Presenter, Interactor, Controller
            SearchExerciseViewModel viewModel = new SearchExerciseViewModel();
            SearchExerciseOutputBoundary presenter = new SearchExercisePresenter(viewModel);
            SearchExerciseInputBoundary interactor = new SearchExerciseInteractor(dataAccess, presenter);
            SearchExerciseController controller = new SearchExerciseController(interactor);

            // 3. Dropdown filter options from the API
            List<String> bodyParts = dataAccess.fetchAllBodyParts();
            List<String> targetMuscles = dataAccess.fetchAllMuscles();
            List<String> equipment = dataAccess.fetchAllEquipments();

            // 4. View inside a JFrame
            SearchExerciseView searchView = new SearchExerciseView(
                    controller, viewModel, bodyParts, targetMuscles, equipment
            );

            JFrame frame = new JFrame("Exercise Search");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(searchView);
            frame.setSize(760, 520);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}