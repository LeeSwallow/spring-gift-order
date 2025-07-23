package gift.Test.e2e.product;

import gift.dto.product.ProductUpdateRequest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class ProductUpdateTest extends AbstractProductTest {

    private static final ParameterDescriptor[] PRODUCT_UPDATE_PATH_PARAMETERS = {
            parameterWithName("id").description("수정할 제품 ID")
    };

    private static final FieldDescriptor[] PRODUCT_UPDATE_REQUEST = {
            fieldWithPath("name").description("수정된 제품 이름").type(JsonFieldType.STRING).optional(),
            fieldWithPath("price").description("수정된 제품 가격").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("imageUrl").description("수정된 제품 이미지 URL").type(JsonFieldType.STRING).optional()
    };

    private static final FieldDescriptor[] PRODUCT_UPDATE_RESPONSE = {
            fieldWithPath("id").description("제품 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("name").description("제품 이름").type(JsonFieldType.STRING),
            fieldWithPath("price").description("제품 가격").type(JsonFieldType.NUMBER),
            fieldWithPath("imageUrl").description("제품 이미지 URL").type(JsonFieldType.STRING),
            fieldWithPath("createdAt").description("제품 생성 시간").type(JsonFieldType.STRING),
            fieldWithPath("updatedAt").description("제품 수정 시간").type(JsonFieldType.STRING)
    };

    @Test
    @DisplayName("제품 수정 성공 테스트")
    public void Product_Update_Success() {
        Long validId = this.testProducts.getFirst().id(); // 테스트용 제품 ID 가져오기
        String url = getBaseUrl() + "/api/products/{id}";
        ProductUpdateRequest request = new ProductUpdateRequest("수정된 제품", 1500L, "수정된 이미지 URL");
        RestAssured.given(this.spec)
                .filter(document("상품 수정 성공",
                        pathParameters(PRODUCT_UPDATE_PATH_PARAMETERS),
                        requestHeaders(AUTHENTICATE_HEADERS),
                        requestFields(PRODUCT_UPDATE_REQUEST),
                        responseFields(PRODUCT_UPDATE_RESPONSE)))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                .body(request)
                .when()
                .put(url, validId) // 존재하는 제품 ID로 변경
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", notNullValue())
                .body("price", notNullValue())
                .body("imageUrl", notNullValue());
    }

    @Test
    @DisplayName("제품 수정 성공 테스트: 특정 필드 누락 가능")
    public void update_Product_Success_With_Partial_Request() {
        String url = getBaseUrl() + "/api/products/{id}"; // 존재하는 제품 ID로 변경
        Long validId = this.testProducts.getFirst().id(); // 테스트용 제품 ID 가져오기
        ProductUpdateRequest request = new ProductUpdateRequest("수정된 제품", null, "수정된 이미지 URL"); // 가격 필드 누락

        RestAssured.given(this.spec)
                .filter(document("상품 수정 성공 - 특정 필드 누락",
                        pathParameters(PRODUCT_UPDATE_PATH_PARAMETERS),
                        requestHeaders(AUTHENTICATE_HEADERS),
                        requestFields(PRODUCT_UPDATE_REQUEST),
                        responseFields(PRODUCT_UPDATE_RESPONSE)))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                .body(request)
                .when()
                .put(url, validId) // 존재하는 제품 ID로 변경
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", notNullValue())
                .body("price", notNullValue())
                .body("imageUrl", notNullValue());
    }


    @Test
    @DisplayName("제품 수정 실패 테스트 : 유효성 검사 실패 시 400 반환")
    public void update_Product_Validation_Failure_Returns_400() {
        String url = getBaseUrl() + "/api/products/{id}";
        Long validId = this.testProducts.getFirst().id(); // 테스트용 제품 ID 가져오기

        List<ProductUpdateRequest> invalidRequests = List.of(
                new ProductUpdateRequest("", 1000L, "이미지 URL"), // 이름 필드 비어있음
                new ProductUpdateRequest("유효한 이름", -100L, "이미지 URL"), // 가격 필드 음수
                new ProductUpdateRequest("유효하지않은 이름<>", 1000L, "") // 이름 필드 유효하지 않음
        );

        for (ProductUpdateRequest request : invalidRequests) {
            RestAssured.given(this.spec)
                    .filter(document("상품 수정 실패 - 유효성 검사 실패",
                            pathParameters(PRODUCT_UPDATE_PATH_PARAMETERS),
                            requestHeaders(AUTHENTICATE_HEADERS),
                            requestFields(PRODUCT_UPDATE_REQUEST),
                            responseFields(ERROR_MESSAGE_FIELDS)))
                    .contentType("application/json")
                    .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                    .body(request)
                    .when()
                    .put(url, validId) // 존재하는 제품 ID로 변경
                    .then()
                    .statusCode(400);
        }
    }
}
