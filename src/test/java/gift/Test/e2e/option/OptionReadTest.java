package gift.Test.e2e.option;

import gift.dto.option.OptionCreateRequest;
import gift.dto.option.OptionResponse;
import gift.entity.UserRole;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class OptionReadTest extends  AbstractOptionTest {
    static final FieldDescriptor[] OPTION_READ_RESPONSE = {
            fieldWithPath("id").description("옵션 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("name").description("옵션 이름").type(JsonFieldType.STRING),
            fieldWithPath("quantity").description("옵션 수량").type(JsonFieldType.NUMBER),
            fieldWithPath("createdAt").description("옵션 생성 시간").type(JsonFieldType.STRING),
            fieldWithPath("updatedAt").description("옵션 수정 시간").type(JsonFieldType.STRING)
    };

    static final FieldDescriptor[] OPTION_READ_PAGE_RESPONSE = concat(BASE_PAGINATION_FIELDS, new FieldDescriptor[]{
            fieldWithPath("contents[]").description("옵션 목록").type(JsonFieldType.ARRAY),
            fieldWithPath("contents[].id").description("옵션 ID").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].name").description("옵션 이름").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].quantity").description("옵션 수량").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].createdAt").description("옵션 생성 시간").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].updatedAt").description("옵션 수정 시간").type(JsonFieldType.STRING).optional()
    });

    private Long testProductId;
    private List<OptionResponse> testOptions;

    private ValidatableResponse findAllWithoutDocumentation(int page, int size, String sort) {
        return RestAssured.given()
                .header(AUTH_HEADER_KEY, this.adminToken)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort)
                .when()
                .get(getRequestUrl(), this.testProductId)
                .then();
    }

    private ValidatableResponse findByIdWithoutDocumentation(Long optionId) {
        return RestAssured.given()
                .header(AUTH_HEADER_KEY, this.adminToken)
                .when()
                .get(getRequestUrl() + "/{optionId}", this.testProductId, optionId)
                .then();
    }

    @BeforeEach
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);
        this.testProductId = this.testProducts.get(UserRole.ROLE_ADMIN).id();
        this.testOptions = new ArrayList<>();
        // 테스트를 위한 옵션 데이터 생성
        for (int i = 0; i < 5; i++) {
            var request = new OptionCreateRequest("옵션 " + i, 10L * (i + 1));
            var res = createOptionToProduct(request, this.testProductId, this.adminToken);
            this.testOptions.add(res);
        }
    }

    @Test
    @DisplayName("옵션 전체 조회 성공 테스트")
    public void find_All_Option_Success() {
        // 옵션 전체 조회 성공 테스트
        RestAssured.given(this.spec)
                .filter(document("옵션 전체 조회 성공",
                        queryParameters(PAGE_PARAMETERS),
                        pathParameters(parameterWithName("productId").description("옵션이 속한 제품 ID")),
                        responseFields(OPTION_READ_PAGE_RESPONSE)))
                .header(AUTH_HEADER_KEY, this.adminToken)
                .when()
                .get(getRequestUrl(), this.testProductId)
                .then()
                .statusCode(200)
                .body("contents", notNullValue())
                .body("page", notNullValue())
                .body("size", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(5))
                .body("totalPages", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("옵션 전체 조회 실패 테스트 - 음수 페이지 크기, 음수 페이지 번호")
    public void find_All_Option_Success_Negative_Page_And_Size() {
        // 음수 페이지와 크기 요청 시 기본값 적용
        findAllWithoutDocumentation(-1, -1, "id,asc")
                .statusCode(400)
                .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("옵션 전체 조회 성공 테스트 - 정렬 기준 검사")
    public void find_All_Option_Success_Sort() {
        // 정렬 기준 검사
        var sortedOptions = findAllWithoutDocumentation(0, 5, "quantity,desc")
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("contents", OptionResponse.class);

        // 수량이 내림차순으로 정렬되었는지 확인
        Long prevQuantity = Long.MAX_VALUE;
        for (OptionResponse option : sortedOptions) {
            assertThat(option.quantity(), lessThanOrEqualTo(prevQuantity));
            prevQuantity = option.quantity();
        }
    }

    @Test
    @DisplayName("전체 옵션 조회 실패 테스트 - 허용되지 않은 정렬 기준(400 Bad Request)")
    public void find_All_Option_Failure_Invalid_Sort() {
        // 허용되지 않은 정렬 기준 요청 시 400 반환
        findAllWithoutDocumentation(0, 5, "invalidField,asc")
                .statusCode(400)
                .body("status", equalTo(400))
                .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("옵션 ID로 조회 성공 테스트")
    public void find_Option_By_Id_Success() {
        // 옵션 ID로 조회 성공 테스트
        var testOption = this.testOptions.getFirst();
        RestAssured.given(this.spec)
                .filter(document("옵션 ID로 조회 성공",
                        pathParameters(
                                parameterWithName("productId").description("옵션이 속한 제품 ID"),
                                parameterWithName("optionId").description("조회할 옵션 ID")
                        ),
                        responseFields(OPTION_READ_RESPONSE)))
                .header(AUTH_HEADER_KEY, this.adminToken)
                .when()
                .get(getRequestUrl() + "/{optionId}", this.testProductId, testOption.id())
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("id", equalTo(testOption.id().intValue()))
                .body("name", notNullValue())
                .body("name", equalTo(testOption.name()))
                .body("quantity", notNullValue())
                .body("quantity", equalTo(testOption.quantity().intValue()));
    }

    @Test
    @DisplayName("옵션 ID로 조회 실패 테스트 - 존재하지 않는 옵션 ID(404 Not Found)")
    public void find_Option_By_Id_Failure_Not_Found() {
        // 존재하지 않는 옵션 ID로 조회 실패 테스트
        Long nonExistentOptionId = -1L; // 존재하지 않는 ID
        findByIdWithoutDocumentation(nonExistentOptionId)
                .statusCode(404)
                .body("status", equalTo(404));
    }
}
