package gift.Test.e2e.product;

import gift.Test.e2e.AbstractControllerTest;
import gift.Test.e2e.testutil.RestAssuredUtils;
import gift.dto.option.OptionCreateRequest;
import gift.dto.product.ProductCreateRequest;
import gift.dto.product.ProductResponse;
import gift.dto.user.UserAdminResponse;
import gift.dto.user.UserCreateRequest;
import gift.entity.type.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.restdocs.RestDocumentationContextProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractProductTest extends AbstractControllerTest {

    private RestAssuredUtils restAssuredUtils;
    protected Map<UserRole, UserAdminResponse> testUsers;
    protected Map<UserRole, String> testUserTokens;
    protected List<ProductResponse> testProducts;


    @BeforeEach
    public void setUp(RestDocumentationContextProvider provider) {
        super.setUp(provider);
        this.restAssuredUtils = new RestAssuredUtils(getBaseUrl(), this.adminToken);
        this.testUsers = new HashMap<>();
        this.testUserTokens = new HashMap<>();
        this.testProducts = new ArrayList<>();

        // 테스트용 사용자 생성
        Map.of(
                UserRole.ROLE_ADMIN,
                new UserCreateRequest("prodUser1@example.com", "password123!", List.of("ROLE_ADMIN")),
                UserRole.ROLE_MD,
                new UserCreateRequest("prodUser2@example.com", "password123!", List.of("ROLE_MD")),
                UserRole.ROLE_USER,
                new UserCreateRequest("prodUser3@example.com", "password123!", List.of("ROLE_USER"))
        ).forEach((key, value) -> {
            this.testUsers.put(key, restAssuredUtils.createUser(value));
            this.testUserTokens.put(key, restAssuredUtils.getToken(value));
        });
        // 테스트 옵션
        var options = List.of(new OptionCreateRequest("테스트 옵션", 10L));

        // 테스트용 제품 생성
         Stream.of(
            new ProductCreateRequest("테스트 제품1", 1000L, "www.example.com/image1.jpg", options),
            new ProductCreateRequest("테스트 제품2", 2000L, "www.example.com/image2.jpg", options),
            new ProductCreateRequest("테스트 제품3", 3000L, "www.example.com/image3.jpg", options)
        ).forEach(request ->
            this.testProducts.add(restAssuredUtils.createProduct(request, this.adminToken))
         );
    }

    @AfterEach
    public void tearDown() {
        if (this.testUsers != null) {
            this.testUsers.forEach((role, user) -> {
                if (user != null) {
                    restAssuredUtils.deleteUser(user.id());
                }
            });
        }
        if (this.testProducts != null) {
            this.testProducts.forEach(product -> {
                if (product != null) {
                    restAssuredUtils.deleteProduct(product.id());
                }
            });
        }
        this.restAssuredUtils = null;
    }

    public String getRequestUrl() {
        return getBaseUrl() + "/api/products";
    }
}
