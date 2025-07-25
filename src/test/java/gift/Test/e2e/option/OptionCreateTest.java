package gift.Test.e2e.option;

import gift.dto.option.OptionCreateRequest;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class OptionCreateTest extends AbstractOptionTest {
    private final FieldDescriptor[] OPTION_CREATE_REQUEST = {
            fieldWithPath("name").description("옵션 이름").type(JsonFieldType.STRING),
            fieldWithPath("quantity").description("옵션 수량").type(JsonFieldType.NUMBER)
    };

    private final FieldDescriptor[] PRODUCT_CREATE_RESPONSE = {
            fieldWithPath("id").description("옵션 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("name").description("옵션 이름").type(JsonFieldType.STRING),
            fieldWithPath("quantity").description("옵션 수량").type(JsonFieldType.NUMBER),
            fieldWithPath("createdAt").description("옵션 생성 시간").type(JsonFieldType.STRING),
            fieldWithPath("updatedAt").description("옵션 수정 시간").type(JsonFieldType.STRING)
    };

    private ValidatableResponse requestWithoutDocumentation(
            OptionCreateRequest request, Long productId, String token
    ) {
        return RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, token)
                .body(request)
                .when()
                .post(getRequestUrl(), productId)
                .then();
    }

    @Test
    @DisplayName("옵션 생성 요청 성공 테스트")
    public void Option_Create_Success() {
        OptionCreateRequest request = new OptionCreateRequest("Test Create Option",1000L);
        Long testProductId = this.testProducts.get(UserRole.ROLE_USER).id();

        RestAssured.given(this.spec)
                .filter(document("옵션 생성 성공",
                        pathParameters(
                            parameterWithName("productId").description("상품 ID")
                        ),
                        requestHeaders(AUTHENTICATE_HEADERS),
                        requestFields(OPTION_CREATE_REQUEST),
                        responseFields(PRODUCT_CREATE_RESPONSE)))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken)
                .body(request)
                .when()
                .post(getRequestUrl(), testProductId)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", notNullValue())
                .body("name", equalTo(request.name()))
                .body("quantity", notNullValue())
                .body("quantity", equalTo(request.quantity().intValue()));
    }

    @Test
    @DisplayName("옵션 생성 요청 성공 테스트 - 소유중인 상품에 소유자가 옵션 생성 요청")
    public void Option_Create_Success_Owner() {
        OptionCreateRequest request = new OptionCreateRequest("Test Create Option", 1000L);
        Long testProductId = this.testProducts.get(UserRole.ROLE_USER).id();
        String ownerToken = this.testUserTokens.get(UserRole.ROLE_USER);

        requestWithoutDocumentation(request, testProductId, ownerToken)
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", notNullValue())
                .body("name", equalTo(request.name()))
                .body("quantity", notNullValue())
                .body("quantity", equalTo(request.quantity().intValue()));
    }

    @Test
    @DisplayName("옵션 생성 요청 실패 테스트 - 유효성 검증 에러(400 Bad Request)")
    public void Option_Create_Failure_Invalid_Request() {
        Long testProductId = this.testProducts.get(UserRole.ROLE_USER).id();
        String longText = "a".repeat(256); // 256자 이상의 긴 문자열
        Stream.of(
                new OptionCreateRequest(null, 1000L), // 이름이 null인 경우
                new OptionCreateRequest("", 1000L), // 이름이 빈 문자열인 경우
                new OptionCreateRequest("Valid Name", null), // 수량이 null인 경우
                new OptionCreateRequest("Valid Name", -100L), // 수량이 음수인 경우
                new OptionCreateRequest("Valid Name", 0L), // 수량이 0인 경우
                new OptionCreateRequest("Valid Name", 1_00_000_000L), // 수량이 1억 이상인 경우
                new OptionCreateRequest(longText, 1000L), // 이름이 너무 긴 경우
                new OptionCreateRequest("Invalid Name<>", 1000L) // 이름에 유효하지 않은 문자가 포함된 경우
        ).forEach(request ->
                requestWithoutDocumentation(request, testProductId, this.adminToken)
                        .statusCode(400)
                        .body("status", notNullValue())
                        .body("status", equalTo(400))
                        .body("validationErrors", notNullValue())
        );
    }

    @Test
    @DisplayName("옵션 생성 요청 실패 테스트 - 소유하지 않은 일반 사용자가 옵션 생성 요청(403 Forbidden)")
    public void Option_Create_Failure_Not_Owner() {
        // 일반 사용자가 자신이 소유하지 않은 상품에 옵션 생성 요청 시도
        OptionCreateRequest request = new OptionCreateRequest("Test Create Option", 1000L);
        Long testProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        String unauthorizedToken = this.testUserTokens.get(UserRole.ROLE_USER);

        requestWithoutDocumentation(request, testProductId, unauthorizedToken)
                .statusCode(403)
                .body("status", notNullValue())
                .body("status", equalTo(403));

        // 게스트 권한으로 요청 시도
        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(getRequestUrl(), testProductId)
                .then()
                .statusCode(403)
                .body("status", notNullValue())
                .body("status", equalTo(403));
    }

    @Test
    @DisplayName("옵션 생성 요청 실패 테스트 - 존재하지 않는 상품에 옵션 생성 요청(404 Not Found)")
    public void Option_Create_Failure_Non_Existent_Product() {
        OptionCreateRequest request = new OptionCreateRequest("Test Create Option", 1000L);
        Long nonExistentProductId = 9999L; // 존재하지 않는 상품 ID

        requestWithoutDocumentation(request, nonExistentProductId, this.adminToken)
                .statusCode(404)
                .body("status", notNullValue())
                .body("status", equalTo(404));
    }

    @Test
    @DisplayName("옵션 생성 요청 실패 테스트 - 같은 이름의 옵션이 이미 존재하는 경우(409 Conflict)")
    public void Option_Create_Failure_Already_Exists() {
        OptionCreateRequest request = new OptionCreateRequest("Test Create Option", 1000L);
        Long testProductId = this.testProducts.get(UserRole.ROLE_USER).id();
        // 먼저 옵션을 생성
        requestWithoutDocumentation(request, testProductId, this.adminToken)
                .statusCode(201);

        // 같은 이름의 옵션을 다시 생성 시도
        requestWithoutDocumentation(request, testProductId, this.adminToken)
                .statusCode(409)
                .body("status", notNullValue())
                .body("status", equalTo(409));
    }
}
