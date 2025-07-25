package gift.common.mapper;

import gift.dto.option.OptionResponse;
import gift.dto.product.ProductResponse;
import gift.dto.user.UserAdminResponse;
import gift.dto.user.UserDefaultResponse;
import gift.dto.wishlist.WishedProductResponse;
import gift.entity.*;

import java.util.stream.Collectors;

public class EntityToDtoMapper {

    private EntityToDtoMapper() {
        // 인스턴스 생성 방지
    }



    public static ProductResponse toDto(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getImageUrl(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    public static WishedProductResponse toDto(WishedProduct wishedProduct) {
        Product product = wishedProduct.getProduct();
        Long subtotal = product.getPrice() * wishedProduct.getQuantity();

        return new WishedProductResponse(
                wishedProduct.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                wishedProduct.getQuantity(),
                subtotal,
                wishedProduct.getCreatedAt(),
                wishedProduct.getUpdatedAt()
        );
    }

    public static UserDefaultResponse toDto(User user) {
        return new UserDefaultResponse(
            user.getId(),
            user.getEmail(),
            user.getProvider()
        );
    }

    public static UserAdminResponse toAdminDto(User user) {
        return new UserAdminResponse(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getClientId(),
            user.getProvider(),
            user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toList()),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public static OptionResponse toDto(Option option) {
        return new OptionResponse(
            option.getId(),
            option.getName(),
            option.getQuantity(),
            option.getCreatedAt(),
            option.getUpdatedAt()
        );
    }
}
