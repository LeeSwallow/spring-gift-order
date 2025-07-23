package gift.Test.e2e.product;

import gift.dto.option.OptionCreateRequest;
import gift.dto.product.ProductCreateRequest;
import gift.dto.product.ProductResponse;
import gift.entity.UserRole;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class ProductCreateTest extends AbstractProductTest {

    private final FieldDescriptor[] PRODUCT_CREATE_REQUEST = {
            fieldWithPath("name").description("제품 이름('카카오'가 포함될 시 MD 이상의 권한 필요)").type(JsonFieldType.STRING),
            fieldWithPath("price").description("제품 가격").type(JsonFieldType.NUMBER),
            fieldWithPath("imageUrl").description("제품 이미지 URL").type(JsonFieldType.STRING),
            fieldWithPath("options[]").description("제품 옵션 목록").type(JsonFieldType.ARRAY),
            fieldWithPath("options[].name").description("옵션 이름").type(JsonFieldType.STRING).optional(),
            fieldWithPath("options[].quantity").description("옵션 수량").type(JsonFieldType.NUMBER).optional()
    };

    private final FieldDescriptor[] PRODUCT_CREATE_RESPONSE = {
            fieldWithPath("id").description("제품 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("name").description("제품 이름").type(JsonFieldType.STRING),
            fieldWithPath("price").description("제품 가격").type(JsonFieldType.NUMBER),
            fieldWithPath("imageUrl").description("제품 이미지 URL").type(JsonFieldType.STRING),
            fieldWithPath("createdAt").description("제품 생성 시간").type(JsonFieldType.STRING),
            fieldWithPath("updatedAt").description("제품 수정 시간").type(JsonFieldType.STRING)
    };

    @Test
    @DisplayName("제품 생성 성공 테스트")
    public void Product_Create_Success() {
        var options = List.of(
                new OptionCreateRequest("옵션1", 10L),
                new OptionCreateRequest("옵션2", 20L)
        );
        ProductCreateRequest request =
                new ProductCreateRequest("새로운 제품", 1000L, "이미지 URL", options);
        ProductResponse response = RestAssured.given(this.spec)
                .filter(document("상품 생성 성공",
                        requestFields(PRODUCT_CREATE_REQUEST),
                        requestHeaders(AUTHENTICATE_HEADERS),
                        responseFields(PRODUCT_CREATE_RESPONSE)
                ))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                .body(request)
                .when()
                .post(getRequestUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("name", notNullValue())
                .body("price", notNullValue())
                .body("imageUrl", notNullValue())
                .extract()
                .as(ProductResponse.class);
        this.testProducts.add(response);
    }

    @Test
    @DisplayName("제품 생성 성공 - MD 권한으로 카카오 제품 생성 테스트")
    public void Product_Create_Success_MD() {
        var options = List.of(
                new OptionCreateRequest("옵션1", 10L),
                new OptionCreateRequest("옵션2", 20L)
        );
        ProductCreateRequest request = new ProductCreateRequest("카카오 제품", 1000L, "이미지 URL", options);

        ProductResponse response = RestAssured.given(this.spec)
                .filter(document("상품 생성 성공 - MD 권한",
                        requestFields(PRODUCT_CREATE_REQUEST),
                        requestHeaders(AUTHENTICATE_HEADERS),
                        responseFields(PRODUCT_CREATE_RESPONSE)
                ))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_MD)) // MD 권한으로 요청
                .body(request)
                .when()
                .post(getRequestUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("name", equalTo("카카오 제품"))
                .body("price", equalTo(1000))
                .body("imageUrl", equalTo("이미지 URL"))
                .extract()
                .as(ProductResponse.class);
        this.testProducts.add(response);
    }

    @Test
    @DisplayName("제품 생성 실패 - 필수 필드 누락 테스트 (400 Bad Request)")
    public void Product_Create_Failure_MissingFields() {
        var options = List.of(
                new OptionCreateRequest("옵션1", 10L),
                new OptionCreateRequest("옵션2", 20L)
        );
        List<ProductCreateRequest> requests = List.of(
                new ProductCreateRequest(null, 1000L, "이미지 URL", options),
                new ProductCreateRequest("제품 이름", null, "이미지 URL", options),
                new ProductCreateRequest("제품 이름", 1000L, null, options),
                new ProductCreateRequest("", 1000L, "이미지 URL", options),
                new ProductCreateRequest("제품 이름", 1000L, "이미지 URL", null)
        );

        requests.forEach(request ->
            RestAssured.given()
                    .contentType("application/json")
                    .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                    .body(request)
                    .when()
                    .post(getRequestUrl())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("title", notNullValue())
                    .body("detail", notNullValue())
                    .body("validationErrors", notNullValue())
        );
    }

    @Test
    @DisplayName("제품 생성 실패 - 유효성 검사 실패 테스트- USER 권한 상품 이름에 카카오 추가 (400 Bad Request)")
    public void Product_Create_Failure_ValidationError() {
        var options = List.of(
                new OptionCreateRequest("옵션1", 10L),
                new OptionCreateRequest("옵션2", 20L)
        );
        ProductCreateRequest request = new ProductCreateRequest("카카오 제품", 1000L, "이미지 URL", options);

        RestAssured.given(this.spec)
                .filter(document("상품 생성 실패 - 유효성 검사 실패",
                        requestFields(PRODUCT_CREATE_REQUEST),
                        requestHeaders(AUTHENTICATE_HEADERS),
                        responseFields(ERROR_MESSAGE_FIELDS)
                ))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER)) // USER 권한으로 요청
                .body(request)
                .when()
                .post(getRequestUrl())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("title", notNullValue())
                .body("detail", notNullValue())
                .body("validationErrors", notNullValue());
    }


    @Test
    @DisplayName("제품 생성 실패 - 유효성 검증 오류 (400 Bad Request)")
    public void Product_Create_Failure_ValidationError_Case() {
        var options = List.of(
                new OptionCreateRequest("옵션1", 10L),
                new OptionCreateRequest("옵션2", 20L)
        );
        List<ProductCreateRequest> requests = List.of(
                new ProductCreateRequest("테스트 제품", -1000L, "이미지 URL", options), // 가격이 음수
                new ProductCreateRequest("이름이 너무 김 이름이 너무 김 이름이 너무 김 이름이 너무 김", 1000L, "", options), // 이름이 너무 김
                new ProductCreateRequest("<><><>", 1000L, "이미지 URL", options), // 이름에 허용하지 않는 문자 포함
                new ProductCreateRequest("제품 이름", 1000L, "이미지 URL", List.of()) // 옵션이 비어 있음
        );
        requests.forEach(request ->
            RestAssured.given(this.spec)
                    .contentType("application/json")
                    .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                    .body(request)
                    .when()
                    .post(getRequestUrl())
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("title", notNullValue())
                    .body("detail", notNullValue())
                    .body("validationErrors", notNullValue())
        );
    }

    @Test
    @DisplayName("제품 생성 실패 - 권한 없음 테스트 (403 Forbidden)")
    public void Product_Create_Failure_Unauthorized() {
        var options = List.of(
                new OptionCreateRequest("옵션1", 10L),
                new OptionCreateRequest("옵션2", 20L)
        );
        ProductCreateRequest request = new ProductCreateRequest("제품 이름", 1000L, "이미지 URL", options);

        RestAssured.given(this.spec)
                .filter(document("상품 생성 실패 - 권한 없음",
                        requestFields(PRODUCT_CREATE_REQUEST),
                        requestHeaders(AUTHENTICATE_HEADERS),
                        responseFields(ERROR_MESSAGE_FIELDS)
                ))
                .contentType("application/json")
                .body(request)
                .when()
                .post(getRequestUrl())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body("title", notNullValue())
                .body("detail" , notNullValue());
    }
}
