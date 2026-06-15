package sparta.eventserver.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.eventserver.domain.notification.dto.event.NotificationEvent;
import sparta.eventserver.domain.notification.entity.Notification;
import sparta.eventserver.domain.notification.enums.NotificationType;
import sparta.eventserver.domain.notification.repository.NotificationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "notification";

    // 서비스 레이어에서는 eventPublisher.publishEvent(NotificationSendEvent) 사용
    public void sendInternal(Long userId, NotificationType type, String message, Object relatedData) {
        String relatedDataJson = toJson(relatedData);
        Notification notification = Notification.create(userId, type, message, relatedDataJson);
        notificationRepository.save(notification);

        NotificationEvent event = NotificationEvent.builder()
                .notificationId(notification.getId())
                .userId(userId)
                .type(type.name())
                .message(message)
                .relatedData(relatedData)
                .createdAt(notification.getCreatedAt())
                .build();

        kafkaTemplate.send(TOPIC,
                String.valueOf(userId), // Key: userId 기준 파티션
                toJson(event))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka 알림 전송 실패 - notificationId={}, userId={}", notification.getId(), userId, ex);
                    }
                });
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("알림 직렬화 실패", e);
        }
    }
}
