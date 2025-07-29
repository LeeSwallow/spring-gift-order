package gift.Test.e2e.wishlist;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.dto.wishlist.WishedProductResponse;
import io.restassured.RestAssured;
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

public class WishListReadTest extends AbstractWishlistTest {

    static final FieldDescriptor[] PRODUCT_READ_RESPONSE = {
            fieldWithPath("id").description("위시리스트 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("productId").description("위시리스트에 추가된 제품 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("name").description("제품 이름").type(JsonFieldType.STRING),
            fieldWithPath("price").description("제품 가격").type(JsonFieldType.NUMBER),
            fieldWithPath("imageUrl").description("제품 이미지 URL").type(JsonFieldType.STRING),
            fieldWithPath("quantity").description("위시리스트에 추가된 제품의 수량").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("subtotal").description("위시리스트에 추가된 제품의 총액").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("createdAt").description("제품 생성 시간").type(JsonFieldType.STRING).optional(),
            fieldWithPath("updatedAt").description("제품 업데이트 시간").type(JsonFieldType.STRING).optional()
    };

    static final FieldDescriptor[] PRODUCT_READ_PAGE_RESPONSE = concat(BASE_PAGINATION_FIELDS, new FieldDescriptor[]{
            fieldWithPath("totalQuantity").description("위시리스트에 추가된 제품의 총 수량").type(JsonFieldType.NUMBER),
            fieldWithPath("totalPrice").description("위시리스트에 추가된 제품의 총액").type(JsonFieldType.NUMBER),
            fieldWithPath("contents[]").description("제품 목록").type(JsonFieldType.ARRAY),
            fieldWithPath("contents[].id").description("제품 ID").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].name").description("제품 이름").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].price").description("제품 가격").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].imageUrl").description("제품 이미지 URL").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].quantity").description("위시리스트에 추가된 제품의 수량").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].subtotal").description("위시리스트에 추가된 제품의 총액").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].createdAt").description("제품 생성 시간").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].updatedAt").description("제품 업데이트 시간").type(JsonFieldType.STRING).optional()
    });

    @Test
    @DisplayName("위시리스트 전체 조회 성공 테스트")
    public void find_All_Wishlist_Success() {
        // 위시리스트 전체 조회 성공 테스트
        RestAssured.given(this.spec)
                .filter(document("위시리스트 전체 조회 성공",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Wishlist")
                                .summary("위시리스트 전체 조회 API")
                                .description("사용자의 위시리스트에 추가된 모든 제품을 페이지 단위로 조회합니다. " +
                                        " 토큰으로 부터 사용자 정보를 추출하여 해당 사용자의 위시리스트를 조회합니다.")
                                .queryParameters(PAGE_PARAMETERS)
                                .requestHeaders(AUTHENTICATE_HEADERS)
                                .responseFields(PRODUCT_READ_PAGE_RESPONSE)
                                .build()
                )))
                .header(AUTH_HEADER_KEY, this.testToken)
                .when()
                .get(getRequestUrl())
                .then()
                .statusCode(200)
                .body("page", notNullValue())
                .body("size", notNullValue())
                .body("totalElements", notNullValue())
                .body("totalPages", notNullValue())
                .body("totalQuantity", notNullValue())
                .body("totalPrice", notNullValue())
                .body("contents", notNullValue());
    }

    @Test
    @DisplayName("위시리스트 전체 조회 성공 테스트 : 정렬 파라미터 포함")
    public void find_All_Wishlist_Success_With_Page_And_Size() {
        List<WishedProductResponse> res = RestAssured.given()
                .queryParam("sort", "product.price,desc")
                .header(AUTH_HEADER_KEY, this.testToken)
                .when()
                .get(getRequestUrl())
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("contents", WishedProductResponse.class);

        Long prevPrice = Long.MAX_VALUE;
        for (WishedProductResponse product : res) {
            // 가격이 내림차순으로 정렬되었는지 확인
            assertThat(product.price(), lessThanOrEqualTo(prevPrice));
            prevPrice = product.price();
        }
    }


    @Test
    @DisplayName("위시리스트 전체 조회 실패 테스트 : 음수 페이지와 크기 요청 시 (400 Bad Request)")
    public void find_All_Wishlist_Success_Negative_Page_And_Size_Request_Default_Returned() {
        // 위시리스트 전체 조회 실패 테스트 : 잘못된 페이지 요청 시 400 반환
        RestAssured.given()
                .queryParam("page", -1)
                .queryParam("size", -1)
                .header(AUTH_HEADER_KEY, this.testToken)
                .when()
                .get(getRequestUrl())
                .then()
                .statusCode(400)
                .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("위시리스트 전체 조회 실패 테스트 : 권한이 없는 경우(403 Forbidden)")
    public void find_All_Wishlist_Failure_Unauthorized() {
        // 위시리스트 전체 조회 실패 테스트 : 권한이 없는 경우(403 Forbidden)
        RestAssured.given()
                .when()
                .get(getRequestUrl())
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("위시리스트 전체 조회 실패 테스트: 잘못된 정렬 파라미터 요청 시 400 반환(400 Bad Request)")
    public void find_All_Wishlist_Failure_Invalid_Sort_Request_400_Returned() {
        List<String> invalidSortFields = List.of(
                "invalidField", // 존재하지 않는 정렬 기준
                "product.price,invalidDirection", // 잘못된 정렬 방향
                "product.name,asc,desc" // 잘못된 정렬 기준
        );

        invalidSortFields.forEach(sortField ->
            RestAssured.given(this.spec)
                    .header(AUTH_HEADER_KEY, this.testToken)
                    .queryParam("sort", sortField)
                    .when()
                    .get(getRequestUrl())
                    .then()
                    .statusCode(400));
    }

    @Test
    @DisplayName("위시리스트 단건 제품 조회 성공 테스트")
    public void find_Wishlist_Product_Success() {
        // 위시리스트 단건 제품 조회 성공 테스트
        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 3);
        RestAssured.given(this.spec)
                .filter(document("위시리스트 단건 제품 조회 성공",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Wishlist")
                            .summary("위시리스트 단건 제품 조회 API")
                            .description("사용자의 위시리스트에 추가된 특정 제품을 조회합니다. " +
                                    "토큰으로 부터 사용자 정보를 추출하여 해당 사용자의 위시리스트를 조회합니다.")
                            .pathParameters(
                                    parameterWithName("id").description("위시리스트 ID")
                            )
                            .requestHeaders(AUTHENTICATE_HEADERS)
                            .responseFields(PRODUCT_READ_RESPONSE)
                            .build()
                )))
                .header(AUTH_HEADER_KEY, this.testToken)
                .when()
                .get(getRequestUrl() + "/{id}", res.id())
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("productId", notNullValue())
                .body("name", notNullValue())
                .body("price", notNullValue())
                .body("imageUrl", notNullValue())
                .body("quantity", notNullValue())
                .body("subtotal", notNullValue())
                .body("id", equalTo(res.id().intValue()))
                .body("productId", equalTo(productId.intValue()))
                .body("name", equalTo(this.testProducts.getFirst().name()))
                .body("price", equalTo(this.testProducts.getFirst().price().intValue()))
                .body("imageUrl", equalTo(this.testProducts.getFirst().imageUrl()))
                .body("quantity", equalTo(3))
                .body("subtotal", equalTo(this.testProducts.getFirst().price().intValue() * 3));
    }

    @Test
    @DisplayName("위시리스트 단건 제품 조회 실패 테스트 : 존재하지 않는 제품 ID 요청 시 404 반환")
    public void find_Wishlist_Product_Failure_NonExistentId_404_Returned() {
        // 위시리스트 단건 제품 조회 실패 테스트 : 존재하지 않는 제품 ID 요청 시 404 반환
        RestAssured.given()
                .header(AUTH_HEADER_KEY, this.testToken)
                .when()
                .get(getRequestUrl() + "/999999") // 존재하지 않는 ID
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("위시리스트 단건 제품 조회 실패 테스트 : 권한이 없는 경우(403 Forbidden)")
    public void find_Wishlist_Product_Failure_Unauthorized() {
        // 위시리스트 단건 제품 조회 실패 테스트 : 권한이 없는 경우(403 Forbidden)
        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 3);
        RestAssured.given()
                .when()
                .get(getRequestUrl() + "/" + res.id())
                .then()
                .statusCode(403);
    }

}

