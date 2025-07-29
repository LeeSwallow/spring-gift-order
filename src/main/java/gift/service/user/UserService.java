package gift.service.user;

import gift.entity.User;
import gift.common.model.CustomPage;
import gift.entity.type.Provider;
import org.springframework.data.domain.Pageable;


public interface UserService {
    CustomPage<User> findAllBy(Pageable pageable);
    User findById(Long userId);
    User findByEmail(String email);
    User findByClientIdAndProvider(String clientId, Provider provider);
    User create(User user);
    User update(User user);
    void deleteById(Long userId);
    Boolean existsById(Long userId);
    Boolean existsByClientIdAndProvider(String clientId, Provider provider);
    User getReference(Long userId);
}
