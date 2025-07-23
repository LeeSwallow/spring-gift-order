package gift.Test.integration.jpa;

import gift.entity.Role;
import gift.entity.UserRole;
import gift.repository.role.RoleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoleRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;


    @Test
    @DisplayName("간단한 role repository 저장/읽기 테스트")
    public void roleRepositoryTest() {
        UserRole userRole = UserRole.ROLE_USER;
        Role role = roleRepository.save(new Role(userRole));
        assertEquals(userRole, role.getName());

        Role foundRole = roleRepository.findByName(userRole).orElse(null);
        Assertions.assertNotNull(foundRole);
    }
}
