package gift.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="wished_products")
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "WishedProduct.withUser",
                attributeNodes = {@NamedAttributeNode("user")}
        ),
        @NamedEntityGraph(
                name = "WishedProduct.withProduct",
                attributeNodes = {@NamedAttributeNode("product")}
        ),
        @NamedEntityGraph(
                name = "WishedProduct.withUserAndProduct",
                attributeNodes = {
                        @NamedAttributeNode("user"),
                        @NamedAttributeNode("product")
                }
        )
})
public class WishedProduct extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;


    protected WishedProduct() {

    }

    public WishedProduct(User user, Product product, Integer quantity) {
        this(null, user, product, quantity);
    }

    public WishedProduct(Long id, User user, Product product, Integer quantity) {
        super(id);
        this.user = user;
        this.product = product;
        this.quantity = quantity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
