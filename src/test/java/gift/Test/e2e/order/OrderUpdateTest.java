package gift.Test.e2e.order;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.dto.order.OrderCreateRequest;
import gift.dto.order.OrderResponse;
import gift.dto.order.OrderUpdateRequest;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class OrderUpdateTest extends AbstractOrderTest {
    private static final FieldDescriptor[] ORDER_UPDATE_REQUEST = {
            fieldWithPath("quantity").description("수정할 주문 수량").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("totalPrice").description("수정할 주문 총 금액").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("message").description("수정할 주문 메시지").type(JsonFieldType.STRING).optional()
    };

    private final FieldDescriptor[] ORDER_UPDATE_RESPONSE = {
            fieldWithPath("id").description("주문 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("optionId").description("주문한 옵션 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("quantity").description("주문 수량").type(JsonFieldType.NUMBER),
            fieldWithPath("message").description("주문 메시지").type(JsonFieldType.STRING).optional(),
            fieldWithPath("totalPrice").description("총 주문 금액").type(JsonFieldType.NUMBER),
            fieldWithPath("orderDateTime").description("주문 생성 시간").type(JsonFieldType.STRING)
    };

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

    private ValidatableResponse updateOrderWithoutDocumentation(
            Long orderId, OrderUpdateRequest request, String token
    ) {
        return RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, token)
                .body(request)
                .when()
                .put(getRequestUrl() + "/{id}", orderId)
                .then();
    }

    @Test
    @DisplayName("주문 수정 요청 성공 테스트")
    public void Order_Update_Success() {
        OrderResponse testOrder = this.testOrders.get(UserRole.ROLE_ADMIN);
        var request = new OrderUpdateRequest(
                10, 10000L, "Happy Birthday Updated!"
        );

        RestAssured.given(this.spec)
                .filter(document("주문 수정 성공",
                        resource(
                            ResourceSnippetParameters.builder()
                            .tag("Order")
                            .summary("주문 수정 API")
                            .description("주문을 수정합니다. 주문 ID와 수정할 필드를 포함합니다.")
                            .pathParameters(
                                    parameterWithName("id").description("수정할 주문 ID")
                            )
                            .requestHeaders(AUTHENTICATE_HEADERS)
                            .requestFields(ORDER_UPDATE_REQUEST)
                            .responseFields(ORDER_UPDATE_RESPONSE)
                            .build()
                )))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_ADMIN))
                .body(request)
                .when()
                .put(getRequestUrl() + "/{id}", testOrder.id())
                .then()
                .statusCode(200)
                .body("id", equalTo(testOrder.id().intValue()))
                .body("optionId", equalTo(testOrder.optionId().intValue()))
                .body("quantity", equalTo(request.quantity()))
                .body("message", equalTo(request.message()))
                .body("totalPrice", equalTo(request.totalPrice().intValue()))
                .body("orderDateTime", notNullValue());
    }

    @Test
    @DisplayName("주문 수정 성공 테스트 - USER 권한으로 message 필드만 수정")
    public void Order_Update_Success_User() {
        OrderResponse testOrder = this.testOrders.get(UserRole.ROLE_USER);
        var request = new OrderUpdateRequest(
                null, null, "Happy Birthday Updated by User!"
        );

        updateOrderWithoutDocumentation(testOrder.id(), request, this.testUserTokens.get(UserRole.ROLE_USER))
                .statusCode(200)
                .body("id", equalTo(testOrder.id().intValue()))
                .body("optionId", equalTo(testOrder.optionId().intValue()))
                .body("quantity", equalTo(testOrder.quantity()))
                .body("message", equalTo(request.message()))
                .body("totalPrice", equalTo(testOrder.totalPrice().intValue()))
                .body("orderDateTime", notNullValue());
    }

    @Test
    @DisplayName("주문 수정 실패 테스트 - 유효성 검증 에러(400 Bad Request)")
    public void Order_Update_Fail_Validation_Error() {
        String longText = "A".repeat(500); // 255자를 초과하는 메시지
        Stream.of(
                new OrderUpdateRequest(1, 10000L, longText), // 메시지가 너무 긴 경우
                new OrderUpdateRequest(0, 100L, null), // 수량이 0인 경우
                new OrderUpdateRequest(null, -1L, null) // 총가격이 음수인 경우
        ).forEach(request ->
            updateOrderWithoutDocumentation(this.testOrders.get(UserRole.ROLE_ADMIN).id(), request, this.testUserTokens.get(UserRole.ROLE_ADMIN))
                    .statusCode(400)
                    .body("validationErrors", notNullValue())
        );
    }

    @Test
    @DisplayName("주문 수정 실패 테스트 - 권한이 없는 경우(403 Forbidden)")
    public void Order_Update_Fail_Forbidden() {
        OrderResponse testOrder = this.testOrders.get(UserRole.ROLE_USER);
        var request = new OrderUpdateRequest(5, 5000L, "Happy Birthday Updated!");

        updateOrderWithoutDocumentation(testOrder.id(), request, this.testUserTokens.get(UserRole.ROLE_USER))
                .statusCode(403)
                .body("status", equalTo(403));
    }

    @Test
    @DisplayName("주문 수정 실패 테스트 - 주문이 존재하지 않는 경우(404 Not Found)")
    public void Order_Update_Fail_Not_Found() {
        Long nonExistentOrderId = 999L; // 존재하지 않는 주문 ID
        var request = new OrderUpdateRequest(null, null, "Happy Birthday Updated!");
        Long notUsersOrderId = this.testOrders.get(UserRole.ROLE_ADMIN).id(); // 관리자 권한으로 생성된 주문 ID

        updateOrderWithoutDocumentation(nonExistentOrderId, request, this.testUserTokens.get(UserRole.ROLE_ADMIN))
                .statusCode(404)
                .body("status", equalTo(404));

        updateOrderWithoutDocumentation(notUsersOrderId, request, this.testUserTokens.get(UserRole.ROLE_USER))
                .statusCode(404)
                .body("status", equalTo(404));
    }

}
