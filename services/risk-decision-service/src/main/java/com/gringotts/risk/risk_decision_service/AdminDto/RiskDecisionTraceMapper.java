package com.gringotts.risk.risk_decision_service.AdminDto;
import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionTrace;
import com.gringotts.risk.risk_decision_service.domain.model.RiskDecisionTraceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RiskDecisionTraceMapper {

    RiskDecisionAdminResponse toAdminResponse(RiskDecisionTraceEntity entity);

    List<RiskDecisionAdminResponse> toAdminResponseList(List<RiskDecisionTraceEntity> entities);

    @Mapping(target = "modelName", source = "trace.modelMetadata.modelName")
    @Mapping(target = "modelVersion", source = "trace.modelMetadata.modelVersion")
    @Mapping(target = "id", ignore = true)
    RiskDecisionTraceEntity toEntity(RiskDecisionTrace trace, RiskDecisionRequest request);
}
