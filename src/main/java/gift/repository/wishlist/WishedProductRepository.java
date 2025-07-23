package gift.repository.wishlist;

import gift.entity.WishedProduct;
import gift.entity.WishedProductStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface WishedProductRepository extends JpaRepository<WishedProduct, Long> {

    @EntityGraph("WishedProduct.withProduct")
    Page<WishedProduct> findAllByUserId(Long userId, Pageable pageable);

    void deleteAllByUserId(Long userId);

    Boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("""
            SELECT new gift.entity.WishedProductStats(
                SUM(w.quantity),
                SUM(w.quantity * p.price)
            )
            FROM WishedProduct w
            JOIN w.product p
            WHERE w.user.id = :userId
       """)
    WishedProductStats calculateStatsByUserId(Long userId);

}
