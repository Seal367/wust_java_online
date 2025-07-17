package com.example.javafx3.ui;

import com.example.javafx3.model.SkillCard;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.ArrayList;

/**
 * Simple notification system for skill events that works without JavaFX dependencies
 * This is a fallback implementation that logs notifications to console and maintains a message queue
 */
public class SkillNotificationSystem {
    private final ConcurrentLinkedQueue<NotificationMessage> messageQueue;
    private final List<NotificationListener> listeners;
    private static final int MAX_QUEUE_SIZE = 10;
    
    public SkillNotificationSystem(Object parentContainer) {
        // parentContainer parameter kept for compatibility but not used in this simple implementation
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.listeners = new ArrayList<>();
    }
    
    /**
     * Show skill unlock notification
     */
    public void showSkillUnlocked(SkillCard skill) {
        String message = String.format("🎉 NEW SKILL UNLOCKED: %s %s - %s", 
            skill.getIcon(), skill.getName(), skill.getDescription());
        addNotification(NotificationType.SKILL_UNLOCKED, message, skill.getRarityColor());
    }
    
    /**
     * Show skill activation notification
     */
    public void showSkillActivated(SkillCard skill) {
        String message = String.format("✅ SKILL ACTIVATED: %s %s", 
            skill.getIcon(), skill.getName());
        addNotification(NotificationType.SKILL_ACTIVATED, message, "#27ae60");
    }
    
    /**
     * Show skill conflict notification
     */
    public void showSkillConflict(String conflictMessage) {
        String message = "⚠️ SKILL CONFLICT: " + conflictMessage;
        addNotification(NotificationType.SKILL_CONFLICT, message, "#e67e22");
    }
    
    /**
     * Show general game notification
     */
    public void showGameNotification(String title, String message, String icon) {
        String fullMessage = String.format("%s %s: %s", icon, title, message);
        addNotification(NotificationType.GAME_EVENT, fullMessage, "#3498db");
    }
    
    /**
     * Add notification to queue and process it
     */
    private void addNotification(NotificationType type, String message, String color) {
        NotificationMessage notification = new NotificationMessage(type, message, color, System.currentTimeMillis());
        
        // Add to queue (remove oldest if queue is full)
        if (messageQueue.size() >= MAX_QUEUE_SIZE) {
            messageQueue.poll();
        }
        messageQueue.offer(notification);
        
        // Process notification immediately
        processNotification(notification);
        
        // Notify listeners
        for (NotificationListener listener : listeners) {
            listener.onNotification(notification);
        }
    }
    
    /**
     * Process notification (console output for now)
     */
    private void processNotification(NotificationMessage notification) {
        // Simple console output - in a full JavaFX implementation this would show UI notifications
        System.out.println("[NOTIFICATION] " + notification.message);
        
        // For important notifications, also print to stderr for visibility
        if (notification.type == NotificationType.SKILL_UNLOCKED || 
            notification.type == NotificationType.SKILL_CONFLICT) {
            System.err.println("[IMPORTANT] " + notification.message);
        }
    }
    
    /**
     * Get recent notifications
     */
    public List<NotificationMessage> getRecentNotifications() {
        return new ArrayList<>(messageQueue);
    }
    
    /**
     * Clear all notifications
     */
    public void clearNotifications() {
        messageQueue.clear();
    }
    
    /**
     * Add notification listener
     */
    public void addNotificationListener(NotificationListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove notification listener
     */
    public void removeNotificationListener(NotificationListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Get notification count
     */
    public int getNotificationCount() {
        return messageQueue.size();
    }
    
    /**
     * Check if there are unread notifications
     */
    public boolean hasUnreadNotifications() {
        return !messageQueue.isEmpty();
    }
    
    /**
     * Notification message data class
     */
    public static class NotificationMessage {
        public final NotificationType type;
        public final String message;
        public final String color;
        public final long timestamp;
        
        public NotificationMessage(NotificationType type, String message, String color, long timestamp) {
            this.type = type;
            this.message = message;
            this.color = color;
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s", type, message);
        }
    }
    
    /**
     * Notification types
     */
    public enum NotificationType {
        SKILL_UNLOCKED,
        SKILL_ACTIVATED,
        SKILL_CONFLICT,
        GAME_EVENT
    }
    
    /**
     * Interface for notification listeners
     */
    public interface NotificationListener {
        void onNotification(NotificationMessage notification);
    }
}
