package gift.service.wishlist;

import gift.common.mapper.ModelMapper;
import gift.common.model.CustomPage;
import gift.entity.WishedProduct;
import gift.repository.wishlist.WishedProductRepository;
import gift.service.product.ProductService;
import gift.service.user.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class WishedProductServiceImpl implements WishedProductService {
    private final WishedProductRepository wishedProductRepository;
    private final ProductService productService;
    private final UserService userService;

    public WishedProductServiceImpl(
            WishedProductRepository wishedProductRepository,
            ProductService productService,
            UserService userService
    ) {
        this.wishedProductRepository = wishedProductRepository;
        this.productService = productService;
        this.userService = userService;
    }

    private void validateUserId(Long userId) {
        if (!userService.existsById(userId)) {
            throw new NoSuchElementException("존재하지 않는 사용자입니다. userId: " + userId);
        }
    }

    public void validateProductId(Long productId) {
        if (!productService.existsById(productId)) {
            throw new NoSuchElementException("존재하지 않는 제품입니다. productId: " + productId);
        }
    }

    private CustomPage<WishedProduct> addExtrasAndReturn(CustomPage<WishedProduct> customPage, Long userId) {
        var stats = wishedProductRepository.calculateStatsByUserId(userId);
        customPage.setExtras(
                Map.of("totalQuantity", stats.getTotalQuantity(), "totalPrice", stats.getTotalPrice())
        );
        return customPage;
    }

    @Override
    @Transactional
    public CustomPage<WishedProduct> findAllBy(Long userId, Pageable pageable) {
        validateUserId(userId);
        var pagedProducts = wishedProductRepository.findAllByUserId(userId, pageable);
        var customPage = ModelMapper.toCustomPage(pagedProducts);
        return addExtrasAndReturn(customPage, userId);
    }


    @Override
    @Transactional
    public WishedProduct findBy(Long userId, Long wishedProductId) {
        validateUserId(userId);
        var wishedProduct = wishedProductRepository.findById(wishedProductId)
                .orElseThrow(() -> new NoSuchElementException("장바구니에 해당 제품이 없습니다. wishedProductId: " + wishedProductId));

        if (!wishedProduct.getUser().getId().equals(userId)) {
            throw new NoSuchElementException("장바구니에 해당 제품이 없습니다. wishedProductId: " + wishedProductId);
        }
        return wishedProduct;
    }

    @Override
    @Transactional
    public WishedProduct create(Long userId, Long productId, Integer quantity) {
        validateUserId(userId);
        validateProductId(productId);
        if (wishedProductRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new DuplicateKeyException("이미 장바구니에 존재하는 제품입니다. productId: " + productId);
        }
        var productRef = productService.getReference(productId);
        var userRef = userService.getReference(userId);
        return wishedProductRepository.save(new WishedProduct(null, userRef, productRef, quantity));
    }

    @Override
    @Transactional
    public void deleteBy(Long userId, Long wishedProductId) {
        findBy(userId, wishedProductId); // 검증을 위해 호출
        wishedProductRepository.deleteById(wishedProductId);
    }

    @Override
    @Transactional
    public void deleteAll(Long userId) {
        validateUserId(userId);
        wishedProductRepository.deleteAllByUserId(userId);
    }

    @Override
    @Transactional
    public Optional<WishedProduct> updateQuantityBy(Long userId, Long wishedProductId, Integer quantity) {
        var existingProduct = findBy(userId, wishedProductId);

        if (quantity == null || quantity <= 0) {
            wishedProductRepository.deleteById(wishedProductId);
            return Optional.empty();
        }
        existingProduct.setQuantity(quantity);
        return Optional.of(wishedProductRepository.save(existingProduct));
    }

    @Override
    @Transactional
    public Optional<WishedProduct> changeQuantityBy(Long userId, Long wishedProductId, Integer amount) {
        var wishedProduct = findBy(userId, wishedProductId);
        if (wishedProduct.getQuantity() + amount <= 0) {
            wishedProductRepository.deleteById(wishedProductId);
            return Optional.empty();
        }
        wishedProduct.setQuantity(wishedProduct.getQuantity() + amount);
        return Optional.of(wishedProductRepository.save(wishedProduct));
    }
}
