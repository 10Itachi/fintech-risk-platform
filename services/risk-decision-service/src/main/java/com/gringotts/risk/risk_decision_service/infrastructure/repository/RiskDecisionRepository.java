package com.gringotts.risk.risk_decision_service.infrastructure.repository;

import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskDecisionRepository extends JpaRepository<RiskDecisionTraceEntity,Long>{
}
