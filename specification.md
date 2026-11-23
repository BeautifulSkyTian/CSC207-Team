# Specification

## Package layout

The code follows Clean Architecture. Each package belongs to one layer, and dependencies only point inward:

| Package | Layer |
|---|---|
| `entities` | Entities |
| `use_case` | Use cases (interactors, boundaries, input/output data, data access interfaces) |
| `interface_adapter` | Interface adapters (controllers, presenters, view models) |
| `view`, `data_access`, `app` | Frameworks & drivers |

Entries marked `(stub)` exist but have no behaviour yet: an empty class, or methods whose bodies are still `TODO`. A folder marked `(stub)` means every file in it is a stub.

```
src/main/java/
├── app/
│   ├── Main.java                                 starts the app
│   └── AppBuilder.java                           creates and wires every class below
│
├── view/
│   ├── ViewManager.java                          shows the active view's card in a CardLayout
│   ├── HomeView.java                             template list, Start Workout, History, Import, Export
│   └── CreateTemplateView.java                   (stub) new template: name, exercises and their sets
│
├── interface_adapter/
│   ├── ViewModel.java                            base class: state + PropertyChangeSupport
│   ├── ViewManagerModel.java                     name of the active view
│   ├── home/
│   │   ├── HomeViewModel.java
│   │   └── HomeState.java
│   ├── workout/
│   │   ├── WorkoutViewModel.java
│   │   └── WorkoutState.java                     (stub) the ongoing workout as plain values, no entities
│   ├── load_templates/                           (stub)
│   │   ├── LoadTemplatesController.java
│   │   └── LoadTemplatesPresenter.java
│   ├── start_workout/                            (stub)
│   │   ├── StartWorkoutController.java
│   │   └── StartWorkoutPresenter.java
│   ├── view_history/                             (stub)
│   │   ├── ViewHistoryController.java
│   │   └── ViewHistoryPresenter.java
│   ├── import_template/                          (stub)
│   │   ├── ImportTemplateController.java
│   │   └── ImportTemplatePresenter.java
│   └── export_template/                          (stub)
│       ├── ExportTemplateController.java
│       └── ExportTemplatePresenter.java
│
├── use_case/
│   ├── TemplateDataAccessInterface.java          saved templates; planned: readFromFile, writeToFile
│   ├── LogEntryDataAccessInterface.java          completed workouts
│   ├── load_templates/                           (stub)
│   │   ├── LoadTemplatesInputBoundary.java
│   │   ├── LoadTemplatesInteractor.java
│   │   ├── LoadTemplatesOutputBoundary.java
│   │   └── LoadTemplatesOutputData.java
│   ├── start_workout/                            (stub)
│   │   ├── StartWorkoutInputBoundary.java
│   │   ├── StartWorkoutInputData.java
│   │   ├── StartWorkoutInteractor.java
│   │   ├── StartWorkoutOutputBoundary.java
│   │   └── StartWorkoutOutputData.java
│   ├── view_history/                             (stub)
│   │   ├── ViewHistoryInputBoundary.java
│   │   ├── ViewHistoryInteractor.java
│   │   ├── ViewHistoryOutputBoundary.java
│   │   └── ViewHistoryOutputData.java
│   ├── import_template/                          (stub)
│   │   ├── ImportTemplateInputBoundary.java
│   │   ├── ImportTemplateInputData.java
│   │   ├── ImportTemplateInteractor.java
│   │   ├── ImportTemplateOutputBoundary.java
│   │   └── ImportTemplateOutputData.java
│   └── export_template/                          (stub)
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
