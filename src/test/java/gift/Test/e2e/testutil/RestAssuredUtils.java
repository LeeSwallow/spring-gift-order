package gift.Test.e2e.testutil;

import gift.dto.auth.LoginRequest;
import gift.dto.auth.TokenResponse;
import gift.dto.option.OptionCreateRequest;
import gift.dto.option.OptionResponse;
import gift.dto.product.ProductCreateRequest;
import gift.dto.product.ProductResponse;
import gift.dto.user.UserAdminResponse;
import gift.dto.user.UserCreateRequest;
import gift.dto.wishlist.WishedProductCreateRequest;
import gift.dto.wishlist.WishedProductResponse;
import io.restassured.RestAssured;

public class RestAssuredUtils {
    private static final String AUTH_HEADER_KEY = "authorization";
    private final String baseUrl;
    private final String adminToken;


    public RestAssuredUtils(String baseUrl, String adminToken) {
        this.baseUrl = baseUrl;
        this.adminToken = adminToken;
    }

    public UserAdminResponse createUser(UserCreateRequest request) {
        return RestAssured.given()
                .contentType("application/json")
                .header(AUTH_HEADER_KEY, adminToken) // 관리자 토큰 사용
                .body(request)
                .post(baseUrl + "/api/users")
                .then()
                .statusCode(201)
                .extract()
                .as(UserAdminResponse.class);
    }

    public void deleteUser(Long userId) {
        RestAssured.given()
                .header(AUTH_HEADER_KEY, adminToken)
                .delete(baseUrl + "/api/users/" + userId)
                .then()
                .statusCode(204);
    }

    public void deleteUser(String token) {
        RestAssured.given()
                .header(AUTH_HEADER_KEY, token)
                .delete(baseUrl + "/api/users/me")
                .then()
                .statusCode(204);
    }

    public String getToken(UserCreateRequest request) {
        TokenResponse response =  RestAssured.given()
                .contentType("application/json")
                .body(new LoginRequest(request.email(), request.password()))
                .post(baseUrl + "/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .as(TokenResponse.class);
        return "Bearer " + response.token();
    }

    public ProductResponse createProduct(ProductCreateRequest request, String token) {
        return RestAssured.given()
                .header(AUTH_HEADER_KEY, token)
                .contentType("application/json")
                .body(request)
                .post(baseUrl + "/api/products")
                .then()
                .statusCode(201)
                .extract()
                .as(ProductResponse.class);
    }

    public void deleteProduct(Long productId) {
        RestAssured.given()
                .header(AUTH_HEADER_KEY, adminToken)
                .delete(baseUrl + "/api/products/" + productId)
                .then()
                .statusCode(204);
    }

    public WishedProductResponse addProductToWishlist(Long productId, Integer quantity, String token) {
        WishedProductCreateRequest request = new WishedProductCreateRequest(productId, quantity);
        return RestAssured.given()
                .contentType("application/json")
                .body(request)
                .header(AUTH_HEADER_KEY, token)
                .post(baseUrl + "/api/wishes")
                .then()
                .statusCode(201)
                .extract()
                .as(WishedProductResponse.class);
    }

    public OptionResponse createOptionToProduct(OptionCreateRequest request, Long productId, String token) {
        return RestAssured.given()
                .header(AUTH_HEADER_KEY, this.adminToken)
                .contentType("application/json")
                .body(request)
                .when()
                .post(baseUrl + "/api/products/{productId}/options", productId)
                .then()
                .statusCode(201)
                .extract()
                .as(OptionResponse.class);
    }

}
