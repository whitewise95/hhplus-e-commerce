package practice.hhplusecommerce.order.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import practice.hhplusecommerce.common.handler.TransactionalHandler;
import practice.hhplusecommerce.order.application.dto.request.OrderFacadeRequestDto.Create;
import practice.hhplusecommerce.order.application.dto.request.OrderFacadeRequestDto.OrderProductCreate;
import practice.hhplusecommerce.order.application.dto.response.OrderFacadeResponseDto.OrderResponse;
import practice.hhplusecommerce.order.application.dto.response.OrderFacadeResponseDtoMapper;
import practice.hhplusecommerce.order.business.command.OrderCommand;
import practice.hhplusecommerce.order.business.entity.Order;
import practice.hhplusecommerce.order.business.event.DataPlatformEvent;
import practice.hhplusecommerce.order.business.service.OrderService;
import practice.hhplusecommerce.order.infrastructure.dataPlatform.DataPlatform;
import practice.hhplusecommerce.product.business.entity.Product;
import practice.hhplusecommerce.product.business.service.ProductService;
import practice.hhplusecommerce.user.business.entity.User;
import practice.hhplusecommerce.user.business.service.UserService;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderFacade {

  private final OrderService orderService;
  private final UserService userService;
  private final ProductService productService;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * 주문과 결제가 같이 진행됩니다.
   */
  @Transactional
  public OrderResponse order(Long userId, Create create) {
    User user = userService.getUser(userId);

    List<Product> productList = productService.getProductListByProductIdList(create.getProductList().stream().map(OrderProductCreate::getId).toList());

    int totalProductPrice = 0;
    Map<Long, Integer> productDecreaseStockMap = new HashMap<>();

    for (Product product : productList) {
      for (OrderProductCreate orderProductCreate : create.getProductList()) {
        if (product.getId().equals(orderProductCreate.getId())) {
          productDecreaseStockMap.put(product.getId(), orderProductCreate.getQuantity());
          totalProductPrice += product.getPrice() * orderProductCreate.getQuantity();
          break;
        }
      }
    }

    productService.decreaseProductsStock(create.getProductList().stream().map(OrderProductCreate::getId).toList(), productDecreaseStockMap);
    userService.decreaseAmount(userId, totalProductPrice);
    Order order = orderService.createOrder(totalProductPrice, user, productList, create.getProductList());

    // 데이터플랫폼 이벤트 발행
    applicationEventPublisher.publishEvent(
        new DataPlatformEvent(
            new OrderCommand.SendDataPlatform(order.getId(), order.getUser().getId(), order.getOrderTotalPrice())
        )
    );

    OrderResponse orderResponse = OrderFacadeResponseDtoMapper.toOrderResponse(order);
    orderResponse.setOrderProductList(order.getOrderProductList().stream().map(OrderFacadeResponseDtoMapper::toOrderProductResponse).toList());
    return orderResponse;
  }
}
