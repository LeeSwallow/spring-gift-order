package gift.repository.user;

import gift.entity.User;
import gift.entity.type.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph("User.withRole")
    Page<User> findAllBy(Pageable pageable);

    @EntityGraph("User.withRole")
    Optional<User> findById(Long id);

    @EntityGraph("User.withRole")
    Optional<User> findByClientIdAndProvider(String clientId, Provider provider);

    @EntityGraph("User.withRole")
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
    Boolean existsByClientIdAndProvider(String clientId, Provider provider);
}
