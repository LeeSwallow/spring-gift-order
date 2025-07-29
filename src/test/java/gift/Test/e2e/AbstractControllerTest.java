package gift.Test.e2e;

import gift.dto.auth.LoginRequest;
import gift.dto.auth.TokenResponse;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
public abstract class AbstractControllerTest {

    @LocalServerPort
    protected int port;
    protected RequestSpecification spec;
    protected String adminToken;

    public static final String AUTH_HEADER_KEY = "authorization";
    public static final HeaderDescriptor[] AUTHENTICATE_HEADERS = {
            headerWithName("authorization").description("JWT 인증 토큰").optional()
    };

    public static final FieldDescriptor[] BASE_PAGINATION_FIELDS = {
            fieldWithPath("page").description("현재 페이지 번호").type(JsonFieldType.NUMBER),
            fieldWithPath("size").description("페이지 크기").type(JsonFieldType.NUMBER),
            fieldWithPath("totalElements").description("전체 요소 수").type(JsonFieldType.NUMBER),
            fieldWithPath("totalPages").description("전체 페이지 수").type(JsonFieldType.NUMBER),
            fieldWithPath("sort").description("정렬 정보").type(JsonFieldType.ARRAY).optional(),
            fieldWithPath("sort[].field").description("정렬 필드").type(JsonFieldType.STRING).optional(),
            fieldWithPath("sort[].direction").description("정렬 방향").type(JsonFieldType.STRING).optional()
    };

    public static final ParameterDescriptor[] PAGE_PARAMETERS = {
            parameterWithName("page").description("페이지 번호(0부터 시작)").optional(),
            parameterWithName("size").description("페이지 크기(기본값: 5)").optional(),
            parameterWithName("sort").description("정렬 기준(예: name[,desc],price)").optional()
    };

    public static  final FieldDescriptor[] ERROR_MESSAGE_FIELDS = {
        fieldWithPath("type").description("에러를 해결할 수 있는 문서 주소").type(JsonFieldType.STRING),
        fieldWithPath("title").description("에러 제목").type(JsonFieldType.STRING),
        fieldWithPath("status").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
        fieldWithPath("detail").description("에러 상세 메시지").type(JsonFieldType.STRING),
        fieldWithPath("instance").description("에러 인스턴스 ID").type(JsonFieldType.STRING).optional(),
        fieldWithPath("timestamp").description("에러 발생 시간").type(JsonFieldType.STRING).optional(),
        fieldWithPath("stackTrace").description("스택 트레이스 (개발 환경에서만 사용)").type(JsonFieldType.ARRAY).optional(),
        fieldWithPath("validationErrors").description("유효성 검사 오류 목록").type(JsonFieldType.ARRAY).optional(),
        fieldWithPath("validationErrors[].field").description("유효성 검사 오류 필드").type(JsonFieldType.STRING).optional(),
        fieldWithPath("validationErrors[].message").description("유효성 검사 오류 메시지").type(JsonFieldType.STRING).optional(),
    };

    public static FieldDescriptor[] concat(FieldDescriptor[] first, FieldDescriptor[] second) {
        FieldDescriptor[] result = new FieldDescriptor[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    @BeforeEach
    protected void setUp(RestDocumentationContextProvider provider) {
        RestAssured.port = port;
        this.spec = new RequestSpecBuilder()
                .setPort(port)
                .addFilter(documentationConfiguration(provider)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint())
                )
                .build();

        LoginRequest request = new LoginRequest("test@test.com", "qwerty1234@");

        TokenResponse tokenResponse = RestAssured.given()
                .contentType("application/json")
                .body(request)
                .post(getBaseUrl() + "/api/auth/login")
                .as(TokenResponse.class);

        this.adminToken = "Bearer " + tokenResponse.token();
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }
}
