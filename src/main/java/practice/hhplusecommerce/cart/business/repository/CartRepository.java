package practice.hhplusecommerce.cart.business.repository;

import java.util.List;
import java.util.Optional;
import practice.hhplusecommerce.cart.business.entity.Cart;

public interface CartRepository {

  List<Cart> findAllByUserId(Long userId);

  Optional<Cart> findById(Long cartId);

  Cart save(Cart cart);

  void delete(Cart cart);
}
