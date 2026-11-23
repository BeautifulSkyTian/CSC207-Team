package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Starts the app. */
public class Main {

    /**
     * Builds the app and shows its window.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        // Swing components must be created and used on the event dispatch thread.
        SwingUtilities.invokeLater(() -> {
            JFrame application = new AppBuilder()
                    .addHomeView()
                    .addLoadTemplatesUseCase()
                    .addStartWorkoutUseCase()
                    .addViewHistoryUseCase()
                    .addImportTemplateUseCase()
                    .addExportTemplateUseCase()
                    .build();
            application.setVisible(true);
        });
    }
}
