package gift.Test.e2e.wishlist;

import gift.Test.e2e.AbstractControllerTest;
import gift.Test.e2e.testutil.RestAssuredUtils;
import gift.dto.option.OptionCreateRequest;
import gift.dto.product.ProductCreateRequest;
import gift.dto.product.ProductResponse;
import gift.dto.user.UserCreateRequest;
import gift.dto.wishlist.WishedProductResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.restdocs.RestDocumentationContextProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWishlistTest extends AbstractControllerTest {
    private RestAssuredUtils restAssuredUtils;
    protected List<ProductResponse> testProducts;
    protected String testToken;

    protected WishedProductResponse addProductToWishlist(Long productId, Integer quantity) {
        return restAssuredUtils.addProductToWishlist(productId,quantity, this.testToken);
    }

    @BeforeEach
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);
        this.restAssuredUtils = new RestAssuredUtils(getBaseUrl(), adminToken);
        this.testProducts = new ArrayList<>();
        var options = List.of(
                new OptionCreateRequest("옵션1", 100L),
                new OptionCreateRequest("옵션2", 200L)
        );

        for (int i= 0; i < 5; i++) {
            ProductCreateRequest request = new ProductCreateRequest(
                    "테스트 제품 " + i, 1000L + i, "이미지 URL " + i, options
            );
            ProductResponse response = restAssuredUtils.createProduct(request, this.adminToken);
            this.testProducts.add(response);
        }
        var testUserRequest = new UserCreateRequest(
                "wishtest@test.com", "password1234!", List.of("ROLE_USER")
        );
        restAssuredUtils.createUser(testUserRequest);
        this.testToken = restAssuredUtils.getToken(testUserRequest);
    }

    @AfterEach
    public void tearDown() {
        this.testProducts.forEach(product ->
            RestAssured.given()
                    .header(AUTH_HEADER_KEY, this.adminToken)
                    .delete(getBaseUrl() + "/api/products/{id}", product.id())
                    .then()
                    .statusCode(204));
        this.testProducts.clear();
        this.restAssuredUtils.deleteUser(testToken);
        this.testToken = null;
        this.restAssuredUtils = null;
    }

    protected String getRequestUrl() {
        return getBaseUrl() + "/api/wishes";
    }

}
