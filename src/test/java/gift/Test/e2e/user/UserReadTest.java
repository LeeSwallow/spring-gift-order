package gift.Test.e2e.user;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.common.model.CustomPage;
import gift.dto.user.UserAdminResponse;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class UserReadTest extends  AbstractUserTest{

    public static final FieldDescriptor[] MULTIPLE_ADMIN_READ_RESPONSE = concat(BASE_PAGINATION_FIELDS, new FieldDescriptor[]{
            fieldWithPath("contents[]").description("사용자 목록").type(JsonFieldType.ARRAY),
            fieldWithPath("contents[].id").description("사용자 ID").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].email").description("사용자 이메일").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].password").description("인코딩된 사용자 비밀번호").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].clientId").description("사용자 클라이언트 ID").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].provider").description("사용자 제공자 (기본값: LOCAL)").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].roles").description("사용자 역할 목록").type(JsonFieldType.ARRAY).optional(),
            fieldWithPath("contents[].createdAt").description("사용자 생성 시간").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].updatedAt").description("사용자 업데이트 시간").type(JsonFieldType.STRING).optional()
    });

    public static final FieldDescriptor[] SINGLE_ADMIN_READ_RESPONSE = {
            fieldWithPath("id").description("사용자 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("email").description("사용자 이메일").type(JsonFieldType.STRING),
            fieldWithPath("password").description("인코딩된 사용자 비밀번호").type(JsonFieldType.STRING),
            fieldWithPath("clientId").description("사용자 클라이언트 ID").type(JsonFieldType.STRING).optional(),
            fieldWithPath("provider").description("사용자 제공자 (기본값: LOCAL)").type(JsonFieldType.STRING),
            fieldWithPath("roles").description("사용자 역할 목록").type(JsonFieldType.ARRAY),
            fieldWithPath("createdAt").description("사용자 생성 시간").type(JsonFieldType.STRING),
            fieldWithPath("updatedAt").description("사용자 업데이트 시간").type(JsonFieldType.STRING)
    };

    public static final FieldDescriptor[] SINGLE_USER_READ_RESPONSE = {
            fieldWithPath("id").description("사용자 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("email").description("사용자 이메일").type(JsonFieldType.STRING).optional(),
            fieldWithPath("provider").description("사용자 제공자 (기본값: LOCAL)").type(JsonFieldType.STRING),
    };



    @Test
    @DisplayName("다건 사용자 조회 성공 테스트: 관리자 권한으로 요청")
    public void find_All_Users_Success() {
        String url = getRequestUrl();
        RestAssured.given(this.spec)
                .filter(document("사용자 전체 조회 성공",
                        resource(
                        ResourceSnippetParameters.builder()
                            .tag("User")
                            .summary("전체 사용자 조회 API")
                            .description("전체 사용자 목록을 페이지 단위로 조회합니다.")
                            .queryParameters(PAGE_PARAMETERS)
                            .requestHeaders(AUTHENTICATE_HEADERS)
                            .responseFields(MULTIPLE_ADMIN_READ_RESPONSE)
                            .build()
                )))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN)) // 관리자 권한으로 요청
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("page", notNullValue())
                .body("size", notNullValue())
                .body("totalElements", notNullValue())
                .body("totalPages", notNullValue())
                .body("contents", notNullValue())
                .body("contents[0].id", notNullValue())
                .body("contents[0].email", notNullValue())
                .body("contents[0].password", notNullValue())
                .body("contents[0].roles", notNullValue());
    }

    @Test
    @DisplayName("다건 사용자 조회 성공 테스트: 페이지 파라미터와 정렬 기준 포함")
    public void find_All_Users_Success_With_Page_And_Sort() {
        String url = getRequestUrl();
        CustomPage<UserAdminResponse> res = RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN)) // 관리자 권한으로 요청
                .queryParam("page", 0)
                .queryParam("size", 5)
                .queryParam("sort", "id,desc") // 이메일 내림차순 정렬
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        Long prevId = Long.MAX_VALUE; // 이전 ID를 최대값으로 초기화
        for (UserAdminResponse user : res.getContents()) {
            assertThat(user.id(), notNullValue());
            assertThat(user.id(), lessThanOrEqualTo(prevId));
            prevId = user.id(); // 현재 ID를 이전 ID로 업데이트
        }
    }

    @Test
    @DisplayName("다건 사용자 조회 실패 테스트: 관리자 권한 없이 요청(403 Forbidden)")
    public void find_All_Users_Failure_NoAuth() {
        String url = getRequestUrl();
        RestAssured.given()
                .contentType("application/json")
                .when()
                .get(url)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("다건 사용자 실패 테스트: page, size 파라미터가 음수인 경우 (400 Bad Request)")
    public void find_All_Users_Success_Negative_Page_And_Size_Request() {
        String url = getRequestUrl();
        RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN)) // 관리자 권한으로 요청
                .queryParam("page", -1) // 음수 페이지 번호
                .queryParam("size", -5) // 음수 페이지 크기
                .when()
                .get(url)
                .then()
                .statusCode(400) // 유효성 검사 오류 발생
                .body("validationErrors", notNullValue()); // 유효성 검사 오류가 발생해야 함
    }

    @Test
    @DisplayName("다건 사용자 조회 실패 테스트: 잘못된 정렬 기준으로 요청(400 Bad Request)")
    public void find_All_Users_Failure_Invalid_Sort_Request() {
        List<String> invalidSortFields = List.of(
                "invalidField", // 존재하지 않는 정렬 기준
                "email,invalidDirection", // 잘못된 정렬 방향
                "email,asc,desc" // 잘못된 정렬 기준
        );

        invalidSortFields.forEach(sortField -> {
            String url = getRequestUrl();
            RestAssured.given()
                    .contentType("application/json")
                    .param("sort", sortField)
                    .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN)) // 관리자 권한으로 요청
                    .when()
                    .get(url)
                    .then()
                    .statusCode(400)
                    .body("validationErrors", notNullValue());
        });
    }

    @Test
    @DisplayName("단건 사용자 조회 성공 테스트: 관리자 권한으로 요청")
    public void find_User_By_Id_Success() {
        String url = getRequestUrl() + "/{id}";
        UserAdminResponse userResponse = this.testUsers.get(UserRole.ROLE_ADMIN);

        RestAssured.given(this.spec)
                .filter(document("사용자 ID로 조회 성공",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("User")
                            .summary("단건 사용자 조회 API")
                            .description("특정 사용자의 정보를 조회합니다.")
                            .requestHeaders(AUTHENTICATE_HEADERS)
                            .responseFields(SINGLE_ADMIN_READ_RESPONSE)
                            .pathParameters(
                                    parameterWithName("id").description("조회할 사용자 ID")
                            )
                            .build()
                )))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN)) // 관리자 권한으로 요청
                .when()
                .get(url, userResponse.id()) // 존재하는 사용자 ID로 변경
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("email", notNullValue())
                .body("password", notNullValue())
                .body("roles", notNullValue());
    }

    @Test
    @DisplayName("단건 사용자 조회 실패 테스트: 존재하지 않는 사용자 ID")
    public void find_User_By_Id_Failure_NonExistentId() {
        String url = getRequestUrl() + "/{id}";
        RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                .when()
                .get(url, 9999) // 존재하지 않는 사용자 ID로 변경
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("단건 사용자 조회 실패 테스트: 관리자 권한 없이 요청(403 Forbidden)")
    public void find_User_By_Id_Failure_NoAuth() {
        String url = getRequestUrl() + "/{id}";
        RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER)) // 일반 사용자 권한으로 요청
                .when()
                .get(url, testUsers.get(UserRole.ROLE_USER).id()) // 일반 사용자 권한으로 요청
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("단건 사용자 조회 성공 테스트: 일반 사용자 권한으로 요청")
    public void find_User_By_Id_Success_As_User() {
        String url = getRequestUrl() + "/me";
        UserAdminResponse userResponse = this.testUsers.get(UserRole.ROLE_USER);
        RestAssured.given(this.spec)
                .filter(document("사용자 단건 조회 성공 - 일반 사용자 권한",
                        resource(
                        ResourceSnippetParameters.builder()
                            .tag("User")
                            .summary("자기 자신 조회 API")
                            .description("일반 사용자 권한으로 자신의 정보를 조회합니다.")
                            .responseFields(SINGLE_USER_READ_RESPONSE)
                            .requestHeaders(AUTHENTICATE_HEADERS)
                            .build()
                )))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER)) // 일반 사용자 권한으로 요청
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("id", equalTo(userResponse.id().intValue()))
                .body("email", notNullValue())
                .body("email", equalTo(userResponse.email()));
    }
}
