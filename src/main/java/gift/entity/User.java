package gift.entity;

import gift.entity.type.UserRole;
import gift.entity.type.Provider;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"client_id", "provider"})}
)
@NamedEntityGraph(
        name = "User.withRole",
        attributeNodes = { @NamedAttributeNode("roles") }
)

public class User extends BaseEntity {

    @Column(nullable = true, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name= "provider", nullable = false)
    private Provider provider;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    private Set<Role> roles;

    protected User() {

    }

    public User(String email, String password) {
        this(null, email, password, null);
    }

    public User(String email, String password, Set<UserRole> roles) {
        this(null, email, password, roles);
    }

    public User(Long id, String email, String password, Set<UserRole> roles) {
        this(id, email, password, null, Provider.EMAIL, roles);
    }

    public User(String clientId, Provider provider) {
        this(null, null, null, clientId, provider, null);
    }

    public User(String clientId, Provider provider, Set<UserRole> roles) {
        this(null, null, null, clientId, provider, roles);
    }

    public User(Long id, String clientId, Provider provider, Set<UserRole> roles) {
        this(id, null, null, clientId, provider, null);
    }

    public User(Long id, String email, String password, String clientId, Provider provider, Set<UserRole> roles) {
        super(id);
        this.email = email;
        this.password = password;
        this.clientId = clientId;
        this.provider = provider;
        setRoles(roles);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Set<UserRole> getUserRoles() {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    public void setRoles(Set<UserRole> roles) {
        if (roles == null) {
            this.roles = null;
            return;
        }
        this.roles = roles.stream()
                .map(Role::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", updatedAt=" + getUpdatedAt() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), getId());
    }
}