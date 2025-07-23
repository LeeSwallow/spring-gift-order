package gift.service.wishlist;

import gift.common.model.CustomPage;
import gift.entity.WishedProduct;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface WishedProductService {
    CustomPage<WishedProduct> findAllBy(Long userId, Pageable pageable);
    WishedProduct findBy(Long userId, Long wishedProductId);
    WishedProduct create(Long userId, Long productId, Integer quantity);
    void deleteBy(Long userId, Long wishedProductId);
    void deleteAll(Long userId);
    Optional<WishedProduct> updateQuantityBy(Long userId, Long wishedProductId, Integer quantity);
    Optional<WishedProduct> changeQuantityBy(Long userId, Long wishedProductId, Integer amount);
}
