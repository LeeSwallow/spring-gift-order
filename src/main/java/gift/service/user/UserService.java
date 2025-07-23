package gift.service.user;

import gift.entity.User;
import gift.common.model.CustomPage;
import org.springframework.data.domain.Pageable;


public interface UserService {
    CustomPage<User> findAllBy(Pageable pageable);
    User findById(Long userId);
    User findByEmail(String email);
    User create(User user);
    User update(User user);
    void deleteById(Long userId);
    Boolean existsById(Long userId);
    User getReference(Long userId);
}
