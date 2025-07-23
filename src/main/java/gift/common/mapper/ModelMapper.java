package gift.common.mapper;

import gift.common.model.CustomPage;
import gift.common.model.CustomOrder;
import gift.common.model.SortDirection;
import gift.dto.CustomPageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class ModelMapper {

    private ModelMapper() {
        // 객체 생성을 방지하기 위한 private 생성자
    }

    private static List<CustomOrder> extractCustomOrders(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return null;
        }
        List<CustomOrder> customOrders = new ArrayList<>();
        for (Sort.Order order : sort) {
            customOrders.add(toCustomOrder(order));
        }
        return customOrders;
    }

    public static CustomOrder toCustomOrder(Sort.Order order) {
        SortDirection direction = order.isAscending()
                ? SortDirection.ASC : SortDirection.DESC;

        return new CustomOrder(
                order.getProperty(),
                direction
        );
    }

    public static <T> CustomPage<T> toCustomPage(Page<T> page) {
        return new CustomPage<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                (int) page.getTotalElements(),
                page.getTotalPages(),
                extractCustomOrders(page.getSort()),
                null
        );
    }

    public static PageRequest toPageRequest(CustomPageRequest request) {
        return PageRequest.of(
                request.getPage(),
                request.getSize(),
                request.getSort()
        );
    }

    public static Sort toSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (int i = 0; i < sortParams.size(); ++i) {
            String property = sortParams.get(i);
            Sort.Direction direction = Sort.Direction.ASC; // 기본 정렬 방향은 ASC
            try {
                direction = Sort.Direction.valueOf(sortParams.get(i + 1).toUpperCase());
                i++; // 다음 인덱스는 방향이므로 건너뜀
            } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
                // 방향이 잘못되었거나 인덱스가 범위를 벗어난 경우 기본 방향으로 설정
            }
            orders.add(new Sort.Order(direction, property));
        }
        return Sort.by(orders);
    }
}
