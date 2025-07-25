package gift.Test.e2e.user;

import gift.dto.user.UserAdminResponse;
import gift.dto.user.UserCreateRequest;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class UserDeleteTest extends AbstractUserTest {

    @Test
    @DisplayName("사용자 삭제 성공 테스트: 관리자 권한으로 요청")
    public void Admin_Delete_Success() {
        String url = getRequestUrl() + "/{id}";
        Long targetId = this.testUsers.get(UserRole.ROLE_USER).id();
        RestAssured.given(this.spec)
                .filter(document("관리자 권한으로 사용자 삭제 성공",
                        pathParameters(parameterWithName("id").description("삭제할 사용자 ID")),
                        requestHeaders(AUTHENTICATE_HEADERS)))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN)) // 관리자 권한으로 요청
                .when()
                .delete(url, targetId)
                .then()
                .statusCode(204);

        // 삭제한 사용자 ID 제거
        this.testedUserIds.removeIf(id -> id.equals(targetId));
    }

    @Test
    @DisplayName("사용자 삭제 성공 테스트: 일반 사용자 권한으로 요청")
    public void User_Delete_Success() {
        String url = getRequestUrl()+ "/me";
        RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER)) // 일반 사용자 권한으로 요청
                .delete(url)
                .then()
                .statusCode(204);
        // 삭제한 사용자 ID 제거
        this.testedUserIds.removeIf(id -> id.equals(this.testUsers.get(UserRole.ROLE_USER).id()));
    }

    @Test
    @DisplayName("사용자 삭제 실패 테스트: 존재하지 않는 사용자 ID")
    public void Admin_Delete_Failure_NonExistentId() {
        String url = getRequestUrl() + "/{id}";
        Long nonExistentId = 9999L; // 존재하지 않는 사용자 ID
        RestAssured.given(this.spec)
                .filter(document("관리자 권한으로 존재하지 않는 사용자 삭제 실패",
                        pathParameters(parameterWithName("id").description("삭제할 사용자 ID")),
                        requestHeaders(AUTHENTICATE_HEADERS)))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN)) // 관리자 권한으로 요청
                .when()
                .delete(url, nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("사용자 삭제 실패 테스트: 일반 사용자 권한으로 요청")
    public void User_Delete_Failure_Unauthorized() {
        UserAdminResponse userResponse = RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 일반 사용자 권한으로 요청
                .body(new UserCreateRequest("delete@test.com", "password123!", List.of("ROLE_USER")))
                .when()
                .get(getRequestUrl() + "/me") // 자신의 정보 조회
                .then()
                .statusCode(200)
                .extract()
                .as(UserAdminResponse.class);

        Long targetId = userResponse.id(); // 삭제할 사용자 ID

        String url = getRequestUrl() + "/{id}";

        RestAssured.given(this.spec)
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER)) // 일반 사용자 권한으로 요청
                .when()
                .delete(url, targetId)
                .then()
                .statusCode(403); // 권한 없음
    }
}
