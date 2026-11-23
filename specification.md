# Specification

## Package layout

The code follows Clean Architecture. Each package belongs to one layer, and dependencies only point inward:

| Package | Layer |
|---|---|
| `entities` | Entities |
| `use_case` | Use cases (interactors, boundaries, input/output data, data access interfaces) |
| `interface_adapter` | Interface adapters (controllers, presenters, view models) |
| `view`, `data_access`, `app` | Frameworks & drivers |

Entries marked `(planned)` don't exist yet. Entries marked `(planned removal)` still exist but will be deleted.

```
src/main/java/
├── app/                                          (planned)
│   ├── Main.java                                 starts the app
│   └── AppBuilder.java                           creates and wires every class below
│
├── view/                                         (planned)
│   ├── ViewManager.java                          shows the active view's card in a CardLayout
│   └── HomeView.java                             template list, Start Workout, History, Import, Export
│
├── interface_adapter/                            (planned)
│   ├── ViewModel.java                            base class: state + PropertyChangeSupport
│   ├── ViewManagerModel.java                     name of the active view
│   ├── home/
│   │   ├── HomeViewModel.java
│   │   └── HomeState.java
│   ├── workout/
│   │   ├── WorkoutViewModel.java
│   │   └── WorkoutState.java                     the ongoing workout as plain values, no entities
│   ├── load_templates/
│   │   ├── LoadTemplatesController.java
│   │   └── LoadTemplatesPresenter.java
│   ├── start_workout/
│   │   ├── StartWorkoutController.java
│   │   └── StartWorkoutPresenter.java
│   ├── view_history/
│   │   ├── ViewHistoryController.java
│   │   └── ViewHistoryPresenter.java
│   ├── import_template/
│   │   ├── ImportTemplateController.java
│   │   └── ImportTemplatePresenter.java
│   └── export_template/
│       ├── ExportTemplateController.java
│       └── ExportTemplatePresenter.java
│
├── use_case/
│   ├── TemplateDataAccessInterface.java          saved templates; planned: readFromFile, writeToFile
│   ├── LogEntryDataAccessInterface.java          completed workouts
│   ├── load_templates/                           (planned)
│   │   ├── LoadTemplatesInputBoundary.java
│   │   ├── LoadTemplatesInteractor.java
│   │   ├── LoadTemplatesOutputBoundary.java
│   │   └── LoadTemplatesOutputData.java
│   ├── start_workout/                            (planned)
│   │   ├── StartWorkoutInputBoundary.java
│   │   ├── StartWorkoutInputData.java
│   │   ├── StartWorkoutInteractor.java
│   │   ├── StartWorkoutOutputBoundary.java
│   │   └── StartWorkoutOutputData.java
│   ├── view_history/                             (planned)
│   │   ├── ViewHistoryInputBoundary.java
│   │   ├── ViewHistoryInteractor.java
│   │   ├── ViewHistoryOutputBoundary.java
│   │   └── ViewHistoryOutputData.java
│   ├── import_template/                          (planned)
│   │   ├── ImportTemplateInputBoundary.java
│   │   ├── ImportTemplateInputData.java
│   │   ├── ImportTemplateInteractor.java
│   │   ├── ImportTemplateOutputBoundary.java
│   │   └── ImportTemplateOutputData.java
│   └── export_template/                          (planned)
│       ├── ExportTemplateInputBoundary.java
│       ├── ExportTemplateInputData.java
│       ├── ExportTemplateInteractor.java
│       ├── ExportTemplateOutputBoundary.java
│       └── ExportTemplateOutputData.java
│
├── data_access/
│   ├── JsonFiles.java                            shared file-writing helper for the JSON DAOs
│   ├── JsonTemplateDataAccessObject.java         implements TemplateDataAccessInterface
│   └── JsonLogEntryDataAccessObject.java         implements LogEntryDataAccessInterface
│
└── entities/
    ├── Exercise.java                             immutable, built with Exercise.ExerciseBuilder
    ├── ExerciseSetGroup.java
    ├── Template.java
    ├── OngoingWorkout.java                       (planned removal) the workout now lives in WorkoutState
    ├── LogEntry.java
    ├── LogExerciseSet.java
    ├── FilterCriteria.java
    └── Editor.java

src/test/java/
└── data_access/
    ├── JsonTemplateDataAccessObjectTest.java
    └── JsonLogEntryDataAccessObjectTest.java
```

Not covered yet: the exercise database (`ExerciseAccess`), the workout use cases, and the History view.
