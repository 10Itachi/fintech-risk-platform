package com.gringotts.notification_service.service;


import com.gringotts.kafkaevents.TransactionFinalizedEvent;

public interface NotificationSender {
    void send(TransactionFinalizedEvent event);
}
