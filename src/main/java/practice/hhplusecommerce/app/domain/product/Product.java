package practice.hhplusecommerce.app.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import practice.hhplusecommerce.app.domain.base.BaseLocalDateTimeEntity;
import practice.hhplusecommerce.global.exception.BadRequestException;

@Getter
@Entity
@NoArgsConstructor
public class Product extends BaseLocalDateTimeEntity {

  @Id
  @Comment("고유번호")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Comment("상품명")
  private String name;

  @NotNull
  @Comment("상품명")
  private Integer price;

  @NotNull
  @Comment("상품명")
  private Integer stock;

  public Product(Long id, String name, Integer price, Integer stock) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.stock = stock;
  }

  public void validSalesPossible(Integer quantity) {
    if (this.stock < quantity) {
      throw new BadRequestException("재고가 부족합니다.");
    }
  }

  public void decreaseStock(Integer quantity) {
    this.stock -= quantity;
    if (this.stock < 0) {
      throw new BadRequestException("재고가 부족합니다.");
    }
  }
}
