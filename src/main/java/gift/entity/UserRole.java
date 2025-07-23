package gift.entity;

public enum UserRole {
    ROLE_GUEST("ROLE_GUEST", 0),
    ROLE_USER("ROLE_USER", 1),
    ROLE_MD("ROLE_MD", 2),
    ROLE_ADMIN("ROLE_ADMIN", 3);

    private final String roleName;
    private final int priority;

    UserRole(String roleName, int priority) {
        this.roleName = roleName;
        this.priority = priority;
    }

    public static UserRole fromString(String roleName) {
        for (UserRole role : UserRole.values()) {
            if (role.roleName.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("해당 상수가 존재하지 않습니다."
                + UserRole.class.getCanonicalName() + "." + roleName);
    }

    @Override
    public String toString() {
        return roleName;
    }

    public int getPriority() {
        return priority;
    }

}
