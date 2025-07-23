package gift.Test.e2e.product;

import gift.common.model.CustomPage;
import gift.dto.product.ProductResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class ProductReadTest extends AbstractProductTest {

    static final FieldDescriptor [] PRODUCT_READ_RESPONSE = {
            fieldWithPath("id").description("제품 ID").type(JsonFieldType.NUMBER),
            fieldWithPath("name").description("제품 이름").type(JsonFieldType.STRING),
            fieldWithPath("price").description("제품 가격").type(JsonFieldType.NUMBER),
            fieldWithPath("imageUrl").description("제품 이미지 URL").type(JsonFieldType.STRING),
            fieldWithPath("createdAt").description("제품 생성 시간").type(JsonFieldType.STRING).optional(),
            fieldWithPath("updatedAt").description("제품 업데이트 시간").type(JsonFieldType.STRING).optional()
    };

    static final FieldDescriptor[] PRODUCT_READ_PAGE_RESPONSE = concat(BASE_PAGINATION_FIELDS, new FieldDescriptor[]{
            fieldWithPath("contents").description("제품 목록").type(JsonFieldType.ARRAY).optional(),
            fieldWithPath("contents[].id").description("제품 ID").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].name").description("제품 이름").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].price").description("제품 가격").type(JsonFieldType.NUMBER).optional(),
            fieldWithPath("contents[].imageUrl").description("제품 이미지 URL").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].createdAt").description("제품 생성 시간").type(JsonFieldType.STRING).optional(),
            fieldWithPath("contents[].updatedAt").description("제품 업데이트 시간").type(JsonFieldType.STRING).optional()
    });



    @Test
    @DisplayName("전체 제품 조회 성공 테스트")
    public void find_All_Products_Success() {
        String url = getBaseUrl() + "/api/products";
        RestAssured.given(this.spec)
                .filter(document("상품 전체 조회 성공",
                        queryParameters(PAGE_PARAMETERS),
                        responseFields(PRODUCT_READ_PAGE_RESPONSE)))
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body("page", notNullValue())
                .body("size", notNullValue())
                .body("totalElements", notNullValue())
                .body("totalPages", notNullValue())
                .body("contents", notNullValue())
                .body("contents[0].id", notNullValue())
                .body("contents[0].name", notNullValue())
                .body("contents[0].price", notNullValue())
                .body("contents[0].imageUrl", notNullValue());
    }

    @Test
    @DisplayName("전체 제품 조회 성공 테스트 : 페이지와 정렬 파라미터 포함")
    public void find_All_Products_Success_With_Page_And_Sort_Parameters() {
        // 페이지와 정렬 파라미터를 포함한 URL
        String url = getBaseUrl() + "/api/products";
        CustomPage<ProductResponse> res = RestAssured.given()
                .queryParam("page", 0)
                .queryParam("size", 5)
                .queryParam("sort", "price,desc")
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        Long prevPrice = Long.MAX_VALUE;
        for (ProductResponse product : res.getContents()) {
            Long currentPrice = product.price();
            // 가격이 내림차순으로 정렬되어 있는지 확인
            if (currentPrice != null) {
                assertThat(currentPrice, lessThanOrEqualTo(prevPrice));
                prevPrice = currentPrice;
            }
        }
    }

    @Test
    @DisplayName("전체 제품 조회 실패 테스트 : 음수 페이지 요청 시  오류 발생(400 Bad Request)")
    public void find_All_Products_Success_Negative_Page_Request_default_page_Returned() {
        RestAssured.given()
                .when()
                .queryParam("page", -1)
                .get(getBaseUrl() + "/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("전체 제품 조회 성공 테스트 : 음수 크기 요청 시 오류 발생(400 Bad Request)")
    public void find_All_Products_Success_Negative_Size_Request_default_size_Returned() {
        RestAssured.given()
                .queryParam("size", -1)
                .when()
                .get(getBaseUrl() + "/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("전체 제품 조회 실패 테스트: 잘못된 정렬 기준으로 요청(400 Bad Request)")
    public void find_All_Products_Failure_Invalid_Sort_Request_400_Returned() {
       List<String> invalidSortFields = List.of(
                "invalidField", // 존재하지 않는 정렬 기준
                "price,invalidDirection", // 잘못된 정렬 방향
                "name,asc,desc" // 잘못된 정렬 기준
        );

        invalidSortFields.forEach(sortField -> {
            String url = getBaseUrl() + "/api/products";
            RestAssured.given()
                    .param("sort", sortField)
                    .when()
                    .get(url)
                    .then()
                    .statusCode(400)
                    .body("validationErrors", notNullValue()); // 유효성 검사 오류가 발생해야 함
        });
    }


    @Test
    @DisplayName("특정 제품 조회 성공 테스트")
    public void find_Specific_Product_Success() {
        String url = getBaseUrl() + "/api/products/{id}"; // 존재하는 제품 ID
        Long testProductId = this.testProducts.getFirst().id(); // 테스트용 제품 ID 가져오기
        RestAssured
                .given(this.spec)
                .filter(document("상품 특정 조회 성공",
                        pathParameters(
                                parameterWithName("id").description("조회할 제품 ID")
                        ),
                        responseFields(PRODUCT_READ_RESPONSE)))
                .when()
                .get(url, testProductId)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", notNullValue())
                .body("price", notNullValue())
                .body("imageUrl", notNullValue());
    }

    @Test
    @DisplayName("특정 제품 조회 실패 테스트 : 존재하지 않는 제품 ID")
    public void find_Specific_Product_Failure_NonExistentId_404_Returned() {
        String url = getBaseUrl() + "/api/products/{id}"; // 존재하지 않는 제품 ID
        RestAssured.given(this.spec)
                .filter(document("상품 특정 조회 실패 - 존재하지 않는 제품 ID",
                        pathParameters(
                                parameterWithName("id").description("조회할 제품 ID")
                        ),
                        responseFields(ERROR_MESSAGE_FIELDS)))
                .when()
                .get(url,9999) // 존재하지 않는 제품 ID
                .then()
                .statusCode(404);
    }
}

