package gift.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.common.exception.KakaoApiException;
import gift.dto.external.KakaoApiErrorResponse;
import gift.dto.external.KakaoSendMessageResponse;
import gift.entity.Option;
import gift.entity.Order;
import gift.entity.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@Component
public class KakaoMessageClient {
    private final static Integer TEMPLATE_ID = 122784;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String url;

    public KakaoMessageClient(
            @Value("${gift.service.kakao.message.url}")
            String url,
            RestClient restClient
    ) {
        this.restClient = restClient;
        this.url = url;
        this.objectMapper = new ObjectMapper();
    }


    private String formatInstant(Instant instant) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm:ss"));
    }

    private String generateTemplate(Order order) {
        Option option = order.getOption();
        Product product = option.getProduct();

        Map<String, String> templateArgs = Map.of(
                "ORDER_ID", String.valueOf(order.getId()),
                "ORDER_QUANTITY", String.valueOf(order.getQuantity()),
                "ORDER_TIMESTAMP", formatInstant(order.getCreatedAt()),
                "ORDER_MESSAGE", order.getMessage(),
                "TOTAL_PRICE", String.valueOf(order.getTotalPrice()),
                "OPTION_NAME", option.getName(),
                "PRODUCT_NAME", product.getName(),
                "PRODUCT_PRICE", String.valueOf(product.getPrice()),
                "PRODUCT_IMAGE_URL", product.getImageUrl()
        );
        try {
            return objectMapper.writeValueAsString(templateArgs);
        } catch (Exception e) {
            throw new IllegalStateException("문자열을 JSON 으로 변환하는 데 실패했습니다.", e);
        }
    }

    private MultiValueMap<String, String> createRequestBody(Order order) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("template_id", TEMPLATE_ID.toString());
        body.add("template_args", generateTemplate(order));
        return body;
    }

    public void sendMessage(Order order, String accessToken) {
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + accessToken)
                .body(createRequestBody(order))
                .exchange((req, res) -> {
                    if (res.getStatusCode().is4xxClientError() || res.getStatusCode().is5xxServerError()) {
                        var errorResponse = res.bodyTo(KakaoApiErrorResponse.class);
                        throw new KakaoApiException(
                                HttpStatus.valueOf(res.getStatusCode().value()),
                                Objects.requireNonNull(errorResponse).code(),
                                errorResponse.msg()
                        );
                    }
                    return res.bodyTo(KakaoSendMessageResponse.class);
                });
    }
}
