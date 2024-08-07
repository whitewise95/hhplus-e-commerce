package practice.hhplusecommerce.product.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import practice.hhplusecommerce.product.application.ProductFacade;
import practice.hhplusecommerce.product.presentation.request.ProductRequestDto;
import practice.hhplusecommerce.product.presentation.response.ProductResponseDto.ProductResponse;
import practice.hhplusecommerce.product.presentation.response.ProductResponseDtoMapper;

@Tag(name = "상품")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

  private final ProductFacade productFacade;

  @Operation(summary = "상품 목록 조회")
  @GetMapping
  public List<ProductResponse> getProductList() {
    return productFacade.getProductList()
        .stream()
        .map(ProductResponseDtoMapper::toProductResponse)
        .toList();
  }

  @Operation(summary = "상품 상세 조회")
  @GetMapping("/{product-id}")
  public ProductResponse getProduct(@PathVariable("product-id") Long productId) {
    return ProductResponseDtoMapper.toProductResponse(productFacade.getProduct(productId));
  }


  @Operation(summary = "인기 판매 상품 조회")
  @GetMapping("/top")
  public List<ProductResponse> getPopularProduct() {
    return productFacade.getPopularProduct()
        .stream()
        .map(ProductResponseDtoMapper::toProductResponse)
        .toList();
  }

  @Operation(summary = "상품 수정")
  @PutMapping("/{product-id}")
  public void updateProduct(
      @PathVariable("product-id") Long productId,
      @RequestBody @Validated ProductRequestDto.Update update
  ) {
    productFacade.updateProduct(productId, update);
  }

  @Operation(summary = "상품 삭제")
  @DeleteMapping("/{product-id}")
  public void deleteProduct(@PathVariable("product-id") Long productId) {
    productFacade.deleteProduct(productId);
  }
}
