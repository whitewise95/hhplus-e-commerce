package practice.hhplusecommerce.cart.infrastructure.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.hhplusecommerce.cart.business.entity.Cart;

@Repository
public interface CartJpaRepository extends JpaRepository<Cart, Long> {

  List<Cart> findAllByUserId(Long userId);
}
