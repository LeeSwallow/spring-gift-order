package gift.service.product;

import gift.common.exception.AccessDeniedException;
import gift.common.mapper.ModelMapper;
import gift.common.model.CustomPage;
import gift.entity.Product;
import gift.entity.UserRole;
import gift.repository.product.ProductRepository;
import gift.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
public class ProductServiceImpl implements ProductService {
    private final static Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final UserService userService;

    public ProductServiceImpl(ProductRepository productRepository, UserService userService) {
        this.productRepository = productRepository;
        this.userService = userService;
    }

    private void validateProduct(Product product, UserRole role, Long userId) {
        if (role == UserRole.ROLE_ADMIN) {
            log.info("관리자 권한으로 상품 검증을 건너뜁니다.");
            return;
        }
        if (!product.getOwner().getId().equals(userId)) {
            log.error("상품 소유자 ID가 인증된 사용자 ID와 일치하지 않습니다. 소유자 ID: {}, 인증된 사용자 ID: {}",
                      product.getOwner(), userId);
            throw new AccessDeniedException("상품 소유자 ID가 인증된 사용자 ID와 일치하지 않습니다.");
        }
    }

    @Override
    public CustomPage<Product> findAllBy(Pageable pageable) {
        return ModelMapper.toCustomPage(productRepository.findAllBy(pageable));
    }

    @Override
    public Product findById(Long productId) {
        if (productId == null) {
            log.error("상품 ID가 null 입니다.");
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
        Optional<Product> product = productRepository.findById(productId);

        return product.orElseThrow(() ->
        {
            log.error("Id {}에 해당하는 상품이 존재하지 않습니다.", productId);
            return new NoSuchElementException(
                    String.format("Id %d에 해당하는 상품이 존재하지 않습니다.", productId)
            );
        });
    }

    @Override
    public Product create(Product product, UserRole role, Long userId) {
        product.setOwner(userService.getReference(userId));
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(Product product, UserRole role, Long userId) {
        Product updated = findById(product.getId());
        validateProduct(updated, role, userId);
        if (product.getName() != null) {
            updated.setName(product.getName());
        }

        if (product.getPrice() != null) {
            updated.setPrice(product.getPrice());
        }

        if (product.getImageUrl() != null) {
            updated.setImageUrl(product.getImageUrl());
        }

        return productRepository.save(updated);
    }

    @Override
    @Transactional
    public void deleteById(Long productId, UserRole role, Long userId) {
        Product deleted = findById(productId);
        validateProduct(deleted, role, userId);
        productRepository.deleteById(productId);
    }

    @Override
    public Boolean existsById(Long productId) {
        return productRepository.existsById(productId);
    }

    @Override
    public Product getReference(Long productId) {
        return productRepository.getReferenceById(productId);
    }
}
