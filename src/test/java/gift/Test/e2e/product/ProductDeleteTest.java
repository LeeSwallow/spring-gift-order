package gift.Test.e2e.product;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class ProductDeleteTest extends AbstractProductTest {

    @Test
    @DisplayName("제품 삭제 성공 테스트")
    public void Product_Delete_Success() {
        Long testProductId = this.testProducts.getFirst().id(); // 테스트용 제품 ID 가져오기
        String url = getRequestUrl() + "/{id}";
        RestAssured.given(this.spec)
                .filter(document("상품 삭제 성공",
                        resource(
                            ResourceSnippetParameters.builder()
                                .tag("Product")
                                .summary("제품 삭제 API")
                                .description("제품 ID를 입력받아 해당 제품을 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("삭제할 제품 ID")
                                )
                                .requestHeaders(AUTHENTICATE_HEADERS)
                                .build()
                )))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken)
                .delete(url, testProductId)
                .then()
                .statusCode(204);

        this.testProducts.removeIf(product -> product.id().equals(testProductId));
    }

    @Test
    @DisplayName("제품 삭제 실패 테스트: 존재하지 않는 제품 ID")
    public void Product_Delete_Failure_NonExistentId() {
        String url = getRequestUrl() + "/{id}";
        RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken)
                .delete(url, 9999)
                .then()
                .statusCode(404);
    }
}
