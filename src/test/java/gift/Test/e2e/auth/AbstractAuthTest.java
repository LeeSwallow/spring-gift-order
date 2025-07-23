package gift.Test.e2e.auth;

import gift.Test.e2e.AbstractControllerTest;
import gift.dto.user.UserAdminResponse;
import gift.dto.user.UserCreateRequest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.restdocs.RestDocumentationContextProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractAuthTest extends AbstractControllerTest {

    protected Map<UserCreateRequest, UserAdminResponse> testUsers;

    private UserAdminResponse getAdminResponse(UserCreateRequest request) {
        return RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 토큰 사용
                .body(request)
                .post(getBaseUrl() + "/api/users")
                .then()
                .statusCode(201)
                .extract()
                .as(UserAdminResponse.class);
    }

    @BeforeEach
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);
        this.testUsers = new HashMap<>();

        UserCreateRequest userRequest = new UserCreateRequest(
                "user1@example.com",
                "password123!",
                List.of("ROLE_USER")
        );
        UserAdminResponse userResponse = getAdminResponse(userRequest);
        this.testUsers.put(userRequest, userResponse);

        UserCreateRequest adminRequest = new UserCreateRequest(
                "user2@example.com",
                "password123!",
                List.of("ROLE_ADMIN")
        );
        UserAdminResponse adminResponse = getAdminResponse(adminRequest);
        this.testUsers.put(adminRequest, adminResponse);
    }

    @AfterEach
    public void tearDown() {
        if (this.testUsers == null || this.testUsers.isEmpty()) {
            return;
        }
        for (UserAdminResponse user : this.testUsers.values()) {
            RestAssured.given()
                    .header(AUTH_HEADER_KEY, this.adminToken)
                    .when()
                    .delete(getBaseUrl() + "/api/users/" + user.id())
                    .then()
                    .statusCode(204);
        }
        this.testUsers.clear();
    }

    public String getRequestUrl() {
        return getBaseUrl() + "/api/auth";
    }
}
