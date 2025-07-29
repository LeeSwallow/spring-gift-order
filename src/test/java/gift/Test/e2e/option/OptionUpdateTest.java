package gift.Test.e2e.option;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import gift.dto.option.OptionCreateRequest;
import gift.dto.option.OptionPatchRequest;
import gift.dto.option.OptionResponse;
import gift.dto.option.OptionUpdateRequest;
import gift.entity.type.UserRole;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

public class OptionUpdateTest extends AbstractOptionTest {

    private static final FieldDescriptor[] OPTION_UPDATE_REQUEST = {
            fieldWithPath("name").description("수정할 옵션 이름").type(JsonFieldType.STRING).optional(),
            fieldWithPath("quantity").description("수정할 옵션 수량").type(JsonFieldType.NUMBER).optional()
    };

    private static final FieldDescriptor[] OPTION_PATCH_REQUEST = {
            fieldWithPath("amount").description("증감할 옵션 수량").type(JsonFieldType.NUMBER),
    };

    private static final ParameterDescriptor[] OPTION_UPDATE_PATH_PARAMETERS = {
            parameterWithName("productId").description("옵션이 속한 제품 ID"),
            parameterWithName("id").description("수정할 옵션 ID")
    };

    static final FieldDescriptor[] OPTION_UPDATE_RESPONSE = {
            fieldWithPath("id").description("옵션 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("name").description("옵션 이름").type(JsonFieldType.STRING),
            fieldWithPath("quantity").description("옵션 수량").type(JsonFieldType.NUMBER),
            fieldWithPath("createdAt").description("옵션 생성 시간").type(JsonFieldType.STRING),
            fieldWithPath("updatedAt").description("옵션 수정 시간").type(JsonFieldType.STRING)
    };


    private Map<UserRole, OptionResponse> testOptions;

    private ValidatableResponse updateWithoutDocumentation(
            Long productId, Long optionId, OptionUpdateRequest request, String token
    ) {
        return RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, token)
                .body(request)
                .when()
                .put(getRequestUrl() + "/{id}", productId, optionId)
                .then();
    }

