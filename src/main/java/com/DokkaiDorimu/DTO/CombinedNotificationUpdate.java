package com.DokkaiDorimu.DTO;

public class CombinedNotificationUpdate {
    private final NotificationDTO notification;
    private final long unreadCount;

    public CombinedNotificationUpdate(NotificationDTO notification, long unreadCount) {
        this.notification = notification;
        this.unreadCount = unreadCount;
    }

    public NotificationDTO getNotification() {
        return notification;
    }

    public long getUnreadCount() {
        return unreadCount;
    }
}
