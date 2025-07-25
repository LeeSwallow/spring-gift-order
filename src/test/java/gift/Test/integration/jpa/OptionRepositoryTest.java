package gift.Test.integration.jpa;

import gift.entity.*;
import gift.entity.type.UserRole;
import gift.repository.option.OptionRepository;
import gift.repository.product.ProductRepository;
import gift.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class OptionRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OptionRepository optionRepository;

    private User testUser;
    private Product testProduct;
    private List<Option> testOptions;

    @BeforeEach
    public void setUp() {
        // 테스트 사용자 생성
        if (testUser == null) {
            Set<UserRole> roles = Set.of(UserRole.ROLE_ADMIN);
            testUser = new User("option@repo.test", "OptionRepoTest!", roles);
            testUser = userRepository.save(testUser);
        }
        // 테스트 제품 생성
        if (testProduct == null) {
            testProduct = new Product("Test Product", 1000L, "http://example.com/image.jpg", testUser);
            testProduct = productRepository.save(testProduct);
        }
        if (testOptions == null) {
            testOptions = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Option option = new Option("Test Option " + i, 100L + i * 10, testProduct);
                testOptions.add(optionRepository.save(option));
            }
        }
    }

    @Test
    @DisplayName("옵션 저장 테스트")
    @Order(1)
    public void saveOption() {
        Option option = new Option("saveTestOption", 100L, testProduct);
        Option savedOption = optionRepository.save(option);
        assertNotNull(savedOption);
        assertAll (
                () -> assertEquals(savedOption.getName(), option.getName()),
                () -> assertEquals(savedOption.getProduct(), option.getProduct()),
                () -> assertEquals(savedOption.getQuantity(), option.getQuantity())
        );
        testOptions.add(savedOption);
        // 같은 이름 제품 추가시 예외 발생 확인
        Option duplicateOption = new Option("saveTestOption", 200L, testProduct);
        assertThrows(
                DataIntegrityViolationException.class,
                () -> optionRepository.save(duplicateOption)
        );
    }

    @Test
    @DisplayName("옵션 목록 조회 테스트")
    @Order(2)
    public void findAllOptions() {
        for (int i = 0; i < 5; i++) {
            Option option = new Option("findAllTestOption " + i, 100L + i * 10, testProduct);
            testOptions.add(optionRepository.save(option));
        }
        Page<Option> optionsPage =
                optionRepository.findAllByProductId(testProduct.getId(), PageRequest.of(0, 5));

        assertNotNull(optionsPage);
        assertAll(
                () -> assertEquals(testOptions.size(), optionsPage.getTotalElements()),
                () -> assertEquals(5, optionsPage.getSize()),
                () -> assertTrue(testOptions.containsAll(optionsPage.getContent())),
                () -> assertTrue(optionsPage.getContent().stream()
                        .allMatch(option -> option.getProduct().getId().equals(testProduct.getId())))
        );
    }

    @Test
    @DisplayName("옵션 ID로 조회 테스트")
    @Order(3)
    public void findOptionById() {
        Long validId = testOptions.getFirst().getId();
        Long invalidId = -1L;

        Optional<Option> foundOption = optionRepository.findById(validId);
        assertTrue(foundOption.isPresent());
        assertEquals(testOptions.getFirst(), foundOption.get());
        assertFalse(optionRepository.findById(invalidId).isPresent());
    }

    @Test
    @DisplayName("옵션 수정 테스트")
    @Order(4)
    public void updateOption() {
        Option optionToUpdate = testOptions.getFirst();
        optionToUpdate.setName("Updated Option Name");
        optionToUpdate.setQuantity(200L);

        Option updatedOption = optionRepository.save(optionToUpdate);

        assertNotNull(updatedOption);
        assertAll(
                () -> assertEquals("Updated Option Name", updatedOption.getName()),
                () -> assertEquals(200L, updatedOption.getQuantity())
        );
    }

    @Test
    @DisplayName("옵션 삭제 테스트")
    @Order(5)
    public void deleteOption() {
        Option optionToDelete = testOptions.getFirst();
        Long idToDelete = optionToDelete.getId();
        optionRepository.deleteById(optionToDelete.getId());
        assertFalse(optionRepository.findById(idToDelete).isPresent());
        testOptions.removeFirst();
    }

}
