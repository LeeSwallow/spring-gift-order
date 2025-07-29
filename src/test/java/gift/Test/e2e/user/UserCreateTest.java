package gift.Test.e2e.user;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.dto.user.UserAdminResponse;
import gift.dto.user.UserCreateRequest;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class UserCreateTest extends AbstractUserTest {

    private final FieldDescriptor[] USER_CREATE_REQUEST = {
        fieldWithPath("email").description("사용자 이메일").type(JsonFieldType.STRING),
        fieldWithPath("password").description("사용자 비밀번호").type(JsonFieldType.STRING),
        fieldWithPath("roles[]").description("사용자 역할 목록 (기본값: [ROLE_USER])").type(JsonFieldType.ARRAY).optional(),
    };

    private final FieldDescriptor[] ADMIN_RESPONSE = {
        fieldWithPath("id").description("사용자 ID").type(JsonFieldType.NUMBER),
        fieldWithPath("email").description("사용자 이메일").type(JsonFieldType.STRING).optional(),
        fieldWithPath("password").description("인코딩된 사용자 비밀번호").type(JsonFieldType.STRING).optional(),
        fieldWithPath("clientId").description("사용자 클라이언트 ID").type(JsonFieldType.STRING).optional(),
        fieldWithPath("provider").description("사용자 제공자 (기본값: LOCAL)").type(JsonFieldType.STRING),
        fieldWithPath("roles").description("사용자 역할 목록").type(JsonFieldType.ARRAY),
            fieldWithPath("createdAt").description("사용자 생성 시간").type(JsonFieldType.STRING),
        fieldWithPath("updatedAt").description("사용자 수정 시간").type(JsonFieldType.STRING)
    };


    @Test
    @DisplayName("사용자 생성 성공 테스트: 관리자 권한으로 요청")
    public void Admin_Create_Success() {
        String url = getRequestUrl();
        UserCreateRequest request = new UserCreateRequest("testuser1@example.com", "password123!", List.of("ROLE_USER"));
        UserAdminResponse response = RestAssured.given(this.spec)
                .filter(document("관리자 권한으로 사용자 생성 성공",
                    resource(ResourceSnippetParameters.builder()
                        .tag("User")
                        .summary("사용자 생성 API")
                        .description("관리자 권한으로 새로운 사용자를 생성합니다.")
                        .requestFields(USER_CREATE_REQUEST)
                        .requestHeaders(AUTHENTICATE_HEADERS)
                        .responseFields(ADMIN_RESPONSE)
                        .build()
                )))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN)) // 관리자 권한으로 요청
                .body(request)
                .when()
                .post(url)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("email", notNullValue())
                .body("password", notNullValue())
                .body("roles", notNullValue())
                .body("roles[0]", equalTo("ROLE_USER"))
                .extract()
                .as(UserAdminResponse.class);
        this.testedUserIds.add(response.id());
    }

    @Test
    @DisplayName("사용자 생성 실패 테스트: 관리자 권한 없이 요청(403 Forbidden)")
    public void Admin_Create_Failure_NoAuth() {
        String url = getRequestUrl();
        UserCreateRequest request = new UserCreateRequest("testuser1@example.com", "password123!", List.of("ROLE_USER"));
        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(url)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("사용자 생성 실패 테스트: 필수 필드 누락(400 Bad Request)")
    public void User_Create_Failure_MissingFields() {
        String url = getRequestUrl();
        UserCreateRequest request = new UserCreateRequest(null, null, null);
        RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_MD)) // 관리자 권한으로 요청
                .body(request)
                .when()
                .post(url)
                .then()
                .statusCode(400);
    }
}
