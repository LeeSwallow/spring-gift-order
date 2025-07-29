package gift.Test.integration.jpa;

import gift.entity.*;
import gift.entity.type.UserRole;
import gift.repository.product.ProductRepository;
import gift.repository.role.RoleRepository;
import gift.repository.user.UserRepository;
import gift.repository.wishlist.WishedProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WishedProductRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishedProductRepository wishedProductRepository;

    private User testUser;
    private List<Product> testProducts;

    @BeforeEach
    public void setUp() {
        roleRepository.saveAll(List.of(
                new Role(UserRole.ROLE_USER),
                new Role(UserRole.ROLE_MD),
                new Role(UserRole.ROLE_ADMIN)
        ));

        if (this.testUser == null) {
            User user = new User(
                    "testuser@test.com",
                    "testuser123!",
                    Set.of(roleRepository.findByName(UserRole.ROLE_USER).orElseThrow().getName())
            );
            this.testUser = userRepository.save(user);
        }
        if (this.testProducts == null) {
            this.testProducts = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Product product = new Product(null, "Test Product " + i, 1000L + (i * 100), "http://example.com/image" + i + ".jpg", this.testUser);
                this.testProducts.add(productRepository.save(product));
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("위시리스트에 상품 추가 테스트")
    public void save_test() {
        WishedProduct wishedProduct = new WishedProduct(
                this.testUser,
                this.testProducts.getFirst(),
                2
        );
        WishedProduct saved = wishedProductRepository.save(wishedProduct);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(saved.getId()),
                () -> Assertions.assertEquals(this.testUser.getId(), saved.getUser().getId()),
                () -> Assertions.assertEquals(this.testProducts.getFirst().getId(), saved.getProduct().getId()),
                () -> Assertions.assertEquals(2, saved.getQuantity())
        );
    }

    @Test
    @Order(2)
    @DisplayName("위시리스트 상품 조회 테스트")
    public void findById_test() {
        WishedProduct wishedProduct = new WishedProduct(
                this.testUser,
                this.testProducts.getFirst(),
                2
        );
        WishedProduct saved = wishedProductRepository.save(wishedProduct);

        WishedProduct found = wishedProductRepository.findById(saved.getId()).orElse(null);
        Assertions.assertNotNull(found);
        Assertions.assertAll(
                () -> Assertions.assertEquals(saved.getId(), found.getId()),
                () -> Assertions.assertEquals(saved.getUser().getId(), found.getUser().getId()),
                () -> Assertions.assertEquals(saved.getProduct().getId(), found.getProduct().getId()),
                () -> Assertions.assertEquals(saved.getQuantity(), found.getQuantity())
        );
    }

    @Test
    @Order(3)
    @DisplayName("위시리스트 상품 수정 테스트")
    public void update_test() {
        WishedProduct wishedProduct = new WishedProduct(
                this.testUser,
                this.testProducts.getFirst(),
                2
        );
        WishedProduct saved = wishedProductRepository.save(wishedProduct);

        saved.setQuantity(3);
        WishedProduct updated = wishedProductRepository.save(saved);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(updated.getId()),
                () -> Assertions.assertEquals(saved.getUser().getId(), updated.getUser().getId()),
                () -> Assertions.assertEquals(saved.getProduct().getId(), updated.getProduct().getId()),
                () -> Assertions.assertEquals(3, updated.getQuantity())
        );
    }

    @Test
    @Order(4)
    @DisplayName("위시리스트 단건 삭제 테스트")
    public void delete_test() {
        WishedProduct wishedProduct = new WishedProduct(
                this.testUser,
                this.testProducts.getFirst(),
                2
        );
        WishedProduct saved = wishedProductRepository.save(wishedProduct);

        wishedProductRepository.delete(saved);
        Assertions.assertFalse(wishedProductRepository.findById(saved.getId()).isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("위시리스트 전체 삭제 테스트")
    public void deleteAll_test() {
        testProducts.forEach(product -> {
            WishedProduct wishedProduct = new WishedProduct(
                    this.testUser,
                    product,
                    1
            );
            wishedProductRepository.save(wishedProduct);
        });
        wishedProductRepository.deleteAll();
        Assertions.assertTrue(wishedProductRepository.findAll().isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("사용자별 위시리스트 페이지네이션 테스트")
    public void findByUserIdWithPagination_test() {
        int quantity = 1;
        long totalPrice = 0;
        long totalQuantity = 0;
        for (Product product : testProducts) {
            WishedProduct wishedProduct = new WishedProduct(
                    this.testUser,
                    product,
                    quantity++
            );
            wishedProductRepository.save(wishedProduct);
            totalPrice += product.getPrice() * wishedProduct.getQuantity();
            totalQuantity += wishedProduct.getQuantity();
        }
        var pagedProducts = wishedProductRepository.findAllByUserId(
                this.testUser.getId(),
                PageRequest.of(0, 5)
            ).getContent();

        Assertions.assertFalse(pagedProducts.isEmpty());
        Assertions.assertAll(
                () -> Assertions.assertEquals(5, pagedProducts.size()),
                () -> Assertions.assertEquals(this.testUser.getId(), pagedProducts.getFirst().getUser().getId())
        );

        var stats = wishedProductRepository.calculateStatsByUserId(this.testUser.getId());
        long finalTotalPrice = totalPrice;
        long finalTotalQuantity = totalQuantity;
        Assertions.assertAll(
                () -> Assertions.assertEquals(finalTotalPrice, stats.getTotalPrice()),
                () -> Assertions.assertEquals(finalTotalQuantity, stats.getTotalQuantity())
        );
    }
}
