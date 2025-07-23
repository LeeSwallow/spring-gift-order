package gift.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @Enumerated(EnumType.STRING)
    private UserRole name;

    public Role() {
    }

    public Role(UserRole userRole) {
        this.name = userRole;
    }

    public void setName(UserRole name) {
        this.name = name;
    }
    public UserRole getName() {
        return name;
    }
}
