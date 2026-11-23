package use_case.search_exercise;

import entities.FilterCriteria;
import java.util.Objects;

/**
 * Input data transferred from the Controller to the Search Exercises Interactor.
 * Wraps the domain {@link FilterCriteria} specifying the user's search and filter selections.
 */
public class SearchExerciseInputData {
    private final FilterCriteria criteria;

    /**
     * Constructs a new {@code SearchExercisesInputData} instance.
     *
     * @param criteria the filter criteria for searching exercises; must not be null
     */
    public SearchExerciseInputData(FilterCriteria criteria) {
        this.criteria = Objects.requireNonNull(criteria, "FilterCriteria must not be null");
    }

    /**
     * Returns the filter criteria for this search request.
     *
     * @return the {@link FilterCriteria}
     */
    public FilterCriteria getCriteria() {
        return criteria;
    }

}
