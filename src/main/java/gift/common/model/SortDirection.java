package gift.common.model;

public enum SortDirection {
    ASC("asc"), DESC("desc");

    private final String value;
    SortDirection(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return value;
    }

    public static boolean contains(String value) {
        for (SortDirection direction : SortDirection.values()) {
            if (direction.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
