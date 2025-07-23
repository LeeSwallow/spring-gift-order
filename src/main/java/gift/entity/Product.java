package gift.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.*;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Long price;
    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User owner;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(min=1, message = "상품은 최소 하나의 옵션을 가져야 합니다.")
    List<Option> options;

    protected Product() {

    }

    public Product(String name, Long price, String imageUrl) {
        this(null, name, price, imageUrl, null);
    }

    public Product(String name, Long price, String imageUrl, List<Option> options) {
        this(null, name, price, imageUrl, null);
        setOptions(options);
    }

    public Product(String name, Long price, String imageUrl, User owner) {
        this(null, name, price, imageUrl, owner);
    }

    public Product(Long id, String name, Long price, String imageUrl, User owner) {
        super(id);
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Option> getOptions() {
        return this.options;
    }

    public void setOptions(List<Option> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("상품 옵션은 최소 하나 이상이어야 합니다.");
        }
        if (this.options == null) {
            this.options = new ArrayList<>();
        } else {
            this.options.clear(); // orphanRemoval을 위해 기존 옵션들을 제거
        }
        options.forEach(this::addOption);
    }

    public void addOption(Option option) {
        this.options.add(option);
        option.setProduct(this);
    }


    @Override
    public String toString() {
        return "Product{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Product objProd)) return false;
        return Objects.equals(getId(), objProd.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), getId());
    }
}
