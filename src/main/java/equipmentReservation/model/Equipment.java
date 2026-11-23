package equipmentReservation.model;

/**
 * A shared equipment item such as a laptop or camera (FR-03). Its id is
 * unique and never changes (FR-21).
 */
public class Equipment extends AbstractModel {

    /** Unique id; frozen after creation (FR-21). */
    private final String id;

    /** Display name, used in catalog search (FR-04). */
    private final String name;

    /** Category, used in catalog filtering (FR-05). */
    private final String category;

    /** Free-text description; may be empty. */
    private final String description;

    /** Physical condition. */
    private final Condition condition;

    /** Whether the item can be used. */
    private final OperationalStatus operationalStatus;

    /**
     * Creates an equipment item.
     *
     * @param id          unique id; not blank
     * @param name        display name; not blank
     * @param category    category; not blank
     * @param description description; may be empty but not null
     * @param condition   physical condition; not null
     * @param status      operational status; not null
     * @throws ValidationException if a required field is null or blank (FR-21)
     */
    public Equipment(String id, String name, String category, String description,
                     Condition condition, OperationalStatus status) throws ValidationException {
        this.id = Checks.requireText(id, "Equipment ID");
        this.name = Checks.requireText(name, "Name");
        this.category = Checks.requireText(category, "Category");
        this.description = Checks.requireValue(description, "Description");
        this.condition = Checks.requireValue(condition, "Condition");
        this.operationalStatus = Checks.requireValue(status, "Operational status");
    }

    /**
     * Returns the unique id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the display name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the description.
     *
     * @return the description, possibly empty
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the physical condition.
     *
     * @return the condition
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Returns the operational status.
     *
     * @return the operational status
     */
    public OperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    /**
     * Tells whether the item can be used (FR-09).
     *
     * @return {@code true} if the status is {@link OperationalStatus#OPERATIONAL}
     */
    public boolean isOperational() {
        return operationalStatus == OperationalStatus.OPERATIONAL;
    }
}
