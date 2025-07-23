package gift.Test.integration.jpa;

import gift.entity.Role;
import gift.entity.User;
import gift.entity.UserRole;
import gift.repository.role.RoleRepository;
import gift.repository.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    public void setUp() {
        roleRepository.saveAll(List.of(
                new Role(UserRole.ROLE_USER),
                new Role(UserRole.ROLE_MD),
                new Role(UserRole.ROLE_ADMIN)
        ));
    }

    @Test
    @Order(1)
    @DisplayName("단건 유저 저장 테스트")
    public void save_user_test() {
        User user = new User(
                "test1234@test.com",
                "test1234!",
                Set.of(roleRepository.findByName(UserRole.ROLE_USER).orElseThrow().getName())
        );
        user.setEmail("test1234@test.com");
        user.setPassword("test1234!");
        user.setRoles(Set.of(roleRepository.findByName(UserRole.ROLE_USER).orElseThrow().getName()));
        User savedUser = userRepository.save(user);
        UserRole role = savedUser.getRoles()
                .stream()
                .findFirst()
                .orElseThrow()
                .getName();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(savedUser.getId()),
                () -> Assertions.assertEquals(savedUser.getEmail(), user.getEmail()),
                () -> Assertions.assertEquals(savedUser.getPassword(), user.getPassword()),
                () -> Assertions.assertFalse(savedUser.getRoles().isEmpty()),
                () -> Assertions.assertEquals(UserRole.ROLE_USER, role)
        );
    }

    @Test
    @Order(2)
    @DisplayName("유저 ID로 조회 테스트")
    public void findById_test() {
        User user = new User(
                "test1234@test.com",
                "test1234!",
                Set.of(roleRepository.findByName(UserRole.ROLE_USER).orElseThrow().getName())
        );
        User savedUser = userRepository.save(user);

        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
        Assertions.assertNotNull(foundUser);

        UserRole role = foundUser.getRoles()
                .stream()
                .findFirst()
                .orElseThrow()
                .getName();

        Assertions.assertAll(
                () -> Assertions.assertEquals(savedUser.getId(), foundUser.getId()),
                () -> Assertions.assertEquals(savedUser.getEmail(), foundUser.getEmail()),
                () -> Assertions.assertEquals(savedUser.getPassword(), foundUser.getPassword()),
                () -> Assertions.assertFalse(foundUser.getRoles().isEmpty()),
                () -> Assertions.assertEquals(UserRole.ROLE_USER, role)
        );
    }

    @Test
    @Order(3)
    @DisplayName("유저 이메일로 조회 테스트")
    public void findByEmail_test() {
        User user = new User(
                "test1234@test.com",
                "test1234!",
                Set.of(roleRepository.findByName(UserRole.ROLE_USER).orElseThrow().getName())
        );
        User savedUser = userRepository.save(user);
        User foundUser = userRepository.findByEmail(savedUser.getEmail()).orElse(null);
        Assertions.assertNotNull(foundUser);

        UserRole role = foundUser.getRoles()
                .stream()
                .findFirst()
                .orElseThrow()
                .getName();

        Assertions.assertAll(
                () -> Assertions.assertEquals(savedUser.getId(), foundUser.getId()),
                () -> Assertions.assertEquals(savedUser.getEmail(), foundUser.getEmail()),
                () -> Assertions.assertEquals(savedUser.getPassword(), foundUser.getPassword()),
                () -> Assertions.assertFalse(foundUser.getRoles().isEmpty()),
                () -> Assertions.assertEquals(UserRole.ROLE_USER, role)
        );
    }

    @Test
    @Order(4)
    @DisplayName("유저 정보 수정 테스트")
    public void update_user_test() {
        User user = new User(
                "test1234@test.com",
                "test1234!",
                Set.of(roleRepository.findByName(UserRole.ROLE_USER).orElseThrow().getName())
        );
        User savedUser = userRepository.save(user);
        // 유저 정보 수정
        savedUser.setEmail("modified1234@test.com");
        savedUser.setPassword("modified1234!");
        var roles = Set.of(roleRepository.findByName(UserRole.ROLE_MD).orElseThrow().getName());
        savedUser.setRoles(new HashSet<>(roles));
        User updatedUser = userRepository.save(savedUser);

        UserRole updatedRole = updatedUser.getRoles()
                .stream()
                .findFirst()
                .orElseThrow()
                .getName();

        Assertions.assertAll(
                () -> Assertions.assertEquals(updatedUser.getEmail(), savedUser.getEmail()),
                () -> Assertions.assertEquals(updatedUser.getPassword(), savedUser.getPassword()),
                () -> Assertions.assertFalse(updatedUser.getRoles().isEmpty()),
                () -> Assertions.assertEquals(UserRole.ROLE_MD, updatedRole)
        );
    }

    @Test
    @Order(5)
    @DisplayName("유저 삭제 테스트")
    public void delete_user_test() {
        User user = new User(
                "test1234@test.com",
                "test1234!",
                Set.of(roleRepository.findByName(UserRole.ROLE_USER).orElseThrow().getName())
        );
        User savedUser = userRepository.save(user);
        // 저장된 유저 삭제
        userRepository.deleteById(savedUser.getId());

        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
        Assertions.assertNull(foundUser);
    }

    @Test
    @Order(6)
    @DisplayName("페이지네이션 테스트")
    public void findAllByPageable_test() {
        // 여러 유저 저장
        for (int i = 0; i < 10; i++) {
            User user = new User(
                    "test" + i + "@test.com",
                    "test" + i + "!",
                    Set.of(roleRepository.findByName(UserRole.ROLE_USER).orElseThrow().getName())
            );
            userRepository.save(user);
        }

        // 페이지네이션 조회
        var pageable = PageRequest.of(0, 5);
        var page = userRepository.findAllBy(pageable);

        Assertions.assertAll(
                () -> Assertions.assertEquals(5, page.getContent().size()),
                () -> Assertions.assertTrue(page.hasNext())
        );
    }
}
