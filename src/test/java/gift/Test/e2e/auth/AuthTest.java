package gift.Test.e2e.auth;

import gift.dto.auth.LoginRequest;
import gift.dto.auth.SignupRequest;
import gift.dto.user.UserCreateRequest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class AuthTest extends AbstractAuthTest {

    private static final FieldDescriptor[] LOGIN_REQUEST_FIELDS = {
        fieldWithPath("email").description("사용자 이메일").type(JsonFieldType.STRING),
        fieldWithPath("password").description("사용자 비밀번호").type(JsonFieldType.STRING)
    };

    private static final FieldDescriptor[] SIGNUP_REQUEST_FIELDS = {
        fieldWithPath("email").description("사용자 이메일").type(JsonFieldType.STRING),
        fieldWithPath("password").description("사용자 비밀번호").type(JsonFieldType.STRING),
        fieldWithPath("passwordConfirm").description("비밀번호 확인").type(JsonFieldType.STRING),
    };

    private static final FieldDescriptor[] AUTH_RESPONSE_FIELDS = {
        fieldWithPath("token").description("인증 토큰").type(JsonFieldType.STRING)
    };

    @Test
    @DisplayName("로그인 성공 테스트")
    public void testLoginSuccess() {
        UserCreateRequest req = this.testUsers
                .keySet()
                .stream()
                .findFirst()
                .orElseThrow();

        // 로그인 요청
        RestAssured.given(this.spec)
                .filter(document("로그인 성공",
                        requestFields(LOGIN_REQUEST_FIELDS),
                        responseFields(AUTH_RESPONSE_FIELDS)))
                .contentType("application/json")
                .body(new LoginRequest(req.email(), req.password()))
                .when()
                .post(getBaseUrl() + "/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 이메일 또는 비밀번호(401 Unauthorized)")
    public void testLoginFailure() {
        RestAssured.given()
                .contentType("application/json")
                .body(new LoginRequest("notexist@test.com", "password123!"))
                .when()
                .post(getBaseUrl() + "/api/auth/login")
                .then()
                .statusCode(401);
    }


    @Test
    @DisplayName("회원가입 성공 테스트")
    public void testSignupSuccess() {
        SignupRequest request = new SignupRequest(
                "newuser@test.com", "password123!", "password123!");

        RestAssured.given(this.spec)
                .filter(document("회원가입 성공",
                        requestFields(SIGNUP_REQUEST_FIELDS),
                        responseFields(AUTH_RESPONSE_FIELDS)))
                .contentType("application/json")
                .body(request)
                .when()
                .post(getBaseUrl() + "/api/auth/signup")
                .then()
                .statusCode(201)
                .body("token", notNullValue());
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 유효성 : 이메일 형식 오류(400 Bad Request)")
    public void testSignupFailureInvalidEmail() {
        SignupRequest request = new SignupRequest(
                "invalid-email", "password123!", "password123!");
        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(getBaseUrl() + "/api/auth/signup")
                .then()
                .statusCode(400)
                .body("title", notNullValue())
                .body("detail", notNullValue())
                .body("validationErrors", Matchers.hasSize(1));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 유효성 : 비밀번호 유효성(400 Bad Request)")
    public void testSignupFailureInvalidPassword() {
        SignupRequest notContainSign = new SignupRequest(
                "newuser@test.com", "password123", "password123");

        SignupRequest notContainNumber = new SignupRequest(
                "newuser@test.com", "password!", "password!");

        SignupRequest tooShort = new SignupRequest(
                "newuser@test.com", "sh!1", "sh!1");
        SignupRequest tooLong = new SignupRequest(
                "newuser@test.com", "tooLong!tooLong!tooLong!tooLong!tooLong!tooLong!1",
                "tooLong!tooLong!tooLong!tooLong!tooLong!tooLong!1");

        List<SignupRequest> invalidCases = List.of(
                notContainSign, notContainNumber, tooShort, tooLong
        );

        invalidCases.forEach(request -> {
            RestAssured.given()
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post(getBaseUrl() + "/api/auth/signup")
                    .then()
                    .statusCode(400)
                    .body("title", notNullValue())
                    .body("detail", notNullValue())
                    .body("validationErrors", Matchers.hasSize(1));
        });
    }


    @Test
    @DisplayName("회원가입 실패 테스트 - 유효성 : 비밀번호 불일치(400 Bad Request)")
    public void testSignupFailureUnMatchedPassword() {
        SignupRequest request = new SignupRequest(
                "newuser@test.com", "password123!", "differentPassword!");
        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(getBaseUrl() + "/api/auth/signup")
                .then()
                .statusCode(400)
                .body("title", notNullValue())
                .body("detail", notNullValue())
                .body("validationErrors", Matchers.hasSize(1));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 이메일 중복(409 Conflict)")
    public void testSignupFailureDuplicateEmail() {
        UserCreateRequest existingUser = this.testUsers
                .keySet()
                .stream()
                .findFirst()
                .orElseThrow();

        SignupRequest request = new SignupRequest(
                existingUser.email(), "password123!", "password123!");
        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(getBaseUrl() + "/api/auth/signup")
                .then()
                .statusCode(409)
                .body("title", notNullValue())
                .body("detail", notNullValue());
    }
}
