package gift.Test.e2e.option;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.dto.option.OptionCreateRequest;
import gift.dto.option.OptionResponse;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class OptionDeleteTest extends AbstractOptionTest {

    private static final ParameterDescriptor[] OPTION_DELETE_PATH_PARAMETERS = {
            parameterWithName("productId").description("옵션이 속한 제품 ID"),
            parameterWithName("id").description("수정할 옵션 ID")
    };

    List<OptionResponse> testOptions;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);
        Long productId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        this.testOptions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            OptionCreateRequest request = new OptionCreateRequest("옵션 " + (i + 1), 1000L + (i * 100));
            OptionResponse response = createOptionToProduct(request, productId,this.adminToken);
            this.testOptions.add(response);
        }
    }

    private ValidatableResponse deleteWithoutDocumentation(Long productId, Long optionId, String token) {
        String url = getRequestUrl() + "/{id}";
        return RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, token)
                .when()
                .delete(url, productId, optionId)
                .then();
    }

    @Test
    @DisplayName("옵션 단건 삭제 성공 테스트")
    public void Option_Delete_Success() {
        Long productId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        Long optionId = this.testOptions.getFirst().id(); // 첫 번째 옵션 ID 사용
        String url = getRequestUrl() + "/{id}";

        RestAssured.given(this.spec)
                .filter(document("옵션 단건 삭제 성공",
                    resource(
                        ResourceSnippetParameters.builder()
                        .tag("Option")
                        .summary("옵션 단건 삭제")
                        .description("Id에 해당하는 옵션을 삭제합니다.")
                        .pathParameters(OPTION_DELETE_PATH_PARAMETERS)
                        .requestHeaders(AUTHENTICATE_HEADERS)
                        .build()
                )))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                .when()
                .delete(url, productId, optionId)
                .then()
                .statusCode(204); // No Content 응답 확인

        // 삭제 후 옵션이 존재하지 않는지 확인
        RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                .when()
                .get(url, productId, optionId)
                .then()
                .statusCode(404); // 옵션이 존재하는지 확인
    }

    @Test
    @DisplayName("옵션 단건 삭제 실패 테스트: 마지막 옵션 삭제(400 Bad Request)")
    public void Option_Delete_Failure_LastOption() {
        Long productId = this.testProducts.get(UserRole.ROLE_USER).id();

        var options = RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                .when()
                .get(getRequestUrl(), productId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("contents", OptionResponse.class);

        for (int i=0; i < options.size() - 1; i++) {
            Long optionId = options.get(i).id();
            deleteWithoutDocumentation(productId, optionId, this.adminToken)
                    .statusCode(204); // No Content 응답 확인
        }

        Long lastOptionId = options.getLast().id();

        deleteWithoutDocumentation(productId, lastOptionId, this.adminToken)
                .statusCode(400); // Bad Request 응답 확인
    }



    @Test
    @DisplayName("옵션 단건 삭제 실패 테스트 - 권한 없는 사용자(403 Forbidden)")
    public void Option_Delete_Failure_NoPermission() {
        Long productId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        Long optionId = this.testOptions.getFirst().id(); // 첫 번째 옵션 ID 사용
        deleteWithoutDocumentation(productId, optionId, this.testUserTokens.get(UserRole.ROLE_USER))
                .statusCode(403); // Forbidden 응답 확인
    }

    @Test
    @DisplayName("옵션 단건 삭제 실패 테스트 - 존재하지 않는 옵션 ID(404 Not Found)")
    public void Option_Delete_Failure_NotFound() {
        Long productId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        Long nonExistentOptionId = 999L; // 존재하지 않는 옵션 ID

        deleteWithoutDocumentation(productId, nonExistentOptionId, this.adminToken)
                .statusCode(404); // Not Found 응답 확인
    }
}
