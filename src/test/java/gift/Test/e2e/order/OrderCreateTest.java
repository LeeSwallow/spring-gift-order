package gift.Test.e2e.order;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.dto.order.OrderCreateRequest;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.stream.Stream;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class OrderCreateTest extends AbstractOrderTest {
    private final FieldDescriptor[] ORDER_CREATE_REQUEST = {
            fieldWithPath("optionId").description("주문할 옵션 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("quantity").description("주문 수량").type(JsonFieldType.NUMBER),
            fieldWithPath("message").description("주문 메시지").type(JsonFieldType.STRING).optional()
    };

    private final FieldDescriptor[] ORDER_CREATE_RESPONSE = {
            fieldWithPath("id").description("주문 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("optionId").description("주문한 옵션 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("quantity").description("주문 수량").type(JsonFieldType.NUMBER),
            fieldWithPath("message").description("주문 메시지").type(JsonFieldType.STRING).optional(),
            fieldWithPath("totalPrice").description("총 주문 금액").type(JsonFieldType.NUMBER),
            fieldWithPath("orderDateTime").description("주문 생성 시간").type(JsonFieldType.STRING)
    };

    private ValidatableResponse requestWithoutDocumentation(
            OrderCreateRequest request, String token
    ) {
        return RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, token)
                .body(request)
                .when()
                .post(getRequestUrl())
                .then();
    }



    @Test
    @DisplayName("주문 생성 요청 성공 테스트")
    public void Order_Create_Success() {
        var testOption = this.testOptions.get(UserRole.ROLE_USER);
        var testProduct = this.testProducts.get(UserRole.ROLE_USER);
        var testToken = this.testUserTokens.get(UserRole.ROLE_USER);
        var request = new OrderCreateRequest(testOption.id(), 2, "Happy Birthday!");
        Long testOptionId = this.testOptions.get(UserRole.ROLE_USER).id();
        Long expectTotalPrice = testProduct.price() * request.quantity();

        RestAssured.given(this.spec)
                .filter(document("주문 생성 성공",
                    resource(
                        ResourceSnippetParameters.builder()
                        .tag("Order")
                        .summary("주문 생성 API")
                        .description("주문을 생성합니다. 옵션 ID와 수량, 메시지를 포함합니다.")
                        .requestFields(ORDER_CREATE_REQUEST)
                        .requestHeaders(AUTHENTICATE_HEADERS)
                        .responseFields(ORDER_CREATE_RESPONSE)
                        .build()
                )))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, testToken)
                .body(request)
                .when()
                .post(getRequestUrl())
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("optionId", equalTo(testOptionId.intValue()))
                .body("quantity", equalTo(request.quantity()))
                .body("message", equalTo(request.message()))
                .body("totalPrice", equalTo(expectTotalPrice.intValue()))
                .body("orderDateTime", notNullValue());
    }


    @Test
    @DisplayName("주문 생성 요청 실패 테스트 - 유효성 검증 에러(400 Bad Request)")
    public void Order_Create_Fail_Validation_Error() {
        var testOption = this.testOptions.get(UserRole.ROLE_USER);
        var testToken = this.testUserTokens.get(UserRole.ROLE_USER);
        Stream.of(
                new OrderCreateRequest(null, 2, "Happy Birthday!"), // 옵션 ID가 null
                new OrderCreateRequest(testOption.id(), -1, "Happy Birthday!"), // 수량이 음수
                new OrderCreateRequest(testOption.id(), 0, "Happy Birthday!") // 수량이 0
        ).forEach(request ->
            requestWithoutDocumentation(request, testToken)
                    .statusCode(400)
                    .body("validationErrors", notNullValue())
        );
    }

    @Test
    @DisplayName("주문 생성 요청 실패 테스트 - 인증되지 않은 사용자(403 Forbidden)")
    public void Order_Create_Fail_Unauthorized() {
        var request = new OrderCreateRequest(1L, 2, "Happy Birthday!");

        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when() // guest user 는 주문 불가
                .post(getRequestUrl())
                .then()
                .statusCode(403)
                .body("status", equalTo(403));
    }


    @Test
    @DisplayName("주문 생성 요청 실패 테스트 - 옵션이 존재하지 않는 경우(404 Not Found)")
    public void Order_Create_Fail_Option_Not_Found() {
        var request = new OrderCreateRequest(999L, 2, "Happy Birthday!");
        String testToken = this.testUserTokens.get(UserRole.ROLE_USER);

        requestWithoutDocumentation(request, testToken)
                .statusCode(404);
    }
}
