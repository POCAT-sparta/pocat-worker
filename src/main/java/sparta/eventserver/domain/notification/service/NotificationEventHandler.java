package sparta.eventserver.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sparta.eventserver.domain.notification.dto.event.NotificationSendEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationCommandService notificationCommandService;

    // 비즈니스(서비스) 트랜잭션이 커밋되고 감지
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(NotificationSendEvent event) {
        try {
            notificationCommandService.sendInternal(
                    event.getUserId(),
                    event.getType(),
                    event.getMessage(),
                    event.getRelatedData()
            );
        } catch (Exception e) {
            log.error("알림 처리 실패: userId={}, type={}", event.getUserId(), event.getType(), e);
        }
    }
}
