package gift.dto.option;


import jakarta.validation.constraints.NotNull;

public record OptionPatchRequest(
    @NotNull(message = "수정할 옵션의 수량은 필수입니다.")
    Long amount
) {

}
