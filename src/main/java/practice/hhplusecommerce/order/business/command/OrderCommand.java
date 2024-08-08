package practice.hhplusecommerce.order.business.command;

import jakarta.persistence.Tuple;

public class OrderCommand {

  public record PopularProductResponse(
      Long productId,
      String productName,
      Integer productPrice,
      Integer productStock,
      Long sumQuantity
  ) {

    public PopularProductResponse(Long productId, String productName, Integer productPrice, Integer productStock, Long sumQuantity) {
      this.productId = productId;
      this.productName = productName;
      this.productPrice = productPrice;
      this.productStock = productStock;
      this.sumQuantity = sumQuantity;
    }

    public PopularProductResponse(Tuple tuple) {
      this(
          (Long) tuple.get("productId"),
          (String) tuple.get("productName"),
          (Integer) tuple.get("productPrice"),
          (Integer) tuple.get("productStock"),
          (Long) tuple.get("sumQuantity")
      );
    }
  }

  public record SendDataPlatform(
      Long orderId,
      Long userId,
      Integer orderTotalPrice
  ){

    public SendDataPlatform(Long orderId, Long userId, Integer orderTotalPrice) {
      this.orderId = orderId;
      this.userId = userId;
      this.orderTotalPrice = orderTotalPrice;
    }
  }
}
