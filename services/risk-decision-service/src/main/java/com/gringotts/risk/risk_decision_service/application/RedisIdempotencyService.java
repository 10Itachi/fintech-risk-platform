package com.gringotts.risk.risk_decision_service.application;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Attempts to acquire an idempotency lock for a transaction.
 *
 * Redis Command Used:
 * - SET key value NX EX ttl
 *
 * Behavior:
 * - If key does NOT exist → sets key → returns true (new request)
 * - If key exists → returns false (duplicate request)
 *
 * Why this is important:
 * - Prevents duplicate processing of the same transaction
 * - Ensures only one request proceeds to heavy computation (ML, rules)
 *
 * TTL (expiry):
 * - Prevents stale locks if service crashes
 * - Allows retry after timeout
 */
@Service
public class RedisIdempotencyService {

    private static final String KEY_PREFIX = "risk:txn:";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String COMPLETED = "COMPLETED";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisIdempotencyService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(String transactionId) {
        return KEY_PREFIX + transactionId;
    }

    /**
     * Try acquiring lock (IN_PROGRESS)
     */
    public boolean tryLock(String transactionId) {
        String key = buildKey(transactionId);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, IN_PROGRESS, Duration.ofMinutes(5));

        return Boolean.TRUE.equals(success);
    }

    /**
     * Mark completed with decisionId
     */
    public void markCompleted(String transactionId, String decisionId) {
        String key = buildKey(transactionId);

        String value = COMPLETED + ":" + decisionId;

        redisTemplate.opsForValue()
                .set(key, value, Duration.ofMinutes(10));
    }

    /**
     * Get raw state
     */
    public Optional<String> getState(String transactionId) {
        String key = buildKey(transactionId);
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    /**
     * Extract decisionId if completed
     */
    public Optional<String> getCompletedDecisionId(String transactionId) {
        Optional<String> state = getState(transactionId);

        if (state.isPresent() && state.get().startsWith(COMPLETED)) {
            return Optional.of(state.get().split(":")[1]);
        }

        return Optional.empty();
    }

    public boolean isInProgress(String transactionId) {
        return getState(transactionId)
                .map(val -> val.equals(IN_PROGRESS))
                .orElse(false);
    }

    public void releaseLock(String transactionId) {
        redisTemplate.delete(buildKey(transactionId));
    }
}
