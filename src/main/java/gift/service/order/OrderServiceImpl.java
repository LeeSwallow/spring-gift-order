package gift.service.order;

import gift.common.exception.AccessDeniedException;
import gift.common.mapper.ModelMapper;
import gift.common.model.CustomAuth;
import gift.common.model.CustomPage;
import gift.entity.Option;
import gift.entity.Order;
import gift.entity.User;
import gift.entity.type.Provider;
import gift.entity.type.UserRole;
import gift.external.KakaoMessageClient;
import gift.repository.order.OrderRepository;
import gift.service.option.OptionService;
import gift.service.user.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Integer ADMIN_PRIORITY = UserRole.ROLE_ADMIN.getPriority();

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OptionService optionService;
    private final KakaoMessageClient kakaoMessageClient;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            UserService userService,
            OptionService optionService,
            KakaoMessageClient kakaoMessageClient
    ) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.optionService = optionService;
        this.kakaoMessageClient = kakaoMessageClient;
    }

    private void changeOptionQuantity(Option option, Long userId, Integer amount) {
        // 임시로 관리자 권한을 부여하여 특정 option의 수량을 변경합니다.
        CustomAuth temporaryAuth = new CustomAuth(userId, UserRole.ROLE_ADMIN, Provider.EMAIL);
        optionService.changeQuantityBy(option.getId(), option.getProduct().getId(), temporaryAuth, (long) amount);
    }


    @Override
    @Transactional(readOnly = true)
    public CustomPage<Order> findAllBy(Long userId, Pageable pageable) {
        if (!userService.existsById(userId)) {
            throw new NoSuchElementException("존재하지 않는 사용자입니다. userId: " + userId);
        }
        return ModelMapper.toCustomPage(orderRepository.findAllByUserId(userId, pageable));
    }

    @Override
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 주문입니다. orderId: " + orderId));
    }

    @Override
    public Order findBy(Long id, UserRole role, Long userId) {
        Order order = findById(id);
        if (role.getPriority() < ADMIN_PRIORITY && !order.getUser().getId().equals(userId)) {
            throw new NoSuchElementException("존재하지 않는 주문입니다. orderId: " + id);
        }
        return order;
    }

    @Override
    @Transactional
    public Order create(Integer quantity, String message, Long optionId, Long userId) {
        Option option = optionService.findById(optionId);
        User userRef = userService.getReference(userId);

        changeOptionQuantity(option, userId, -quantity);
        Long totalPrice = option.getProduct().getPrice() * quantity;
        return orderRepository.save(new Order(quantity, totalPrice, message, userRef, option));
    }

    @Override
    @Transactional
    public Order createWithNotification(Integer quantity, String message, Long optionId, CustomAuth auth, String accessToken) {
        Order order = create(quantity, message, optionId, auth.userId());
        // 현재는 KakaoProvider 에 대한 알림 처리만을 구현 합니다.
        if (auth.provider() == Provider.KAKAO) {
            // 카카오톡 메시지 전송
            kakaoMessageClient.sendMessage(order, accessToken);
        }
        return order;
    }

    @Override
    @Transactional
    public Order update(Long id, Integer quantity, Long totalPrice, String message, UserRole role, Long userId) {

        Order order = findById(id);
        if (quantity != null) {
            // 권한 체크: 관리자 권한이 아닌 경우 주문 수량 변경을 허용하지 않음
            if (role.getPriority() < ADMIN_PRIORITY) {
                throw new AccessDeniedException("주문 수량을 변경하기 위해서는 관리자 권한이 필요합니다.");
            }
            int quantityDiff = quantity - order.getQuantity();
            changeOptionQuantity(order.getOption(), order.getUser().getId(), -quantityDiff);
            order.setQuantity(quantity);
            order.setTotalPrice(order.getOption().getProduct().getPrice() * quantity);
        }
        if (totalPrice != null) {
            // 권한 체크: 관리자 권한이 아닌 경우 총 가격 변경을 허용하지 않음
            if (role.getPriority() < ADMIN_PRIORITY) {
                throw new AccessDeniedException("총 가격을 변경하기 위해서는 관리자 권한이 필요합니다.");
            }
            order.setTotalPrice(totalPrice);
        }

        if (message != null) {
            // 권한 체크: 관리자 권한이 아닌 경우 메시지 변경을 허용하지 않음
            if (role.getPriority() < ADMIN_PRIORITY && !order.getUser().getId().equals(userId)) {
                throw new NoSuchElementException("존재하지 않는 주문입니다. orderId: " + id);
            }
            order.setMessage(message);
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Order order = findById(id);

        changeOptionQuantity(order.getOption(), order.getUser().getId(), order.getQuantity());
        orderRepository.delete(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getReference(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NoSuchElementException("존재하지 않는 주문입니다. orderId: " + id);
        }
        return orderRepository.getReferenceById(id);
    }
}
