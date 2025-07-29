package gift.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record OrderUpdateRequest (
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
    Integer quantity,
    @Min(value = 0, message = "총 가격은 0 이상이어야 합니다.")
    Long totalPrice,
    @Size(max = 255, message = "메시지는 최대 255자까지 입력할 수 있습니다.")
    String message
) {

}
