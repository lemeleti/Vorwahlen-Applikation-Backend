package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.core.election.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.core.election.ElectionStatusElementDTO;
import ch.zhaw.vorwahlen.model.core.election.ElectionStatus;
import org.springframework.stereotype.Component;

/**
 * Mapping class for {@link ElectionStatus}.
 */
@Component
public class ElectionStatusMapper implements Mapper<ElectionStatusDTO, ElectionStatus> {
    @Override
    public ElectionStatusDTO toDto(ElectionStatus electionStatus) {
        return new ElectionStatusDTO(toDto(electionStatus.getSubjectValidation()),
                toDto(electionStatus.getContextValidation()),
                toDto(electionStatus.getInterdisciplinaryValidation()),
                toDto(electionStatus.getAdditionalValidation()));

    }

    private ElectionStatusElementDTO toDto(ElectionStatus.ElectionStatusElement electionStatusElement) {
        return new ElectionStatusElementDTO(electionStatusElement.getModuleCategory(),
                electionStatusElement.isValid(),
                electionStatusElement.getReasons());
    }
}
