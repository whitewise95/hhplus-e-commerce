package practice.hhplusecommerce.application.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import practice.hhplusecommerce.app.application.product.ProductFacade;
import practice.hhplusecommerce.app.application.product.dto.ProductFacadeDto;
import practice.hhplusecommerce.app.entity.product.Product;
import practice.hhplusecommerce.app.service.product.ProductService;

@MockBean(JpaMetamodelMappingContext.class)
public class ProductFaçadeTest {

  @InjectMocks
  ProductFacade ProductFacade;

  @Mock
  ProductService productService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  //상품목록조회
  @Test
  public void 상품목록조회시_상품정보가반환되는지 () {
    //given
    List<ProductFacadeDto> givenList = List.of(new ProductFacadeDto(1L, "꽃병", 1500, 5));

    //when

    List<Product> productList = List.of(new Product(1L, "꽃병", 1500, 5));
    when(productService.getProductList()).thenReturn(productList);

    List<ProductFacadeDto> whenList = ProductFacade.getProductList();

    //then
    for (int i = 0; i < 1; i++) {
      assertEquals(givenList.get(i).id(), whenList.get(i).id());
      assertEquals(givenList.get(i).name(), whenList.get(i).name());
      assertEquals(givenList.get(i).price(), whenList.get(i).price());
      assertEquals(givenList.get(i).stock(), whenList.get(i).stock());
    }

    verify(productService).getProductList();
  }

  //상위 상품 목록조회
  @Test
  public void 상위상품목록조회_상품정보가반환되는지 () {
    //given
    List<ProductFacadeDto> givenList = List.of(
        new ProductFacadeDto(1L, "꽃병1", 1500, 5)
        ,new ProductFacadeDto(2L, "꽃병2", 1500, 5)
        ,new ProductFacadeDto(3L, "꽃병3", 1500, 5)
        ,new ProductFacadeDto(4L, "꽃병4", 1500, 5)
        ,new ProductFacadeDto(5L, "꽃병5", 1500, 5)
    );

    //when
    List<Product> productList = List.of(
        new Product(1L, "꽃병1", 1500, 5),
        new Product(2L, "꽃병2", 1500, 5),
        new Product(3L, "꽃병3", 1500, 5),
        new Product(4L, "꽃병4", 1500, 5),
        new Product(5L, "꽃병5", 1500, 5)
    );

    when(productService.getTop5ProductsLast3Days()).thenReturn(productList);
    List<ProductFacadeDto> whenList = ProductFacade.getTop5ProductsLast3Days();


    //then
    for (int i = 0; i < 5; i++) {
      assertEquals(givenList.get(i).id(), whenList.get(i).id());
      assertEquals(givenList.get(i).name(), whenList.get(i).name());
      assertEquals(givenList.get(i).price(), whenList.get(i).price());
      assertEquals(givenList.get(i).stock(), whenList.get(i).stock());
    }

    verify(productService).getTop5ProductsLast3Days();
  }
}
