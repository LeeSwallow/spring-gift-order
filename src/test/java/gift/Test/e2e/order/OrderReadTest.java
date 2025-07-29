package gift.Test.e2e.order;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.dto.order.OrderCreateRequest;
import gift.dto.order.OrderResponse;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class OrderReadTest extends AbstractOrderTest {
    static final FieldDescriptor[] ORDER_READ_RESPONSE = {
            fieldWithPath("id").description("주문 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("optionId").description("주문한 옵션 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("quantity").description("주문 수량").type(JsonFieldType.NUMBER),
            fieldWithPath("message").description("주문 메시지").type(JsonFieldType.STRING),
            fieldWithPath("totalPrice").description("총 주문 금액").type(JsonFieldType.NUMBER),
            fieldWithPath("orderDateTime").description("주문 생성 시간").type(JsonFieldType.STRING)
    };

    static final FieldDescriptor[] ORDER_READ_PAGE_RESPONSE = concat(BASE_PAGINATION_FIELDS, new FieldDescriptor[]{
            fieldWithPath("contents[]").description("주문 목록").type(JsonFieldType.ARRAY),
            fieldWithPath("contents[].id").description("주문 ID").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].optionId").description("주문한 옵션 ID").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].quantity").description("주문 수량").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].message").description("주문 메시지").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].totalPrice").description("총 주문 금액").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].orderDateTime").description("주문 생성 시간").type(JsonFieldType.STRING).optional()
    });

    private List<OrderResponse> orderResponse;

    private ValidatableResponse findAllWithoutDocumentation(int page, int size, String sort) {
        return RestAssured.given()
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort)
                .when()
                .get(getRequestUrl())
                .then();
    }


    @BeforeEach
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);
        orderResponse = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            OrderCreateRequest request = new OrderCreateRequest(
                    this.testOptions.get(UserRole.ROLE_USER).id(),
                    1 + i,
                    "Happy Birthday " + (i + 1)
            );
            OrderResponse response = RestAssured.given()
                    .contentType("application/json")
                    .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER))
                    .body(request)
                    .when()
                    .post(getRequestUrl())
                    .as(OrderResponse.class);
            orderResponse.add(response);
        }
    }

    @Test
    @DisplayName("전체 주문 조회 성공 테스트")
    public void Order_Read_All_Success() {
        RestAssured.given(this.spec)
                .filter(document("주문 전체 조회 성공",
                        resource(
                            ResourceSnippetParameters.builder()
                                .tag("Order")
                                .summary("주문 전체 조회 API")
                                .description("사용자의 모든 주문을 페이지네이션하여 조회합니다.")
                                .pathParameters(PAGE_PARAMETERS)
                                .requestHeaders(AUTHENTICATE_HEADERS)
                                .responseFields(ORDER_READ_PAGE_RESPONSE)
                                .build()
                )))
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER))
                .when()
                .get(getRequestUrl())
                .then()
                .statusCode(200)
                .body("contents", notNullValue())
                .body("contents[0].id", notNullValue())
                .body("contents[0].optionId", notNullValue())
                .body("contents[0].quantity", notNullValue())
                .body("contents[0].message", notNullValue())
                .body("contents[0].totalPrice", notNullValue())
                .body("contents[0].orderDateTime", notNullValue());
    }

    @Test
    @DisplayName("주문 전체 조회 실패 테스트 - 잘못된 query parameter(400 Bad Request)")
    public void Order_Read_All_Fail_Invalid_Query_Parameter() {
        findAllWithoutDocumentation(0, -1, "id,desc") // 페이지 크기가 음수인 경우
                .statusCode(400);
        findAllWithoutDocumentation(-1, 10, "id,desc") // 페이지 번호가 음수인 경우
                .statusCode(400);
        findAllWithoutDocumentation(0, 10, "invalid,sort") // 잘못된 정렬 기준인 경우
                .statusCode(400);
    }


    @Test
    @DisplayName("주문 단건 조회 성공 테스트")
    public void Order_Read_By_Id_Success() {
        OrderResponse testOrder = orderResponse.getFirst();

        RestAssured.given(this.spec)
                .filter(document("주문 단건 조회 성공",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Order")
                            .summary("주문 단건 조회 API")
                            .description("주문 ID로 특정 주문을 조회합니다.")
                            .requestHeaders(AUTHENTICATE_HEADERS)
                            .responseFields(ORDER_READ_RESPONSE)
                            .pathParameters(
                                    parameterWithName("orderId").description("조회할 주문 ID")
                            )
                            .build()
                )))
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER))
                .when()
                .get(getRequestUrl() + "/{orderId}", testOrder.id())
                .then()
                .statusCode(200)
                .body("id", equalTo(testOrder.id().intValue()))
                .body("optionId", equalTo(testOrder.optionId().intValue()))
                .body("quantity", equalTo(testOrder.quantity()))
                .body("message", equalTo(testOrder.message()))
                .body("totalPrice", equalTo(testOrder.totalPrice().intValue()));
    }

    @Test
    @DisplayName("주문 단건 조회 실패 테스트 - 존재하지 않는 주문 ID(404 Not Found)")
    public void Order_Read_By_Id_Fail_Not_Found() {
        Long nonExistentOrderId = -1L;
        RestAssured.given(this.spec)
                .header(AUTH_HEADER_KEY, this.testUserTokens.get(UserRole.ROLE_USER))
                .when()
                .get(getRequestUrl() + "/{orderId}", nonExistentOrderId)
                .then()
                .statusCode(404)
                .body("status", equalTo(404));
    }
}
