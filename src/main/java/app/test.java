package app;

import entities.Exercise;
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
            // 1. Data access (swap in CachedExerciseRepository / ExternalExerciseApiGateway once implemented)
            SearchExerciseDataAccessInterface dataAccess = createStubDataAccess();

            // 2. ViewModel, Presenter, Interactor, Controller
            SearchExerciseViewModel viewModel = new SearchExerciseViewModel();
            SearchExerciseOutputBoundary presenter = new SearchExercisePresenter(viewModel);
            SearchExerciseInputBoundary interactor = new SearchExerciseInteractor(dataAccess, presenter);
            SearchExerciseController controller = new SearchExerciseController(interactor);

            // 3. Dropdown filter options
            List<String> bodyParts = List.of("Chest", "Back", "Legs", "Shoulders", "Arms");
            List<String> targetMuscles = List.of("Pectorals", "Lats", "Quadriceps", "Hamstrings", "Deltoids");
            List<String> equipment = List.of("Barbell", "Dumbbell", "Cable", "Bodyweight");

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

    // Temporary stub so you can run and test the UI right away
    private static SearchExerciseDataAccessInterface createStubDataAccess() {
        Exercise benchPress = new Exercise.ExerciseBuilder()
                .id("ex-1")
                .name("Barbell Bench Press")
                .bodyParts(List.of("Chest"))
                .targetMuscles(List.of("Pectorals"))
                .equipments(List.of("Barbell"))
                .videoUrl("https://example.com/bench-press")
                .instructions(List.of(
                        "Lie flat on a bench with eyes under the bar.",
                        "Grip slightly wider than shoulder-width and unrack.",
                        "Lower the bar under control to mid-chest, then press up."
                ))
                .build();

        Exercise pushUp = new Exercise.ExerciseBuilder()
                .id("ex-2")
                .name("Push-Up")
                .bodyParts(List.of("Chest"))
                .targetMuscles(List.of("Pectorals", "Triceps"))
                .equipments(List.of("Bodyweight"))
                .videoUrl("https://example.com/push-up")
                .instructions(List.of(
                        "Start in a high plank with hands under shoulders.",
                        "Lower your body until chest is just above the floor.",
                        "Push through your palms back to the starting position."
                ))
                .build();

        List<Exercise> sampleExercises = List.of(benchPress, pushUp);

        return new SearchExerciseDataAccessInterface() {
            @Override
            public List<Exercise> fetchAllExercises() {
                return sampleExercises;
            }

            @Override
            public Exercise getExerciseById(String id) {
                return sampleExercises.stream()
                        .filter(ex -> ex.getId().equals(id))
                        .findFirst()
                        .orElse(null);
            }
        };
    }
}