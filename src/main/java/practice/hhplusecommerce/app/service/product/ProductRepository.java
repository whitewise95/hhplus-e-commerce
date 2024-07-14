package practice.hhplusecommerce.app.service.product;

import java.util.List;
import java.util.Optional;
import practice.hhplusecommerce.app.domain.product.Product;

public interface ProductRepository {

  Product save(Product product);

  Optional<Product> findById(Long productId);

  List<Product> findAll();

  List<Product> findAllByIdIn(List<Long> productId);

  void saveAll(List<Product> productList);
}
