package gift.repository.role;

import gift.entity.Role;
import gift.entity.type.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, UserRole> {
    Optional<Role> findByName(UserRole name);
}
