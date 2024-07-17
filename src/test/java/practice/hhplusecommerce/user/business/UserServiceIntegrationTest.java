package practice.hhplusecommerce.user.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static practice.hhplusecommerce.common.jwt.JwtTokenProvider.CLAIMS_KEY_USER_ID;
import static practice.hhplusecommerce.common.jwt.JwtTokenProvider.CLAIMS_KEY_USER_NAME;

import io.jsonwebtoken.Claims;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.hhplusecommerce.common.jwt.JwtTokenProvider;
import practice.hhplusecommerce.user.business.dto.UserServiceResponseDto.TokenResponse;
import practice.hhplusecommerce.user.business.entity.User;
import practice.hhplusecommerce.common.exception.NotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceIntegrationTest {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;


  @Test
  @Transactional
  public void 유저잔액조회기능_유저정보조회되는지_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 0;

    //when
    User user = new User(null, userName, amount);
    User saveUser = userRepository.save(user);

    User when = userService.getUser(saveUser.getId());

    //then
    assertEquals(when.getId(), saveUser.getId());
    assertEquals(when.getName(), saveUser.getName());
    assertEquals(when.getAmount(), saveUser.getAmount());
  }

  @Test
  @Transactional
  public void 유저잔액조회기능_유저없으면_에러반환하는지_통합테스트() {
    //given
    NotFoundException e = null;
    User user = null;

    //when
    try {
      user = userService.getUser(0L);
    } catch (NotFoundException nfe) {
      e = nfe;
    }

    //then
    assertNull(user);
    assertNotNull(e);
    assertEquals(e.getMessage(), "유저가 존재하지 않습니다.");
  }

  @Test
  @Transactional
  public void 유저잔액충전기능_충전되는지_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 0;
    int chargeAmount = 1500;
    int chargeAmount2 = 2500;

    //when
    User user = new User(null, userName, amount);
    User saveUser = userRepository.save(user);

    userService.chargeUserAmount(saveUser.getId(), chargeAmount);
    userService.chargeUserAmount(saveUser.getId(), chargeAmount2);

    User when = userRepository.findById(saveUser.getId()).get();

    //then
    assertEquals(when.getId(), saveUser.getId());
    assertEquals(when.getName(), saveUser.getName());
    assertEquals(when.getAmount(), chargeAmount + chargeAmount2);
  }

  @Test
  @Transactional
  public void 유저잔액충전기능_유저없으면_에러반환하는지_통합테스트() {
    //given
    NotFoundException e = null;
    User user = null;

    //when
    try {
      user = userService.chargeUserAmount(0L, 150);
    } catch (NotFoundException nfe) {
      e = nfe;
    }

    //then
    assertNull(user);
    assertNotNull(e);
    assertEquals(e.getMessage(), "유저가 존재하지 않습니다.");
  }

  @Test
  @Transactional
  public void 유저로그인기능_유저토큰반환되는지_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 0;

    //when
    User user = new User(null, userName, amount);
    User saveUser = userRepository.save(user);

    TokenResponse login = userService.login(saveUser.getName());

    Claims claimsFormToken = jwtTokenProvider.getClaimsFormToken(login.accessToken());

    //then
    assertEquals(login.id(), saveUser.getId());
    assertEquals(login.name(), saveUser.getName());
    assertEquals(login.amount(), saveUser.getAmount());
    assertEquals(claimsFormToken.get(CLAIMS_KEY_USER_NAME).toString(), userName);
    assertEquals(Long.valueOf(claimsFormToken.get(CLAIMS_KEY_USER_ID).toString()), saveUser.getId());
  }

  @Test
  @Transactional
  public void 유저로그인기능_유저정보없으면_에러반환하는지_통합테스트() {
    //given
    NotFoundException e = null;
    TokenResponse response = null;

    //when
    try {
      response = userService.login("백현명");
    } catch (NotFoundException nfe) {
      e = nfe;
    }

    //then
    assertNull(response);
    assertNotNull(e);
    assertEquals(e.getMessage(), "유저가 존재하지 않습니다.");
  }

  @Test
  @Transactional
  public void 유저생성기능_유저장보저장되는지_통합테스트() {
    //given
    String userName = "백현명";
    int amount = 0;

    //when
    User when = userService.saveUser(userName);

    //then
    User then = userRepository.findByName(userName).orElse(null);

    assertNotNull(then);
    assertEquals(then.getName(), when.getName());
    assertEquals(then.getAmount(), when.getAmount());
    assertEquals(then.getId(), when.getId());
  }

//  @Test
//  public void 유저잔액조회기능_유저정보없으면_에러나는지_동시성_통합테스트() {
//    //given
//    String userName = "백현명";
//    int amount = 0;
//    int chargeAmount = 1500;
//    int chargeAmount2 = 2500;
//    int chargeAmount3 = 4500;
//
//    //when
//    User user = new User(null, userName, amount);
//    User saveUser = userRepository.save(user);
//
//    CompletableFuture.allOf(
//        CompletableFuture.runAsync(() -> {
//          userService.chargeUserAmount(saveUser.getId(), chargeAmount);
//        }),
//        CompletableFuture.runAsync(() -> {
//          userService.chargeUserAmount(saveUser.getId(), chargeAmount2);
//        }),
//        CompletableFuture.runAsync(() -> {
//          userService.chargeUserAmount(saveUser.getId(), chargeAmount3);
//        })
//    ).join();
//
//    User when = userRepository.findById(saveUser.getId()).get();
//    userJpaRepository.delete(saveUser);
//    //then
//    assertEquals(when.getId(), saveUser.getId());
//    assertEquals(when.getName(), saveUser.getName());
//    assertEquals(chargeAmount + chargeAmount2 + chargeAmount3, when.getAmount());
//  }
}