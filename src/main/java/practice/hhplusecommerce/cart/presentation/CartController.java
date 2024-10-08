package practice.hhplusecommerce.cart.presentation;


import static practice.hhplusecommerce.iterceptor.JwtTokenInterceptor.TOKEN_INFO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import practice.hhplusecommerce.cart.application.CartFacade;
import practice.hhplusecommerce.cart.presentation.dto.CartRequestDto.CartCreate;
import practice.hhplusecommerce.cart.presentation.dto.CartRequestDtoMapper;
import practice.hhplusecommerce.cart.presentation.dto.CartResponseDto.CartResponse;
import practice.hhplusecommerce.cart.presentation.dto.CartResponseDtoMapper;
import practice.hhplusecommerce.common.jwt.TokenInfoDto;

@Tag(name = "장바구니")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartFacade cartFacade;

  @Operation(summary = "장바구니 목록 조회")
  @ApiResponse(responseCode = "404", description = "유저정보가 존재하지 않습니다.")
  @GetMapping
  public List<CartResponse> getCartList(
      @RequestAttribute(value = TOKEN_INFO) TokenInfoDto tokenInfoDto
  ) {
    return cartFacade.getCartList(tokenInfoDto.getUserId())
        .stream()
        .map(CartResponseDtoMapper::toCartResponse)
        .toList();
  }

  @Operation(summary = "장바구니 추가")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "404", description = "유저정보 or 상품정보가 존재하지 않습니다."),
      @ApiResponse(responseCode = "400", description = "{상품명}이 품절 상태 입니다.")
  })
  @PostMapping
  public CartResponse create(
      @RequestAttribute(value = TOKEN_INFO) TokenInfoDto tokenInfoDto,
      @RequestBody CartCreate create
  ) {
    return CartResponseDtoMapper.toCartResponse(cartFacade.addCart(tokenInfoDto.getUserId(), CartRequestDtoMapper.toCreate(create)));
  }

  @Operation(summary = "장바구니 삭제")
  @ApiResponse(responseCode = "404", description = "유저정보가 존재하지 않습니다.")
  @DeleteMapping("/{cart-id}")
  public CartResponse delete(
      @RequestAttribute(value = TOKEN_INFO) TokenInfoDto tokenInfoDto,
      @PathVariable("cart-id") Long cartId
  ) {
    return CartResponseDtoMapper.toCartResponse(cartFacade.deleteCart(tokenInfoDto.getUserId(), cartId));
  }
}
