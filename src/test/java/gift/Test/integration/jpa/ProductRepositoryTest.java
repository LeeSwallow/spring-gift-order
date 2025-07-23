package gift.Test.integration.jpa;

import gift.entity.Option;
import gift.entity.Product;
import gift.entity.User;
import gift.repository.option.OptionRepository;
import gift.repository.product.ProductRepository;
import gift.repository.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OptionRepository optionRepository;

    User testUser;

    @BeforeEach
    public void setUp() {
        if (this.testUser == null) {
            this.testUser = userRepository.getReferenceById(1L);
        }
    }

    @Test
    @Order(1)
    @DisplayName("상품 저장 테스트")
    public void save() {
        Product product = new Product(null, "Test Product", 1000L, "http://example.com/image.jpg",testUser);
        Product saved = productRepository.save(product);
        assertAll (
                () -> assertNotNull(saved.getId()),
                () -> assertEquals(product.getName(), saved.getName()),
                () -> assertEquals(product.getPrice(), saved.getPrice()),
                () -> assertEquals(product.getImageUrl(), saved.getImageUrl()),
                () -> assertEquals(product.getOwner(), saved.getOwner())
        );
    }

    @Test
    @Order(2)
    @DisplayName("상품 조회 테스트")
    public void findById() {
        Product product = new Product(null, "Test Product", 1000L, "http://example.com/image.jpg",testUser);
        Product saved = productRepository.save(product);

        Product found = productRepository.findById(saved.getId()).orElse(null);
        Assertions.assertNotNull(found);
        assertAll(
                () -> assertEquals(saved.getId(), found.getId()),
                () -> assertEquals(saved.getName(), found.getName()),
                () -> assertEquals(saved.getPrice(), found.getPrice()),
                () -> assertEquals(saved.getImageUrl(), found.getImageUrl()),
                () -> assertEquals(saved.getOwner(), found.getOwner())
        );
    }

    @Test
    @Order(3)
    @DisplayName("상품 수정 테스트")
    public void update() {
        Product product = new Product(null, "Test Product", 1000L, "http://example.com/image.jpg", testUser);
        Product saved = productRepository.save(product);

        saved.setName("Updated Product");
        saved.setPrice(2000L);
        saved.setImageUrl("http://example.com/updated_image.jpg");
        Product updated = productRepository.save(saved);

        assertAll(
                () -> assertNotNull(updated.getId()),
                () -> assertEquals("Updated Product", updated.getName()),
                () -> assertEquals(2000L, updated.getPrice()),
                () -> assertEquals("http://example.com/updated_image.jpg", updated.getImageUrl()),
                () -> assertEquals(saved.getOwner(), updated.getOwner())
        );
    }


    @Test
    @Order(4)
    @DisplayName("상품 삭제 테스트")
    public void delete() {
        Product product = new Product(null, "Test Product", 1000L, "http://example.com/image.jpg", testUser);
        Product saved = productRepository.save(product);

        productRepository.deleteById(saved.getId());
        Product found = productRepository.findById(saved.getId()).orElse(null);
        assertAll(
                () -> assertNull(found, "삭제된 상품은 조회되지 않아야 합니다.")
        );
    }

    @Test
    @Order(5)
    @DisplayName("영속성 테스트")
    public void persistenceTest() {
        Product product = new Product(null, "Persistent Product", 1500L, "http://example.com/persistent_image.jpg", testUser);
        List<Option> options = List.of(
                new Option("Option 1", 10L),
                new Option("Option 2", 20L)
        );
        product.setOptions(options);
        Product savedProduct = productRepository.save(product);
        assertNotNull(savedProduct);
        // 저장된 상품의 옵션이 영속성 컨텍스트에 반영되었는지 확인
        assertEquals(2, savedProduct.getOptions().size());
        List<Option> searchedOptions = optionRepository.findAllByProductId(savedProduct.getId(), PageRequest.of(0, 10))
                .getContent();
        // 옵션이 2개가 저장되었는지 확인
        assertAll(
                () -> assertEquals(2, searchedOptions.size()),
                () -> assertEquals("Option 1", searchedOptions.get(0).getName()),
                () -> assertEquals("Option 2", searchedOptions.get(1).getName())
        );
    }
}
