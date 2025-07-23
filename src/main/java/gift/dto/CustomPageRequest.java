package gift.dto;

import gift.common.validation.annotation.ValidSort;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;
import gift.common.mapper.ModelMapper;
import java.util.List;

public class CustomPageRequest {
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private final Integer page;

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    private final Integer size;

    @ValidSort
    private final List<String> sort;

    private final Sort sortObj;

    public CustomPageRequest(Integer page, Integer size, List<String> sort) {
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 10;
        this.sort = sort;
        this.sortObj = ModelMapper.toSort(sort);
    }

    public Integer getPage() {
        return page;
    }
    public Integer getSize() {
        return size;
    }
    public Sort getSort() {
        return sortObj;
    }
}

