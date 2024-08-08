package practice.hhplusecommerce.order.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import practice.hhplusecommerce.common.exception.BadRequestException;
import practice.hhplusecommerce.common.exception.NotFoundException;
import practice.hhplusecommerce.order.application.dto.request.OrderFacadeRequestDto;
import practice.hhplusecommerce.order.application.dto.request.OrderFacadeRequestDto.Create;
import practice.hhplusecommerce.order.application.dto.request.OrderFacadeRequestDto.OrderProductCreate;
import practice.hhplusecommerce.order.application.dto.response.OrderFacadeResponseDto.OrderResponse;
import practice.hhplusecommerce.order.business.entity.Order;
import practice.hhplusecommerce.order.business.entity.OrderProduct;
import practice.hhplusecommerce.order.business.service.OrderService;
import practice.hhplusecommerce.order.infrastructure.dataPlatform.DataPlatform;
import practice.hhplusecommerce.product.business.entity.Product;
import practice.hhplusecommerce.product.business.service.ProductService;
import practice.hhplusecommerce.user.business.entity.User;
import practice.hhplusecommerce.user.business.service.UserService;

@MockBean(JpaMetamodelMappingContext.class)
public class OrderFacadeTest {

  @InjectMocks
  OrderFacade orderFacade;

  @Mock
  UserService userService;

  @Mock
  ProductService productService;

  @Mock
  OrderService orderService;

  @Mock
  ApplicationEventPublisher applicationEventPublisher;


  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void 주문하기기능_주문정보반환하는지_테스트() {

    //given
    long userId = 1L;
    String userName = "백현명";
    Integer amount = 5000;

    long productId = 1L;
    int quantity = 1;
    String productName = "꽃병";
    int stock = 5;

    long orderProductId = 1L;

    long orderId = 1L;
    int productPrice = 1500;

    //when
    User user = new User(userId, userName, amount);
    when(userService.getUser(userId)).thenReturn(user);

    List<Product> productList = List.of(new Product(1L, productName, productPrice, stock));
    when(productService.getProductListByProductIdList(List.of(productId))).thenReturn(productList);

    OrderFacadeRequestDto.Create create = new Create();

    OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
    orderProductCreate.setId(productId);
    orderProductCreate.setQuantity(quantity);
    create.setProductList(List.of(orderProductCreate));

    Order saveOrder = new Order(1L, productPrice, user);
    saveOrder.addOrderProduct(new OrderProduct(orderProductId, productName, productPrice, quantity, saveOrder, productList.get(0)));
    when(orderService.createOrder(productPrice, user, productList, create.getProductList())).thenReturn(saveOrder);

    doNothing().when(applicationEventPublisher).publishEvent(any());
    OrderResponse orderResponse = orderFacade.order(userId, create);

    //then
    assertEquals(orderResponse.getId(), orderId);
    assertEquals(orderResponse.getOrderTotalPrice(), productPrice);
    assertEquals(orderResponse.getOrderProductList().get(0).getId(), saveOrder.getOrderProductList().get(0).getId());
    assertEquals(orderResponse.getOrderProductList().get(0).getName(), saveOrder.getOrderProductList().get(0).getName());
    assertEquals(orderResponse.getOrderProductList().get(0).getQuantity(), saveOrder.getOrderProductList().get(0).getQuantity());
    assertEquals(orderResponse.getOrderProductList().get(0).getPrice(), saveOrder.getOrderProductList().get(0).getPrice());
  }

  @Test
  public void 주문하기기능_상품이존재하지않을경우_에러반환하는지_테스트() {
    //given
    long userId = 1L;
    String userName = "백현명";
    Integer amount = 2000;

    long productId = 1L;
    int quantity = 5;

    NotFoundException e = null;
    OrderResponse orderResponse = null;

    //when
    User user = new User(userId, userName, amount);
    when(userService.getUser(userId)).thenReturn(user);

    List<Product> productList = List.of();
    when(productService.getProductListByProductIdList(List.of(productId))).thenReturn(productList);

    OrderFacadeRequestDto.Create create = new Create();


    OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
    orderProductCreate.setId(productId);
    orderProductCreate.setQuantity(quantity);
    create.setProductList(List.of(orderProductCreate));

    try {
      orderResponse = orderFacade.order(userId, create);
    } catch (NotFoundException nfe) {
      e = nfe;
    }

    //then
    assertNull(orderResponse);
    assertNotNull(e);
    assertEquals(e.getMessage(), "상품이 존재하지 않습니다.");
  }

  @Test
  public void 주문하기기능_재고가부족할경우_에러반환하는지_테스트() {

    //given
    long userId = 1L;
    String userName = "백현명";
    Integer amount = 2000;
    long productId = 1L;
    int quantity = 6;
    String productName = "꽃병";
    int stock = 5;
    int productPrice = 1500;

    BadRequestException e = null;
    OrderResponse orderResponse = null;

    //when
    User user = new User(userId, userName, amount);
    when(userService.getUser(userId)).thenReturn(user);

    List<Product> productList = List.of(new Product(1L, productName, productPrice, stock));
    when(productService.getProductListByProductIdList(List.of(productId))).thenReturn(productList);

    OrderFacadeRequestDto.Create create = new Create();


    OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
    orderProductCreate.setId(productId);
    orderProductCreate.setQuantity(quantity);
    create.setProductList(List.of(orderProductCreate));

    try {
      orderResponse = orderFacade.order(userId, create);
    } catch (BadRequestException bre) {
      e = bre;
    }

    //then
    assertNull(orderResponse);
    assertNotNull(e);
    assertEquals(e.getMessage(), "재고가 부족합니다.");
  }

  @Test
  public void 주문하기기능_유저잔액이부족할경우_에러반환하는지_테스트() {
    //given
    long userId = 1L;
    String userName = "백현명";
    Integer amount = 500;

    long productId = 1L;
    int quantity = 5;
    String productName = "꽃병";
    int stock = 5;
    int productPrice = 1500;

    BadRequestException e = null;
    OrderResponse orderResponse = null;

    //when
    User user = new User(userId, userName, amount);
    when(userService.getUser(userId)).thenReturn(user);

    List<Product> productList = List.of(new Product(1L, productName, productPrice, stock));
    when(productService.getProductListByProductIdList(List.of(productId))).thenReturn(productList);

    OrderFacadeRequestDto.Create create = new Create();


    OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
    orderProductCreate.setId(productId);
    orderProductCreate.setQuantity(quantity);
    create.setProductList(List.of(orderProductCreate));

    try {
      orderResponse = orderFacade.order(userId, create);
    } catch (BadRequestException bre) {
      e = bre;
    }

    //then
    assertNull(orderResponse);
    assertNotNull(e);
    assertEquals(e.getMessage(), "잔액이 부족합니다.");
  }
}
