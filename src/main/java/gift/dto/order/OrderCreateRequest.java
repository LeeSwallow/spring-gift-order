package gift.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderCreateRequest(
        @NotNull(message = "주문할 Option ID는 필수입니다.")
        Long optionId,
        @NotNull(message = "주문 수량은 필수입니다.")
        @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
        Integer quantity,
        @Size(max = 255, message = "메시지는 최대 255자까지 입력할 수 있습니다.")
        String message
) {
    public OrderCreateRequest {
        // 기본값 설정: message가 null인 경우 빈 문자열로 초기화
        if (message == null) {
            message = "";
        }
    }
}