    private ValidatableResponse patchWithoutDocumentation(
            Long productId, Long optionId, OptionPatchRequest request, String token
    ) {
        return RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, token)
                .body(request)
                .when()
                .patch(getRequestUrl() + "/{id}", productId, optionId)
                .then();
    }


    @BeforeEach
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);

        this.testOptions = new HashMap<>();
        this.testProducts.forEach((role, product) -> {
            var createRequest = new OptionCreateRequest("test update option", 10L);
            var option = createOptionToProduct(createRequest, product.id(), this.adminToken);
            this.testOptions.put(role, option);
        });
    }

    @Test
    @DisplayName("옵션 수정 성공 테스트")
    public void update_Option_Success() {
        Long validProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        Long validId = this.testOptions.get(UserRole.ROLE_ADMIN).id();
        OptionCreateRequest request = new OptionCreateRequest("수정된 옵션", 20L);

        RestAssured.given(this.spec)
            .filter(document("옵션 수정 성공",
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Option")
                        .summary("옵션 수정")
                        .description("Id에 해당하는 옵션을 수정합니다.")
                        .requestFields(OPTION_UPDATE_REQUEST)
                        .requestHeaders(AUTHENTICATE_HEADERS)
                        .pathParameters(OPTION_UPDATE_PATH_PARAMETERS)
                        .responseFields(OPTION_UPDATE_RESPONSE)
                        .build()
            )))
            .contentType("application/json")
            .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
            .body(request)
            .when()
            .put(getRequestUrl() + "/{id}", validProductId, validId) // 존재하는 옵션 ID로 변경
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("name", notNullValue())
            .body("name", equalTo(request.name())) // 이름은 요청한 값으로 확인
            .body("quantity", notNullValue())
            .body("quantity", equalTo(request.quantity().intValue())); // 수량은 요청한 값으로 확인
    }

    @Test
    @DisplayName("옵션 수정 성공 테스트 - 특정 필드 누락")
    public void update_Option_Success_With_Partial_Request() {
        Long validProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        Long validId = this.testOptions.get(UserRole.ROLE_ADMIN).id();
        Stream.of(
                new OptionUpdateRequest("수정된 옵션", null),
                new OptionUpdateRequest(null, 20L)
        ).forEach(request ->
            updateWithoutDocumentation(validProductId, validId, request, this.adminToken)
                    .statusCode(200)
                    .body("id", notNullValue())
                    .body("name", notNullValue())
        );
    }

    @Test
    @DisplayName("옵션 수정 성공 테스트 - user_role인 사용자가 자신의 상품 수정")
    public void update_Option_Success_User_Role() {
        String userToken = this.testUserTokens.get(UserRole.ROLE_USER);
        Long validProductId = this.testProducts.get(UserRole.ROLE_USER).id();
        Long validId = this.testOptions.get(UserRole.ROLE_USER).id();
        OptionUpdateRequest request = new OptionUpdateRequest("수정된 옵션", 20L);
        updateWithoutDocumentation(validProductId, validId, request, userToken)
                .statusCode(200)
                .body("id", notNullValue())
                .body("id", equalTo(validId.intValue()))
                .body("name", notNullValue())
                .body("name", equalTo(request.name())) // 이름은 요청한 값으로 확인
                .body("quantity", notNullValue())
                .body("quantity", equalTo(request.quantity().intValue())); // 수량은 요청한 값으로 확인
    }

    @Test
    @DisplayName("옵션 증감 성공 테스트")
    public void update_Option_Increment_Success() {
        Long validProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        var validOption = this.testOptions.get(UserRole.ROLE_ADMIN);
        var request = new OptionPatchRequest(5L); // 수량을 5 증가
        long expectedQuantity = validOption.quantity() + request.amount();
        RestAssured.given(this.spec)
                .filter(document("옵션 증감 성공",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Option")
                            .summary("옵션 증감")
                            .description("Id에 해당하는 옵션의 수량을 증감합니다.")
                            .requestFields(OPTION_PATCH_REQUEST)
                            .requestHeaders(AUTHENTICATE_HEADERS)
                            .pathParameters(OPTION_UPDATE_PATH_PARAMETERS)
                            .responseFields(OPTION_UPDATE_RESPONSE)
                            .build()
                )))
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, this.adminToken) // 관리자 권한으로 요청
                .body(request)
                .when()
                .patch(getRequestUrl() + "/{id}", validProductId, validOption.id()) // 존재하는 옵션 ID로 변경
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("id", equalTo(validOption.id().intValue()))
                .body("name", notNullValue())
                .body("name", equalTo(validOption.name())) // 이름은 기존 값으로 확인
                .body("quantity", notNullValue())
                .body("quantity", equalTo((int) expectedQuantity)); // 수량은 증가된 값으로 확인
    }

    @Test
    @DisplayName("옵션 증감 성공 테스트 - user_role인 사용자가 자신의 상품 증감")
    public void update_Option_Increment_Success_User_Role() {
        String userToken = this.testUserTokens.get(UserRole.ROLE_USER);
        Long validProductId = this.testProducts.get(UserRole.ROLE_USER).id();
        var validOption = this.testOptions.get(UserRole.ROLE_USER);
        var request = new OptionPatchRequest(5L); // 수량을 5 증가
        long expectedQuantity = validOption.quantity() + request.amount();

        patchWithoutDocumentation(validProductId, validOption.id(), request, userToken)
                .statusCode(200)
                .body("id", notNullValue())
                .body("id", equalTo(validOption.id().intValue()))
                .body("name", notNullValue())
                .body("name", equalTo(validOption.name())) // 이름은 기존 값으로 확인
                .body("quantity", notNullValue())
                .body("quantity", equalTo((int) expectedQuantity)); // 수량은 증가된 값으로 확인
    }

    @Test
    @DisplayName("옵션 수정 실패 테스트 - 유효성 검사 실패 시 400 반환")
    public void update_Option_Validation_Failure_Returns_400() {
        Long validProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        Long validId = this.testOptions.get(UserRole.ROLE_ADMIN).id();
        String longText = "a".repeat(256); // 256자 이상 문자열
        Stream.of(
                new OptionUpdateRequest(longText, 20L), // 이름 길이 초과
                new OptionUpdateRequest("<><>", 20L), // 이름에 유효하지 않은 특수문자 포함
                new OptionUpdateRequest("수정된 옵션", -10L), // 수량 음수
                new OptionUpdateRequest("수정된 옵션", 0L), // 수량 0
                new OptionUpdateRequest("수정된 옵션", 100_000_001L) // 수량 1억 초과
        ).forEach(request ->
            updateWithoutDocumentation(validProductId, validId, request, this.adminToken)
                    .statusCode(400)
                    .body("validationErrors", notNullValue())
        );
        Stream.of(
                new OptionPatchRequest(null) // 수량이 null
        ).forEach(request ->
            patchWithoutDocumentation(validProductId, validId, request, this.adminToken)
                    .statusCode(400)
                    .body("validationErrors", notNullValue())
        );
    }

    @Test
    @DisplayName("옵션 수정 실패 테스트 - 결과가 음수 혹은 1억 이상이 되는 경우(400 Forbidden)")
    public void update_Option_Failure_Negative_Result() {
        Long validProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        var validOption = this.testOptions.get(UserRole.ROLE_ADMIN);

        // 수량을 음수로 만드는 요청
        OptionPatchRequest negativeReq = new OptionPatchRequest(validOption.quantity() * -1 -1); // 수량을 음수로 변경
        // 수량을 1억 이상으로 만드는 요청
        OptionPatchRequest overLimitReq = new OptionPatchRequest(100_000_001L); // 수량을 1억 증가

        patchWithoutDocumentation(validProductId, validOption.id(), negativeReq, this.adminToken)
                .statusCode(400);
        patchWithoutDocumentation(validProductId, validOption.id(), overLimitReq, this.adminToken)
                .statusCode(400);
    }
    

    @Test
    @DisplayName("옵션 수정 실패 테스트 - 권한 없는 사용자 요청(403 Forbidden)")
    public void update_Option_Failure_No_Auth() {
        Long adminProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        Long userOptionId = this.testOptions.get(UserRole.ROLE_ADMIN).id();
        OptionUpdateRequest updateReq = new OptionUpdateRequest("수정된 옵션", 20L);
        OptionPatchRequest patchReq = new OptionPatchRequest(5L);

        // 관리자 권한도 아니고 소유자도 아닌 사용자로 요청
        updateWithoutDocumentation(adminProductId, userOptionId, updateReq, this.testUserTokens.get(UserRole.ROLE_USER))
                .statusCode(403);
        patchWithoutDocumentation(adminProductId, userOptionId, patchReq, this.testUserTokens.get(UserRole.ROLE_USER))
                .statusCode(403);
    }

    @Test
    @DisplayName("옵션 수정 실패 테스트 - 존재하지 않는 ID(상품,옵션)로 요청(404 Not Found)")
    public void update_Option_Failure_Not_Found() {
        Long validProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        Long invalidProductId = -1L; // 존재하지 않는 상품 ID
        Long invalidOptionId = -1L; // 존재하지 않는 옵션 ID
        OptionUpdateRequest updateReq = new OptionUpdateRequest("수정된 옵션", 20L);
        OptionPatchRequest patchReq = new OptionPatchRequest(5L);
        // 존재하는 상품 ID로 요청
        updateWithoutDocumentation(invalidProductId, invalidOptionId, updateReq, this.adminToken)
                .statusCode(404);
        patchWithoutDocumentation(invalidProductId, invalidOptionId, patchReq, this.adminToken)
                .statusCode(404);
        // 존재하는 상품 ID로 존재하지 않는 옵션 ID로 요청
        updateWithoutDocumentation(validProductId, invalidOptionId, updateReq, this.adminToken)
                .statusCode(404);
        patchWithoutDocumentation(validProductId, invalidOptionId, patchReq, this.adminToken)
                .statusCode(404);
    }

    @Test
    @DisplayName("옵션 수정 실패 테스트 - 중복되는 이름으로 수정 요청(409 Conflict)")
    public void update_Option_Failure_Duplicate_Name() {
        Long validProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        Long validId = this.testOptions.get(UserRole.ROLE_ADMIN).id();
        // 새로운 옵션 생성
        var option = createOptionToProduct(
                new OptionCreateRequest("test dup option", 10L), validProductId, this.adminToken
        );

        // 중복되는 이름으로 수정 요청
        var request = new OptionUpdateRequest(option.name(), 20L);
        updateWithoutDocumentation(validProductId, validId, request, this.adminToken)
                .statusCode(409);
    }
}