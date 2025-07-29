package gift.Test.e2e.order;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.dto.order.OrderCreateRequest;
import gift.dto.order.OrderResponse;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationContextProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class OrderDeleteTest extends AbstractOrderTest {
    Map<UserRole, OrderResponse> testOrders;

    @BeforeEach
    @Override
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);
        this.testOrders = new HashMap<>();
        Stream.of(
                UserRole.ROLE_ADMIN,
                UserRole.ROLE_USER
        ).forEach(role -> {
            var option = this.testOptions.get(role);
            OrderResponse response =  RestAssured.given()
                    .contentType("application/json")
                    .header(AUTH_HEADER_KEY, this.testUserTokens.get(role))
                    .body(new OrderCreateRequest(
                            option.id(), 1, "Happy Birthday!"
                    ))
                    .when()
                    .post(getRequestUrl())
                    .then()
                    .statusCode(201)
                    .extract()
                    .as(OrderResponse.class);
            this.testOrders.put(role, response);
        });
    }


    @Test
    @DisplayName("주문 삭제 성공 테스트")
    public void Order_Delete_Success() {
        // 관리자 권한으로 주문 삭제
        Long orderId = this.testOrders.get(UserRole.ROLE_ADMIN).id();
        RestAssured.given(this.spec)


                .filter(document("주문 삭제 성공",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Order")
                            .summary("주문 삭제 API")
                            .description("주문을 삭제하고 204 No Content 응답을 반환합니다.")
                            .pathParameters(
                                    parameterWithName("id").description("삭제할 주문 ID")
                            )
                            .requestHeaders(AUTHENTICATE_HEADERS)
                            .build()
                )))
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN))
                .when()
                .delete(getRequestUrl() + "/{id}", orderId)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("주문 삭제 실패 테스트 - 권한 없음(Unauthorized)")
    public void Order_Delete_Unauthorized() {
        // 사용자 권한으로 주문 삭제 시도
        Long orderId = this.testOrders.get(UserRole.ROLE_USER).id(); // 자신의 주문 ID 사용
        RestAssured.given()
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER))
                .when()
                .delete(getRequestUrl() + "/{id}", orderId)
                .then()
                .statusCode(403); // Forbidden 응답 확인
    }
}
