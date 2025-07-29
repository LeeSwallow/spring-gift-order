package gift.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name = "orders")
@NamedEntityGraph(
        name = "Order.withOptionAndProduct",
        attributeNodes = {
                @NamedAttributeNode(value = "option", subgraph = "option"),
        },
        subgraphs = @NamedSubgraph(
                name = "option",
                attributeNodes = { @NamedAttributeNode("product") }
        )
)
public class Order extends  BaseEntity {

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Option option;

    protected Order() {
    }

    public Order(User user, Option option) {
        this(null, null, null, user, option);
    }

    public Order(Integer quantity, Long totalPrice, String message, User user, Option option) {
        this(null, quantity, totalPrice, message,user, option);
    }

    public Order(Long Id, Integer quantity, Long totalPrice, String message, User user, Option option) {
        super(Id);
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.message = message;
        this.user = user;
        this.option = option;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }
}
