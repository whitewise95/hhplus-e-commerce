package practice.hhplusecommerce.order.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;
import practice.hhplusecommerce.common.exception.BadRequestException;
import practice.hhplusecommerce.common.exception.NotFoundException;
import practice.hhplusecommerce.order.application.dto.request.OrderFacadeRequestDto;
import practice.hhplusecommerce.order.application.dto.request.OrderFacadeRequestDto.Create;
import practice.hhplusecommerce.order.application.dto.request.OrderFacadeRequestDto.OrderProductCreate;
import practice.hhplusecommerce.order.application.dto.response.OrderFacadeResponseDto.OrderResponse;
import practice.hhplusecommerce.order.application.listener.DataPlatformEventListener;
import practice.hhplusecommerce.order.business.entity.Order;
import practice.hhplusecommerce.order.business.entity.OrderProduct;
import practice.hhplusecommerce.order.business.event.DataPlatformEvent;
import practice.hhplusecommerce.order.business.repository.OrderProductRepository;
import practice.hhplusecommerce.order.business.repository.OrderRepository;
import practice.hhplusecommerce.product.business.entity.Product;
import practice.hhplusecommerce.product.business.repository.ProductRepository;
import practice.hhplusecommerce.user.business.entity.User;
import practice.hhplusecommerce.user.business.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderFacadeIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderFacade orderFacade;

  @SpyBean
  private DataPlatformEventListener dataPlatformEventListener;

  @Autowired
  private OrderProductRepository orderProductRepository;

  @Test
  public void 주문기능_주문되는지_통합테스트() {
    //given
    String userName = "백현1";
    int amount = 5500;
    int price1 = 500;
    int price2 = 400;
    int totalProductPrice = price1 * 3 + price2 * 3;

    //when
    User user = new User(null, userName, amount);
    User saveUser = userRepository.save(user);

    List<Product> productList = List.of(
        new Product(null, "꽃병1", price1, 5),
        new Product(null, "꽃병2", price2, 5)
    );

    List<Product> saveProductList = new ArrayList<>();
    for (Product product : productList) {
      saveProductList.add(productRepository.save(product));
    }

    OrderFacadeRequestDto.Create create = new Create();
    create.setProductList(new ArrayList<>());

    for (Product product : saveProductList) {
      OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
      orderProductCreate.setId(product.getId());
      orderProductCreate.setQuantity(3);
      create.getProductList().add(orderProductCreate);
    }

    OrderResponse order = orderFacade.order(saveUser.getId(), create);

    List<OrderProduct> orderProductList = orderProductRepository.findAllByOrderId(order.getId());

    //then
    verify(dataPlatformEventListener, times(1)).handleSendDataPlatformEvent(any());
    assertNotNull(order);
    assertEquals(order.getOrderTotalPrice(), totalProductPrice);

    for (int i = 0; i < orderProductList.size(); i++) {
      assertEquals(orderProductList.get(i).getName(), order.getOrderProductList().get(i).getName());
      assertEquals(orderProductList.get(i).getPrice(), order.getOrderProductList().get(i).getPrice());
      assertEquals(orderProductList.get(i).getQuantity(), order.getOrderProductList().get(i).getQuantity());
    }

    assertEquals(orderProductList.size(), order.getOrderProductList().size());

    userRepository.delete(saveUser);
    productRepository.deleteAllInBatch(saveProductList);
  }

  @Test
  @Transactional
  public void 주문기능_상품이존재하지않을경우_에러반환하는지_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 5500;

    OrderResponse orderResponse = null;
    NotFoundException e = null;

    //when
    User user = new User(null, userName, amount);
    User saveUser = userRepository.save(user);

    OrderFacadeRequestDto.Create create = new Create();
    create.setProductList(new ArrayList<>());

    OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
    orderProductCreate.setId(0L);
    orderProductCreate.setQuantity(3);
    create.getProductList().add(orderProductCreate);

    try {
      orderResponse = orderFacade.order(saveUser.getId(), create);
    } catch (NotFoundException nfe) {
      e = nfe;
    }

    //then
    assertNull(orderResponse);
    assertNotNull(e);
    assertEquals(e.getMessage(), "상품이 존재하지 않습니다.");
  }

  @Test
  @Transactional
  public void 주문기능_유저가존재하지않을경우_에러반환하는지_통합테스트() {
    //given
    OrderResponse orderResponse = null;
    NotFoundException e = null;

    //when
    OrderFacadeRequestDto.Create create = new Create();
    create.setProductList(new ArrayList<>());

    try {
      orderResponse = orderFacade.order(-1L, create);
    } catch (NotFoundException nfe) {
      e = nfe;
    }

    //then
    assertNull(orderResponse);
    assertNotNull(e);
    assertEquals(e.getMessage(), "유저가 존재하지 않습니다.");
  }


  @Test
  @Transactional
  public void 주문기능_상품재고가부족할경우_에러반환하는지_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 5500;
    int price1 = 1500;
    int price2 = 2500;

    OrderResponse orderResponse = null;
    BadRequestException e = null;

    //when
    User user = new User(null, userName, amount);
    User saveUser = userRepository.save(user);

    List<Product> productList = List.of(
        new Product(null, "꽃병1", price1, 5),
        new Product(null, "꽃병2", price2, 5)
    );

    List<Product> saveProductList = new ArrayList<>();
    for (Product product : productList) {
      saveProductList.add(productRepository.save(product));
    }

    OrderFacadeRequestDto.Create create = new Create();
    create.setProductList(new ArrayList<>());

    for (Product product : saveProductList) {
      OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
      orderProductCreate.setId(product.getId());
      orderProductCreate.setQuantity(6);
      create.getProductList().add(orderProductCreate);
    }

    try {
      orderResponse = orderFacade.order(saveUser.getId(), create);
    } catch (BadRequestException bre) {
      e = bre;
    }

    //then
    assertNull(orderResponse);
    assertNotNull(e);
    assertEquals(e.getMessage(), "재고가 부족합니다.");
  }

  @Test
  @Transactional
  public void 주문기능_유저의잔액이부족할경우_에러반환하는지_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 3500;
    int price1 = 1500;
    int price2 = 2500;

    OrderResponse orderResponse = null;
    BadRequestException e = null;

    //when
    User user = new User(null, userName, amount);
    User saveUser = userRepository.save(user);

    List<Product> productList = List.of(
        new Product(null, "꽃병1", price1, 5),
        new Product(null, "꽃병2", price2, 5)
    );

    List<Product> saveProductList = new ArrayList<>();
    for (Product product : productList) {
      saveProductList.add(productRepository.save(product));
    }

    OrderFacadeRequestDto.Create create = new Create();
    create.setProductList(new ArrayList<>());

    for (Product product : saveProductList) {
      OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
      orderProductCreate.setId(product.getId());
      orderProductCreate.setQuantity(4);
      create.getProductList().add(orderProductCreate);
    }

    try {
      orderResponse = orderFacade.order(saveUser.getId(), create);
    } catch (BadRequestException bre) {
      e = bre;
    }

    //then
    assertNull(orderResponse);
    assertNotNull(e);
    assertEquals(e.getMessage(), "잔액이 부족합니다.");
  }

  @Test
  @Transactional
  public void 주문기능_주문완료후재고가차감되었는지_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 5500;
    int price1 = 300;
    int price2 = 500;
    int stock1 = 5;
    int stock2 = 5;

    //when
    User user = new User(null, userName, amount);
    User saveUser = userRepository.save(user);

    List<Product> productList = List.of(
        new Product(null, "꽃병1", price1, stock1),
        new Product(null, "꽃병2", price2, stock2)
    );

    List<Product> saveProductList = new ArrayList<>();
    for (Product product : productList) {
      saveProductList.add(productRepository.save(product));
    }

    OrderFacadeRequestDto.Create create = new Create();
    create.setProductList(new ArrayList<>());

    for (Product product : saveProductList) {
      OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
      orderProductCreate.setId(product.getId());
      orderProductCreate.setQuantity(3);
      create.getProductList().add(orderProductCreate);
    }

    orderFacade.order(saveUser.getId(), create);

    //then
    for (Product product : saveProductList) {
      assertEquals(product.getStock(), 2);
    }
  }

  @Test
  @Transactional
  public void 주문기능_유저잔액차감되었는지_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 5500;
    int price1 = 500;
    int price2 = 300;
    int quantity = 3;

    //when
    User user = new User(null, userName, amount);
    User saveUser = userRepository.save(user);

    List<Product> productList = List.of(
        new Product(null, "꽃병1", price1, 5),
        new Product(null, "꽃병2", price2, 5)
    );

    List<Product> saveProductList = new ArrayList<>();
    for (Product product : productList) {
      saveProductList.add(productRepository.save(product));
    }

    OrderFacadeRequestDto.Create create = new Create();
    create.setProductList(new ArrayList<>());

    for (Product product : saveProductList) {
      OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
      orderProductCreate.setId(product.getId());
      orderProductCreate.setQuantity(3);
      create.getProductList().add(orderProductCreate);
    }

    orderFacade.order(saveUser.getId(), create);

    //then
    assertEquals(saveUser.getAmount(), amount - ((price1 * quantity) + (price2 * quantity)));
  }

  @Test
  public void 주문기능_동시성_비관락_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 2146999999;
    int price = 500;
    int quantity = 2;
    int stock1 = 10000;
    int max = 1000;
    User saveUser = userRepository.save(new User(null, userName, amount));

    List<Product> productList = List.of(
        new Product(null, "꽃병1", price, stock1),
        new Product(null, "꽃병2", price, stock1)
    );

    List<Product> saveProductList = new ArrayList<>();
    for (Product product : productList) {
      saveProductList.add(productRepository.save(product));
    }

    OrderFacadeRequestDto.Create create = new Create();
    create.setProductList(new ArrayList<>());

    for (Product product : saveProductList) {
      OrderFacadeRequestDto.OrderProductCreate orderProductCreate = new OrderProductCreate();
      orderProductCreate.setId(product.getId());
      orderProductCreate.setQuantity(quantity);
      create.getProductList().add(orderProductCreate);
    }

    //when
    CompletableFuture<?>[] futures = IntStream.range(0, max)
        .mapToObj(i -> CompletableFuture.runAsync(() -> orderFacade.order(saveUser.getId(), create)))
        .toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(futures).join();


    //then
    User whenUser = userRepository.findById(saveUser.getId()).get();
    List<Product> whenProductList = productRepository.findAllByIdIn(saveProductList.stream().map(Product::getId).toList());

    List<Order> orderList = orderRepository.findAllByUserId(whenUser.getId());

    userRepository.delete(whenUser);
    productRepository.deleteAllInBatch(saveProductList);
    orderRepository.deleteAllInBatch(orderList);

    assertEquals(whenUser.getAmount(), amount - (max * 2000));

    for (int i = 0; i < whenProductList.size(); i++) {
      assertEquals(stock1 - quantity * max, whenProductList.get(i).getStock());
    }
  }
}
