package gift.Test.e2e.wishlist;

import gift.dto.product.ProductResponse;
import gift.dto.wishlist.WishedProductCreateRequest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class WishlistCreateTest extends AbstractWishlistTest {
    static final FieldDescriptor[] WISHLIST_CREATE_REQUEST = {
            fieldWithPath("productId").description("추가할 제품 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("quantity").description("추가할 제품의 수량(기본값 1)").type(JsonFieldType.NUMBER).optional(),
    };

    static final FieldDescriptor[] WISHLIST_CREATE_RESPONSE = {
            fieldWithPath("id").description("위시리스트 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("productId").description("추가된 제품 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("name").description("제품 이름").type(JsonFieldType.STRING),
            fieldWithPath("price").description("제품 가격").type(JsonFieldType.NUMBER),
            fieldWithPath("imageUrl").description("제품 이미지 URL").type(JsonFieldType.STRING),
            fieldWithPath("quantity").description("추가된 제품의 수량").type(JsonFieldType.NUMBER),
            fieldWithPath("subtotal").description("추가된 제품의 총액").type(JsonFieldType.NUMBER),
            fieldWithPath("createdAt").description("제품 생성 시간").type(JsonFieldType.STRING),
            fieldWithPath("updatedAt").description("제품 수정 시간").type(JsonFieldType.STRING)
    };


    @Test
    @DisplayName("위시리스트에 제품 추가 성공 테스트")
    public void Wishlist_Create_Success() {
        // 위시리스트에 제품 추가 성공 테스트
        ProductResponse product = this.testProducts.getFirst();
        WishedProductCreateRequest request = new WishedProductCreateRequest(product.id(), 3);

        RestAssured.given(this.spec)
                .filter(document("위시리스트 제품 추가 성공",
                        requestFields(WISHLIST_CREATE_REQUEST),
                        responseFields(WISHLIST_CREATE_RESPONSE)
                ))
                .contentType("application/json")
                .body(request)
                .header(AUTH_HEADER_KEY, this.testToken) // 관리자 권한으로 요청
                .post(getRequestUrl())
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("productId", notNullValue())
                .body("productId", equalTo(product.id().intValue()))
                .body("name", notNullValue())
                .body("name", equalTo(product.name()))
                .body("price", notNullValue())
                .body("price", equalTo(product.price().intValue()))
                .body("imageUrl", notNullValue())
                .body("imageUrl", equalTo(product.imageUrl()))
                .body("quantity", notNullValue())
                .body("quantity", equalTo(3))
                .body("subtotal", notNullValue())
                .body("subtotal", equalTo(3 * product.price().intValue()));

        WishedProductCreateRequest defaultRequest = new WishedProductCreateRequest(
                this.testProducts.get(2).id(), null);

        RestAssured.given(this.spec)
                .contentType("application/json")
                .body(defaultRequest)
                .header(AUTH_HEADER_KEY, this.testToken) // 관리자 권한으로 요청
                .post(getRequestUrl())
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("quantity", notNullValue())
                .body("quantity", equalTo(1)); // 기본값 1이 설정되어야 함
    }


    @Test
    @DisplayName("위시리스트에 제품 추가 실패 - 유효성 관련 에러(400 Bad Request)")
    public void Wishlist_Create_Failure_MissingFields() {
        List<WishedProductCreateRequest> requests = List.of(
                new WishedProductCreateRequest(null, 2) // 제품 ID 누락
                , new WishedProductCreateRequest(1L, -1) // 수량이 음수인 경우
        );
        requests.forEach(request ->
            RestAssured.given(this.spec)
                    .contentType("application/json")
                    .body(request)
                    .header(AUTH_HEADER_KEY, this.testToken) // 관리자 권한으로 요청
                    .post(getRequestUrl())
                    .then()
                    .statusCode(400));
    }

    @Test
    @DisplayName("위시리스트에 제품 추가 실패 - 권한이 없는 경우(403 Forbidden)")
    public void Wishlist_Create_Failure_Unauthorized() {
        // 위시리스트에 제품 추가 실패 - 권한이 없는 경우
        WishedProductCreateRequest request = new WishedProductCreateRequest(this.testProducts.getFirst().id(), 2);

        RestAssured.given(this.spec)
                .filter(document("위시리스트 제품 추가 실패 - 권한 없음",
                        requestFields(WISHLIST_CREATE_REQUEST),
                        responseFields(ERROR_MESSAGE_FIELDS)
                ))
                .contentType("application/json")
                .body(request) // header 없이 요청
                .post(getRequestUrl())
                .then()
                .statusCode(403);
    }


    @Test
    @DisplayName("위시리스트에 제품 추가 실패 - 제품이 존재하지 않는 경우(404 Not Found)")
    public void Wishlist_Create_Failure_ProductNotFound() {
        // 위시리스트에 제품 추가 실패 - 제품이 존재하지 않는 경우
        WishedProductCreateRequest request = new WishedProductCreateRequest(9999L, 2);

        RestAssured.given(this.spec)
                .filter(document("위시리스트 제품 추가 실패 - 제품 없음",
                        requestFields(WISHLIST_CREATE_REQUEST),
                        responseFields(ERROR_MESSAGE_FIELDS)
                ))
                .contentType("application/json")
                .body(request)
                .header(AUTH_HEADER_KEY, this.testToken) // 관리자 권한으로 요청
                .post(getRequestUrl())
                .then()
                .statusCode(404);
    }
}

