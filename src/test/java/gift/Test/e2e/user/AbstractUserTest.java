package gift.Test.e2e.user;


import gift.Test.e2e.AbstractControllerTest;
import gift.Test.e2e.testutil.RestAssuredUtils;
import gift.dto.user.UserAdminResponse;
import gift.dto.user.UserCreateRequest;
import gift.entity.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.restdocs.RestDocumentationContextProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractUserTest extends AbstractControllerTest {
    private RestAssuredUtils restAssuredUtils;
    protected Map<UserRole, UserAdminResponse> testUsers;
    protected Map<UserRole, String> testUserTokens;
    protected List<Long> testedUserIds;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);

        restAssuredUtils = new RestAssuredUtils(getBaseUrl(), this.adminToken);
        this.testUsers = new HashMap<>();
        this.testUserTokens = new HashMap<>();
        this.testedUserIds = new ArrayList<>();

        Map.of(
                UserRole.ROLE_ADMIN,
                new UserCreateRequest("user1@test.com", "password123!", List.of("ROLE_ADMIN")),
                UserRole.ROLE_MD,
                new UserCreateRequest("user2@test.com", "password123!", List.of("ROLE_MD")),
                UserRole.ROLE_USER,
                new UserCreateRequest("user3@test.com", "password123!", List.of("ROLE_USER"))
        ).forEach((role, request) -> {
            this.testUsers.put(role, restAssuredUtils.createUser(request));
            this.testUserTokens.put(role, restAssuredUtils.getToken(request));
            this.testedUserIds.add(this.testUsers.get(role).id());
        });
    }

    @AfterEach
    public void tearDown() {
        if (this.testedUserIds != null) {
            this.testedUserIds.forEach(userId -> {
                if (userId != null) {
                    restAssuredUtils.deleteUser(userId);
                }
            });
        }
        this.testUsers.clear();
        this.testUserTokens.clear();
        this.testedUserIds.clear();
        this.restAssuredUtils = null;
    }

    public String getRequestUrl() {
        return getBaseUrl() + "/api/users";
    }

}
