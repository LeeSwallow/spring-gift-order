package gift.entity;

public class WishedProductStats {
    private final Long totalQuantity;
    private final Long totalPrice;

    public WishedProductStats(Long totalQuantity, Long totalPrice) {
        this.totalQuantity = totalQuantity != null ? totalQuantity : 0;
        this.totalPrice = totalPrice != null ? totalPrice : 0L;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public Long getTotalPrice() {
        return totalPrice;
    }
}
