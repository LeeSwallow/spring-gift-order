package gift.Test.e2e.wishlist;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.dto.wishlist.UpdateWishedProductRequest;
import gift.dto.wishlist.WishedProductPatchRequest;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class WishListUpdateTest extends AbstractWishlistTest {

    private static final FieldDescriptor[] WISHLIST_UPDATE_REQUEST    = {
        fieldWithPath("quantity").description("수정할 제품의 수량").type(JsonFieldType.NUMBER).optional()
    };

    private static final FieldDescriptor[] WISHLIST_PATCH_REQUEST = {
          fieldWithPath("amount").description("수정할 제품 수량의 크기(음수 가능)").type(JsonFieldType.NUMBER)
    };

    static final FieldDescriptor[] PRODUCT_RESPONSE = {
            fieldWithPath("id").description("위시리스트 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("productId").description("위시리스트에 추가된 제품 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("name").description("제품 이름").type(JsonFieldType.STRING),
            fieldWithPath("price").description("제품 가격").type(JsonFieldType.NUMBER),
            fieldWithPath("imageUrl").description("제품 이미지 URL").type(JsonFieldType.STRING),
            fieldWithPath("quantity").description("위시리스트에 추가된 제품의 수량").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("subtotal").description("위시리스트에 추가된 제품의 총액").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("createdAt").description("제품 생성 시간").type(JsonFieldType.STRING),
            fieldWithPath("updatedAt").description("제품 수정 시간").type(JsonFieldType.STRING)
    };


    @Test
    @DisplayName("위시리스트 제품 수량 수정 성공 테스트")
    public void WishList_Update_Success() {
        // 위시리스트에 제품을 추가한 후, 해당 제품의 수량을 수정하는 테스트

        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 1);
        UpdateWishedProductRequest request = new UpdateWishedProductRequest(5);
        RestAssured.given(this.spec)
                .filter(document("위시리스트 제품 수정 성공",
                        resource(
                            ResourceSnippetParameters.builder()
                                .tag("Wishlist")
                                .summary("위시리스트 제품 수량 수정 API")
                                .description("위시리스트에 추가된 제품의 수량을 수정합니다.")
                                .requestFields(WISHLIST_UPDATE_REQUEST)
                                .pathParameters(
                                    parameterWithName("id").description("수정할 위시리스트 제품 ID")
                                )
                                .responseFields(PRODUCT_RESPONSE)
                                .build()
                )))
                .contentType("application/json")
                .body(request)
                .header(AUTH_HEADER_KEY, this.testToken) // 관리자 권한으로 요청
                .put(getRequestUrl() + "/{id}", res.id())
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("id", equalTo(res.id().intValue()))
                .body("productId", notNullValue())
                .body("productId", equalTo(productId.intValue()))
                .body("name", notNullValue())
                .body("name", equalTo(this.testProducts.getFirst().name()))
                .body("price", notNullValue())
                .body("price", equalTo(this.testProducts.getFirst().price().intValue()))
                .body("imageUrl", notNullValue())
                .body("imageUrl", equalTo(this.testProducts.getFirst().imageUrl()))
                .body("quantity", notNullValue())
                .body("quantity", equalTo(5));
    }

    @Test
    @DisplayName("위시리스트 제품 수량 수정 실패 테스트: 유효성 검사 실패(400 Bad Request)")
    public void WishList_Update_Failure_Validation() {
        // 위시리스트에 제품을 추가한 후, 해당 제품의 수량을 수정하는 테스트
        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 1);

        List<UpdateWishedProductRequest> requests = List.of(
                new UpdateWishedProductRequest(-1), // 음수 수량
                new UpdateWishedProductRequest(null) // null 수량
        );

        requests.forEach(request ->
                RestAssured.given()
                        .contentType("application/json")
                        .body(request)
                        .header(AUTH_HEADER_KEY, this.testToken) // 관리자 권한으로 요청
                        .put(getRequestUrl() + "/{id}", res.id())
                        .then()
                        .statusCode(400));
    }

    @Test
    @DisplayName("위시리스트 제품 수량 수정 실패 테스트: 권한이 없는 경우(403 Forbidden)")
    public void WishList_Update_Failure_Unauthorized() {
        // 위시리스트에 제품을 추가한 후, 해당 제품의 수량을 수정하는 테스트
        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 1);
        UpdateWishedProductRequest request = new UpdateWishedProductRequest(5);
        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .put(getRequestUrl() + "/{id}", res.id())
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("위시리스트 제품 수량 증가 성공 테스트: 수량을 증가시키는 요청")
    public void WishList_Patch_Success() {
        // 위시리스트에 제품을 추가한 후, 해당 제품의 수량을 증가시키는 테스트

        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 1);
        WishedProductPatchRequest request = new WishedProductPatchRequest(1);
        RestAssured.given(this.spec)
                .filter(document("위시리스트 제품 수정 성공 - 증가",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Wishlist")
                            .summary("위시리스트 제품 수량 수정 API")
                            .description("위시리스트에 추가된 제품의 수량을 증가시킵니다. 수량이 감소하는 경우에도 사용됩니다." +
                                    " 수량이 0으로 감소되면 해당 제품은 위시리스트에서 제거됩니다.(204 No Content 반환)")
                            .requestFields(WISHLIST_PATCH_REQUEST)
                            .pathParameters(
                                parameterWithName("id").description("수정할 위시리스트 제품 ID")
                            )
                            .responseFields(PRODUCT_RESPONSE)
                            .build()
                )))
                .contentType("application/json")
                .body(request)
                .header(AUTH_HEADER_KEY, this.testToken) // 관리자 권한으로 요청
                .patch(getRequestUrl() + "/{id}", res.id())
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("productId", equalTo(productId.intValue()))
                .body("name", notNullValue())
                .body("name", equalTo(this.testProducts.getFirst().name()))
                .body("price", notNullValue())
                .body("price", equalTo(this.testProducts.getFirst().price().intValue()))
                .body("imageUrl", notNullValue())
                .body("imageUrl", equalTo(this.testProducts.getFirst().imageUrl()))
                .body("quantity", notNullValue())
                .body("quantity", equalTo(2));
    }

    @Test
    @DisplayName("위시리스트 제품 수량 감소 성공 테스트: quantity 0으로 감소됬을 때 204 반환")
    public void WishList_Patch_Decrease_Success() {

        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 1);
        WishedProductPatchRequest request = new WishedProductPatchRequest(-1); // 수량을 감소시키는 요청
        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .header(AUTH_HEADER_KEY, this.testToken) // 관리자 권한으로 요청
                .patch(getRequestUrl() + "/{id}", res.id())
                .then()
                .statusCode(204); // 수량이 0으로 감소되었으므로 204 No Content 반환
    }

    @Test
    @DisplayName("위시리스트 제품 수량 수정 실패 테스트: 유효성 검사 실패(400 Bad Request)")
    public void WishList_Patch_Failure_Validation() {
        // 위시리스트에 제품을 추가한 후, 해당 제품의 수량을 수정하는 테스트
        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 1);
        List<WishedProductPatchRequest> requests = List.of(
                new WishedProductPatchRequest(null) // null 수량
        );

        requests.forEach(request ->
            RestAssured.given()
                    .contentType("application/json")
                    .body(request)
                    .header(AUTH_HEADER_KEY, this.testToken) // 관리자 권한으로 요청
                    .patch(getRequestUrl() + "/{id}", res.id())
                    .then()
                    .statusCode(400));
    }
    @Test
    @DisplayName("위시리스트 제품 수량 수정 실패 테스트: 권한이 없는 경우(403 Forbidden)")
    public void WishList_Patch_Failure_Unauthorized() {
        // 위시리스트에 제품을 추가한 후, 해당 제품의 수량을 수정하는 테스트
        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 1);
        WishedProductPatchRequest request = new WishedProductPatchRequest(1); // 수량을 증가시키는 요청
        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .patch(getRequestUrl() + "/{id}", res.id())
                .then()
                .statusCode(403);
    }
}
