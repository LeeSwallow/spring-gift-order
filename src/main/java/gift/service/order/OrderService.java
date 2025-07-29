package gift.service.order;

import gift.common.model.CustomAuth;
import gift.common.model.CustomPage;
import gift.entity.Order;
import gift.entity.type.UserRole;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    CustomPage<Order> findAllBy(Long userId, Pageable pageable);
    Order findById(Long id);
    Order findBy(Long id, UserRole role, Long userId);
    Order create(Integer quantity, String message, Long optionId, Long userId);
    Order createWithNotification(Integer quantity, String message, Long optionId, CustomAuth auth, String accessToken);
    Order update(Long id, Integer quantity, Long totalPrice, String message, UserRole role, Long userId);
    void deleteById(Long id);
    Order getReference(Long id);
}
