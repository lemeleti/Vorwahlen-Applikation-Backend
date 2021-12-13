package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.dto.ElectionStatusElementDTO;
import ch.zhaw.vorwahlen.model.modules.ModuleElectionStatus;
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
