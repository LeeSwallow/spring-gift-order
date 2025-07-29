package gift.Test.e2e.wishlist;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class WishListDeleteTest extends  AbstractWishlistTest {

    @Test
    @DisplayName("단건 위시리스트 삭제 성공 테스트")
    public void delete_Wishlist_Success() {
        // 단건 위시리스트 삭제 성공 테스트
        Long productId = this.testProducts.getFirst().id();

        // 먼저 위시리스트에 제품을 추가
        var res = addProductToWishlist(productId, 1);

        // 위시리스트에서 제품 삭제 요청
        RestAssured.given(this.spec)
                .filter(document("위시리스트 제품 삭제 성공",
                        resource(
                            ResourceSnippetParameters.builder()
                                .tag("Wishlist")
                                .summary("위시리스트 제품 삭제 API")
                                .description("위시리스트에서 특정 제품을 삭제합니다. 일반 사용자의 경우 자기 자신의 위시리스트만 삭제할 수 있습니다.")
                                .pathParameters(
                                        parameterWithName("id").description("삭제할 위시리스트 ID")
                                )
                                .requestHeaders(AUTHENTICATE_HEADERS)
                                .build()
                )))
                .header(AUTH_HEADER_KEY, this.testToken)
                .delete(getRequestUrl() + "/{id}", res.id())
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("위시리스트 전체 삭제 성공 테스트")
    public void delete_All_Wishlist_Success() {
        // 위시리스트 전체 삭제 성공 테스트
        this.testProducts.forEach(
                product -> addProductToWishlist(product.id(), 1)
        );

        RestAssured.given(this.spec)
                .filter(document("위시리스트 전체 삭제 성공",
                        resource(
                            ResourceSnippetParameters.builder()
                                .tag("Wishlist")
                                .summary("위시리스트 전체 삭제 API")
                                .description("사용자의 위시리스트를 전체 삭제합니다. 토큰으로 부터 사용자를 식별하여 전체 위시리스트를 삭제합니다.")
                                .requestHeaders(AUTHENTICATE_HEADERS)
                                .build()
                )))
                .header(AUTH_HEADER_KEY, this.testToken)
                .delete(getRequestUrl())
                .then()
                .statusCode(204);

        // 위시리스트가 비어있는지 확인
        RestAssured.given()
                .header(AUTH_HEADER_KEY, this.testToken)
                .get(getRequestUrl())
                .then()
                .statusCode(200)
                .body("totalElements", equalTo(0))
                .body("contents", empty());
    }

    @Test
    @DisplayName("위시리스트 삭제 실패 테스트: 존재하지 않는 제품 ID(404 Not Found)")
    public void delete_Wishlist_Failure_NotFound() {
        // 존재하지 않는 제품 ID로 위시리스트 삭제 요청
        Long nonExistentProductId = 999L; // 예시로 존재하지 않는 ID 사용

        RestAssured.given()
                .header(AUTH_HEADER_KEY, this.testToken)
                .delete(getRequestUrl() + "/{productId}", nonExistentProductId)
                .then()
                .statusCode(404);
    }
    @Test
    @DisplayName("위시리스트 삭제 실패 테스트: 권한이 없는 경우(403 Forbidden)")
    public void delete_Wishlist_Failure_Unauthorized() {
        // 위시리스트 삭제 요청 시 권한이 없는 경우
        Long productId = this.testProducts.getFirst().id();
        var res = addProductToWishlist(productId, 1);

        RestAssured.given()
                .delete(getRequestUrl() + "/{productId}", res.id())
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("위시리스트 전체 삭제 실패 테스트: 권한이 없는 경우(403 Forbidden)")
    public void delete_All_Wishlist_Failure_Unauthorized() {
        // 위시리스트 전체 삭제 요청 시 권한이 없는 경우
        RestAssured.given()
                .delete(getRequestUrl())
                .then()
                .statusCode(403);
    }
}