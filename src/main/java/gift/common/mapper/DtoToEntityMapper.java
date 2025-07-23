package gift.common.mapper;

import gift.dto.option.OptionCreateRequest;
import gift.dto.product.ProductCreateRequest;
import gift.dto.product.ProductUpdateRequest;
import gift.dto.user.UserCreateRequest;
import gift.dto.user.UserUpdateRequest;
import gift.entity.Option;
import gift.entity.Product;
import gift.entity.User;
import gift.entity.UserRole;

import java.util.stream.Collectors;

public class DtoToEntityMapper {

    private DtoToEntityMapper() {
        // 인스턴스 생성 방지
    }

    public static Product toEntity(ProductCreateRequest request) {
        var options = request.options().stream()
                .map(DtoToEntityMapper::toEntity)
                .toList();

        return new Product(
                request.name(),
                request.price(),
                request.imageUrl(),
                options
        );
    }

    public static Product toEntity(ProductUpdateRequest request) {
        return new Product(
                request.name(),
                request.price(),
                request.imageUrl()
        );
    }

    public static User toEntity(UserCreateRequest request) {
        var mappedRoles = request.roles().stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
        return new User(
                request.email(),
                request.password(),
                mappedRoles
        );
    }

    public static User toEntity(UserUpdateRequest request) {
        var user = new User(
                request.email(),
                request.password()
        );
        if (request.roles() != null) {
            var mappedRoles = request.roles().stream()
                    .map(UserRole::valueOf)
                    .collect(Collectors.toSet());
            user.setRoles(mappedRoles);
        }
        return user;
    }

    public static Option toEntity(OptionCreateRequest request) {
        return new Option(
                request.name(),
                request.quantity()
        );
    }
}
