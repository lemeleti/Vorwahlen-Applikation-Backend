package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.core.election.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.core.election.ElectionStatusElementDTO;
import ch.zhaw.vorwahlen.model.core.election.ModuleElectionStatus;
import org.springframework.stereotype.Component;

/**
 * Mapping class for {@link ModuleElectionStatus}.
 */
@Component
public class ModuleElectionStatusMapper implements Mapper<ElectionStatusDTO, ModuleElectionStatus> {
    @Override
    public ElectionStatusDTO toDto(ModuleElectionStatus moduleElectionStatus) {
        return new ElectionStatusDTO(toDto(moduleElectionStatus.getSubjectValidation()),
                toDto(moduleElectionStatus.getContextValidation()),
                toDto(moduleElectionStatus.getInterdisciplinaryValidation()),
                toDto(moduleElectionStatus.getAdditionalValidation()));

    }

    private ElectionStatusElementDTO toDto(ModuleElectionStatus.ModuleElectionStatusElement electionStatusElement) {
        return new ElectionStatusElementDTO(electionStatusElement.getModuleCategory(),
                electionStatusElement.isValid(),
                electionStatusElement.getReasons());
    }
}
