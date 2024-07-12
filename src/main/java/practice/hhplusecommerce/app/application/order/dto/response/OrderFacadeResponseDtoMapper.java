package practice.hhplusecommerce.app.application.order.dto.response;

import java.util.List;
import practice.hhplusecommerce.app.application.order.dto.response.OrderFacadeResponseDto.OrderProductResponse;
import practice.hhplusecommerce.app.application.order.dto.response.OrderFacadeResponseDto.OrderResponse;
import practice.hhplusecommerce.app.domain.order.Order;
import practice.hhplusecommerce.app.domain.order.OrderProduct;

public class OrderFacadeResponseDtoMapper {

  public static OrderFacadeResponseDto.OrderResponse toOrderResponse(Order order) {
    OrderFacadeResponseDto.OrderResponse orderResponse = new OrderResponse();
    orderResponse.setId(order.getId());
    orderResponse.setOrderTotalPrice(order.getOrderTotalPrice());
    return orderResponse;
  }

  public static OrderFacadeResponseDto.OrderProductResponse toOrderProductResponse(OrderProduct orderProduct) {
    OrderFacadeResponseDto.OrderProductResponse orderProductResponse = new OrderProductResponse();
    orderProductResponse.setId(orderProduct.getId());
    orderProductResponse.setName(orderProduct.getName());
    orderProductResponse.setPrice(orderProduct.getPrice());
    orderProductResponse.setQuantity(orderProduct.getQuantity());
    return orderProductResponse;
  }

}