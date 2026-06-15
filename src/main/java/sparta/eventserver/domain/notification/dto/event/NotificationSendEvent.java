package sparta.eventserver.domain.notification.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sparta.eventserver.domain.notification.enums.NotificationType;

@Getter
@RequiredArgsConstructor
public class NotificationSendEvent {
    private final Long userId;
    private final NotificationType type;
    private final String message;
    private final Object relatedData;
}
