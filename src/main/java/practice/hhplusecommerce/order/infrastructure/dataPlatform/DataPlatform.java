package practice.hhplusecommerce.order.infrastructure.dataPlatform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataPlatform {

  public String send(Long id, Long userId, Integer orderTotalPrice) {
    log.info("주문고유번호 : " + id);
    log.info("유저고유번호 : " + userId);
    log.info("총주문금액 : " + orderTotalPrice);
    log.info("전송완료");
    return "OK 200";
  }
}
