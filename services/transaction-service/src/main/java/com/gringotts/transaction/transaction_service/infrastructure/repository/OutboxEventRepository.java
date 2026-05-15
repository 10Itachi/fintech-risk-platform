package com.gringotts.transaction.transaction_service.infrastructure.repository;

import com.gringotts.enums.OutboxStatus;
import com.gringotts.transaction.transaction_service.domain.model.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusInAndNextRetryAtBefore(
            List<OutboxStatus> statuses,
            Instant time
    );

    @Query("""
    SELECT e FROM OutboxEvent e
    WHERE e.status IN :statuses
    AND e.nextRetryAt <= :time
    ORDER BY e.createdAt ASC
""")
    List<OutboxEvent> findBatchForPublishing(
            @Param("statuses") List<OutboxStatus> statuses,
            @Param("time") Instant time,
            Pageable pageable
    );
}
