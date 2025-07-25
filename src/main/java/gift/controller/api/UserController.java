package gift.controller.api;


import gift.common.aop.annotation.PreAuthorize;
import gift.common.mapper.DtoToEntityMapper;
import gift.common.mapper.EntityToDtoMapper;
import gift.common.mapper.ModelMapper;
import gift.common.model.CustomAuth;
import gift.common.validation.annotation.AllowedSortFields;
import gift.dto.CustomPageRequest;
import gift.dto.user.UserCreateRequest;
import gift.dto.user.UserAdminResponse;
import gift.dto.user.UserDefaultResponse;
import gift.dto.user.UserUpdateRequest;
import gift.entity.User;
import gift.common.model.CustomPage;
import gift.entity.type.UserRole;
import gift.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping
    @PreAuthorize(UserRole.ROLE_ADMIN)
    public ResponseEntity<CustomPage<UserAdminResponse>> getAllUsers(
            @AllowedSortFields(value = { "id", "email", "createdAt", "updatedAt" }, showAllowedFields = true)
            @Valid @ModelAttribute CustomPageRequest request
    ) {
        CustomPage<User> userPage = userService.findAllBy(ModelMapper.toPageRequest(request));
        return new ResponseEntity<>(
                CustomPage.convert(userPage, EntityToDtoMapper::toAdminDto), HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(UserRole.ROLE_ADMIN)
    public ResponseEntity<UserAdminResponse> getUserById(
            @NotNull(message = "사용자 ID는 필수입니다.")
            @PathVariable Long id
    ) {
        var user = userService.findById(id);
        return new ResponseEntity<>(EntityToDtoMapper.toAdminDto(user), HttpStatus.OK);
    }

    @GetMapping("/me")
    @PreAuthorize(UserRole.ROLE_USER)
    public ResponseEntity<UserDefaultResponse> getCurrentUser(
            @RequestAttribute("auth")CustomAuth auth
        ) {
        var user = userService.findById(auth.userId());
        return new ResponseEntity<>(EntityToDtoMapper.toDto(user), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize(UserRole.ROLE_ADMIN)
    public ResponseEntity<UserAdminResponse> createUser(
            @Valid  @RequestBody UserCreateRequest request
    ) {
        log.info("사용자 생성 요청: {}", request);


        var user = userService.create(DtoToEntityMapper.toEntity(request));
        log.info("사용자 생성 완료: {}", user);
        return new ResponseEntity<>(EntityToDtoMapper.toAdminDto(user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize(UserRole.ROLE_ADMIN)
    public ResponseEntity<UserAdminResponse> updateUser(
            @PathVariable @NotNull(message = "사용자 ID는 필수입니다.") Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("사용자 업데이트 요청: {}", request);
        User updateRequest = DtoToEntityMapper.toEntity(request);
        updateRequest.setId(id);
        var user = userService.update(updateRequest);
        log.info("사용자 업데이트 완료: {}", user);
        return new ResponseEntity<>(EntityToDtoMapper.toAdminDto(user), HttpStatus.OK);
    }

    @PutMapping("/me")
    @PreAuthorize(UserRole.ROLE_USER)
    public ResponseEntity<UserDefaultResponse> updateCurrentUser(
            @RequestAttribute("auth") CustomAuth auth,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("현재 사용자 업데이트 요청: {}", request);
        User updateRequest = DtoToEntityMapper.toEntity(request);
        updateRequest.setId(auth.userId());
        var user = userService.update(updateRequest);
        log.info("현재 사용자 업데이트 완료: {}", user);
        return new ResponseEntity<>(EntityToDtoMapper.toDto(user), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(UserRole.ROLE_ADMIN)
    public ResponseEntity<Void> deleteUser(
            @PathVariable @NotNull(message = "사용자 ID는 필수입니다.") Long id
    ) {
        log.info("사용자 삭제 요청: ID={}", id);
        userService.deleteById(id);
        log.info("사용자 삭제 완료: ID={}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/me")
    @PreAuthorize(UserRole.ROLE_USER)
    public ResponseEntity<Void> deleteCurrentUser(
            @RequestAttribute("auth") CustomAuth auth
    ) {
        log.info("현재 사용자 삭제 요청: ID={}", auth.userId());
        userService.deleteById(auth.userId());
        log.info("현재 사용자 삭제 완료: ID={}", auth.userId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
