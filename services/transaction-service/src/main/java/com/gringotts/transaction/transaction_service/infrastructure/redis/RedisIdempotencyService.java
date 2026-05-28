package com.gringotts.transaction.transaction_service.infrastructure.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class RedisIdempotencyService {

    /*
     REDIS KEY FORMAT
     Example:
     idm:txn:abc-123
     */
    private static final String KEY_PREFIX = "idm:txn:";

    /*
     REDIS STATES
     */
    private static final String IN_PROGRESS = "IN_PROGRESS";

    private static final String COMPLETED = "COMPLETED";

    /*
     TTL CONFIGURATION

     IN_PROGRESS:
     Prevent stale locks during crashes.

     COMPLETED:
     Allows duplicate replay protection for some time.
     */
    private static final Duration IN_PROGRESS_TTL =
            Duration.ofMinutes(5);

    private static final Duration COMPLETED_TTL =
            Duration.ofMinutes(10);

    private final RedisTemplate<String, String> redisTemplate;

    public RedisIdempotencyService(
            RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    /*
     BUILD REDIS KEY
     */
    private String buildKey(String idempotencyKey) {
        return KEY_PREFIX + idempotencyKey;
    }

    /*
     TRY ACQUIRE DISTRIBUTED LOCK

     Redis command:
     SET key value NX EX ttl

     Behavior:
     - returns true  -> first request
     - returns false -> duplicate request
     */
    public boolean tryLock(String idempotencyKey) {

        String key = buildKey(idempotencyKey);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(
                        key,
                        IN_PROGRESS,
                        IN_PROGRESS_TTL
                );

        return Boolean.TRUE.equals(success);
    }

    /*
     MARK TRANSACTION COMPLETED

     Example value:
     COMPLETED:transactionId
     */
    public void markCompleted(
            String idempotencyKey,
            String transactionId
    ) {

        String key = buildKey(idempotencyKey);

        String value =
                COMPLETED + ":" + transactionId;

        redisTemplate.opsForValue().set(
                key,
                value,
                COMPLETED_TTL
        );
    }

    /*
     FETCH RAW REDIS STATE
     */
    public Optional<String> getState(
            String idempotencyKey
    ) {

        String key = buildKey(idempotencyKey);

        return Optional.ofNullable(
                redisTemplate.opsForValue().get(key)
        );
    }

    /*
     CHECK IF REQUEST STILL PROCESSING
     */
    public boolean isInProgress(String idempotencyKey) {

        return getState(idempotencyKey)
                .map(IN_PROGRESS::equals)
                .orElse(false);
    }

    /*
     GET COMPLETED TRANSACTION ID

     Example Redis value:
     COMPLETED:txn-123
     */
    public Optional<String> getCompletedTransactionId(
            String idempotencyKey
    ) {

        Optional<String> state =
                getState(idempotencyKey);

        if (state.isEmpty()) {
            return Optional.empty();
        }

        String value = state.get();

        if (!value.startsWith(COMPLETED)) {
            return Optional.empty();
        }

        /*
         Safer split handling.
         */
        String[] parts = value.split(":", 2);

        if (parts.length < 2) {
            return Optional.empty();
        }

        return Optional.of(parts[1]);
    }

    /*
     RELEASE LOCK
     Only remove IN_PROGRESS state.
     Prevent accidental deletion of COMPLETED entries.
     */
    public void releaseLock(String idempotencyKey) {

        Optional<String> state =
                getState(idempotencyKey);

        if (state.isPresent()
                && IN_PROGRESS.equals(state.get())) {

            redisTemplate.delete(
                    buildKey(idempotencyKey)
            );
        }
    }
}