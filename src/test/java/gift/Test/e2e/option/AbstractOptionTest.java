package gift.Test.e2e.option;

import gift.Test.e2e.AbstractControllerTest;
import gift.Test.e2e.testutil.RestAssuredUtils;
import gift.dto.option.OptionCreateRequest;
import gift.dto.option.OptionResponse;
import gift.dto.product.ProductCreateRequest;
import gift.dto.product.ProductResponse;
import gift.dto.user.UserAdminResponse;
import gift.dto.user.UserCreateRequest;
import gift.entity.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.restdocs.RestDocumentationContextProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractOptionTest extends AbstractControllerTest {
    private RestAssuredUtils restAssuredUtils;

    protected Map<UserRole, UserAdminResponse> testUsers;
    protected Map<UserRole, String> testUserTokens;
    protected Map<UserRole, ProductResponse> testProducts;

    protected OptionResponse createOptionToProduct(OptionCreateRequest request, Long productId, String token) {
        return restAssuredUtils.createOptionToProduct(request, productId, token);
    }

    @Override
    @BeforeEach
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);
        this.restAssuredUtils = new RestAssuredUtils(getBaseUrl(), this.adminToken);
        this.testUsers = new HashMap<>();
        this.testUserTokens = new HashMap<>();
        this.testProducts = new HashMap<>();
        Map.of(
                UserRole.ROLE_ADMIN,
                new UserCreateRequest("adminrole1234@test.com", "password123!", List.of("ROLE_ADMIN")),
                UserRole.ROLE_USER,
                new UserCreateRequest("userrole1234@test.com", "password123!", List.of("ROLE_USER"))
        ).forEach((role, request) -> {
            this.testUsers.put(role, restAssuredUtils.createUser(request));
            var token = restAssuredUtils.getToken(request);
            this.testUserTokens.put(role, token);
            var optionRequest = new OptionCreateRequest(role.name() + " Option", 100L);
            var productRequest = new ProductCreateRequest(
                    role.name() + " Prod", 1000L, "www.example.com/image.jpg", List.of(optionRequest)
            );
            this.testProducts.put(role, restAssuredUtils.createProduct(productRequest, token));
        });
    }

    @AfterEach
    public void tearDown() {
        // 테스트용 상품 삭제 - cascade로 옵션도 삭제됨
        if (this.testProducts != null) {
            this.testProducts.forEach((role, product) -> {
                if (product != null) {
                    restAssuredUtils.deleteProduct(product.id());
                }
            });
            this.testProducts.clear();
        }
        // 테스트용 사용자 삭제
        if (this.testUsers != null) {
            this.testUsers.forEach((role, user) -> {
                if (user != null) {
                    restAssuredUtils.deleteUser(user.id());
                }
            });
            this.testUsers.clear();
            this.testUserTokens.clear();
        }
        this.restAssuredUtils = null;
    }

    public String getRequestUrl() {
        return getBaseUrl() + "/api/products/{productId}/options";
    }
}
