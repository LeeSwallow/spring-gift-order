package gift.controller.api;

import gift.common.aop.annotation.PreAuthorize;
import gift.common.mapper.EntityToDtoMapper;
import gift.common.mapper.ModelMapper;
import gift.common.model.CustomAuth;
import gift.common.model.CustomPage;
import gift.common.validation.annotation.AllowedSortFields;
import gift.dto.CustomPageRequest;
import gift.dto.option.OptionCreateRequest;
import gift.dto.option.OptionResponse;
import gift.dto.option.OptionPatchRequest;
import gift.dto.option.OptionUpdateRequest;
import gift.entity.UserRole;
import gift.service.option.OptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products/{productId}/options")
public class OptionController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    @GetMapping
    public ResponseEntity<CustomPage<OptionResponse>> getOptions(
            @PathVariable Long productId,
            @AllowedSortFields(value = { "id", "name", "quantity", "createdAt", "updatedAt" }, showAllowedFields = true)
            @Valid @ModelAttribute CustomPageRequest request
    ) {
        var pagedOptions = CustomPage.convert(
                optionService.findAllBy(productId, ModelMapper.toPageRequest(request)),
                EntityToDtoMapper::toDto
        );
        return ResponseEntity.ok(pagedOptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OptionResponse> getOptionById(
            @PathVariable Long productId,
            @PathVariable Long id
    ) {
        var option =  EntityToDtoMapper.toDto(optionService.findBy(id, productId));
        return ResponseEntity.ok(option);
    }

    @PreAuthorize(UserRole.ROLE_USER)
    @PostMapping
    public ResponseEntity<OptionResponse> createOption(
            @PathVariable Long productId,
            @RequestAttribute("auth") CustomAuth auth,
            @Valid @RequestBody OptionCreateRequest request
            ) {
        var savedOption =
                EntityToDtoMapper.toDto(optionService.create(productId, auth, request.name(), request.quantity()));

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOption);
    }

    @PreAuthorize(UserRole.ROLE_USER)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOption(
            @PathVariable Long productId,
            @PathVariable Long id,
            @RequestAttribute("auth") CustomAuth auth,
            @Valid @RequestBody OptionUpdateRequest request
    ) {
        var updatedOption = optionService.update(id, productId, auth, request.name(), request.quantity());
        return ResponseEntity.ok(EntityToDtoMapper.toDto(updatedOption));
    }

    @PreAuthorize(UserRole.ROLE_USER)
    @PatchMapping("/{id}")
    public ResponseEntity<OptionResponse> updateOptionQuantity(
            @PathVariable Long productId,
            @PathVariable Long id,
            @RequestAttribute("auth") CustomAuth auth,
            @Valid @RequestBody OptionPatchRequest request
            ) {
        var updatedOption = optionService.changeQuantityBy(id, productId, auth, request.amount());
        return ResponseEntity.ok(EntityToDtoMapper.toDto(updatedOption));
    }

    @PreAuthorize(UserRole.ROLE_USER)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOption(
            @PathVariable Long productId,
            @PathVariable Long id,
            @RequestAttribute("auth") CustomAuth auth
    ) {
        optionService.deleteBy(id, productId, auth);
        return ResponseEntity.noContent().build();
    }
}
