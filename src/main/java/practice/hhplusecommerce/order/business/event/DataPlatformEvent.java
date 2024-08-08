package practice.hhplusecommerce.order.business.event;

import lombok.Getter;
import practice.hhplusecommerce.order.business.command.OrderCommand;

@Getter
public class DataPlatformEvent {

 private final Long orderId;
 private final Long userId;
 private final Integer orderTotalPrice;

  public DataPlatformEvent(OrderCommand.SendDataPlatform sendDataPlatform) {
    this.orderId = sendDataPlatform.orderId();
    this.userId = sendDataPlatform.userId();
    this.orderTotalPrice = sendDataPlatform.orderTotalPrice();
  }
}
