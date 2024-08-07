package practice.hhplusecommerce.user.business.repository;

import java.util.Optional;
import practice.hhplusecommerce.user.business.entity.User;

public interface UserRepository {

  Optional<User> findById(long userId);

  User save(User user);

  Optional<User> findByName(String name);

  void delete(User user);

  Optional<User> findByIdPessimisticLock(Long userId);
}
