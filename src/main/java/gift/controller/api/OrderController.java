package gift.controller.api;

import gift.common.aop.annotation.PreAuthorize;
import gift.common.mapper.EntityToDtoMapper;
import gift.common.mapper.ModelMapper;
import gift.common.model.CustomAuth;
import gift.common.model.CustomPage;
import gift.common.validation.annotation.AllowedSortFields;
import gift.dto.CustomPageRequest;
import gift.dto.order.OrderCreateRequest;
import gift.dto.order.OrderResponse;
import gift.dto.order.OrderUpdateRequest;
import gift.entity.Order;
import gift.entity.type.Provider;
import gift.entity.type.UserRole;
import gift.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize(UserRole.ROLE_USER)
    @GetMapping
    public ResponseEntity<CustomPage<OrderResponse>> getAllOrders(
            @AllowedSortFields(
                value = {"id", "option.name", "option.product.name", "quantity", "totalPrice", "createdAt", "updatedAt"},
                showAllowedFields = true
            )
            @Valid @ModelAttribute CustomPageRequest request,
            CustomAuth auth
    ) {
        var ordersPage = orderService.findAllBy(auth.userId(), ModelMapper.toPageRequest(request));
        return new ResponseEntity<>(
                CustomPage.convert(ordersPage, EntityToDtoMapper::toDto), HttpStatus.OK);
    }

    @PreAuthorize(UserRole.ROLE_USER)
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            CustomAuth auth
    ) {
        var order = orderService.findBy(id, auth.role(), auth.userId());
        return new ResponseEntity<>(EntityToDtoMapper.toDto(order), HttpStatus.OK);
    }

    @PreAuthorize(UserRole.ROLE_USER)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestAttribute(value = "X-Access-Token", required = false) String accessToken,
            CustomAuth auth
    ) {
        Order order;
        if (auth.provider() == Provider.KAKAO && accessToken != null) {
            order = orderService.createWithNotification(request.quantity(), request.message(),
                    request.optionId(), auth, accessToken);
        } else {
            order = orderService.create(request.quantity(), request.message(), request.optionId(), auth.userId());
        }
        return new ResponseEntity<>(EntityToDtoMapper.toDto(order), HttpStatus.CREATED);
    }

    @PreAuthorize(UserRole.ROLE_USER)
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request,
            CustomAuth auth
    ) {
        var order = orderService.update(
                id,
                request.quantity(),
                request.totalPrice(),
                request.message(),
                auth.role(),
                auth.userId()
        );
        return new ResponseEntity<>(EntityToDtoMapper.toDto(order), HttpStatus.OK);
    }

    @PreAuthorize(UserRole.ROLE_ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id
    ) {
        orderService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
