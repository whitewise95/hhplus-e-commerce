package practice.hhplusecommerce.order.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import practice.hhplusecommerce.common.exception.BadRequestException;
import practice.hhplusecommerce.order.business.event.DataPlatformEvent;
import practice.hhplusecommerce.order.infrastructure.dataPlatform.DataPlatform;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataPlatformEventListener {

  private final DataPlatform dataPlatform;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleSendDataPlatformEvent(DataPlatformEvent dataPlatformEvent) {
    String status = dataPlatform.send(dataPlatformEvent.getOrderId(), dataPlatformEvent.getUserId(), dataPlatformEvent.getOrderTotalPrice());
    log.info("OrderFacade.order status : {}", status);

    if (!status.equals("OK 200")) {
      log.error("OrderFacade.order status : {}", status);
      throw new BadRequestException("주문정보를 데이처플랫폼에 전송 실패했습니다.");
    }
  }
}
