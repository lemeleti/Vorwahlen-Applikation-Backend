package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Log
public class ElectionService {

    public static final int MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA = 42; // PA = 6 Credits, BA = 12 Credits
    public static final int NUM_CONTEXT_MODULES = 3;
    public static final int NUM_SUBJECT_MODULES = 8;
    public static final int NUM_INTERDISCIPLINARY_MODULES = 1;
    public static final int NUM_ENGLISH_CREDITS = 20;
    public static final int CREDIT_PER_SUBJECT_MODULE = 4;

    private final ElectionRepository electionRepository;

    public Optional<ModuleElectionDTO> getModuleElectionByStudent(StudentDTO studentDTO) {
        var optional = electionRepository.findById(studentDTO.getEmail());
        if (optional.isPresent()) {
            return Optional.of(DTOMapper.mapElectionToDto.apply(optional.get()));
        }
        return Optional.empty();
    }

    public boolean saveElection(StudentDTO studentDTO, ModuleElection moduleElection) {
        // todo: implement
        // optional todo: test double modules like MC1/MC2 (not one missing)
        return false;
    }

    public boolean validateElection(StudentDTO studentDTO, ModuleElection moduleElection) {
        var isValid = false;
        if(studentDTO.isTZ()) {
            // NOTE: IP not checked because we don't store the elected modules from the previous year.
            // todo: validation for 5/6 semester or 7/8 semester
            /*
             * Konsekutive Wahlpfichtmodule: (Like MC1/MC2)
             * IT18 Teilzeit: Wenn Sie im aktuellen Studienjahr schon zwei konsekutive Module belegt haben, wählen Sie mindestens einmal zwei konsekutive Module, ansonsten mindestens zweimal zwei konsekutive Module.
             * IT19 Teilzeit: Wählen Sie bis zu zwei konsekutive Module (empfohlen: zwei Module). Achten Sie speziell auf die nötigen Vorkenntnisse der Module.
             *
             * Wahlpfichtmodule: (Like AI)
             * IT18 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen, wählen Sie total sieben Module (mit genehmigter Dispensation aus der beruflichen Anrechnung fünf Module)
             * IT19 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen wählen Sie total zwei Module. In der Regel wählen Sie hier also kein Modul.
             *
             * Wahlmodule: (like PHMOD)
             * IT18 Teilzeit: Sie können bis zu einem der oben gewählten Wahlpflichtmodule durch ein Wahlmodule ersetzen.
             * IT19 Teilzeit: In der Regel wählen Sie jetzt noch keines dieser Wahlmodule, da es mit grosser Wahrscheinlichkeit mit Ihrem obligatorischen Stundenplan nicht kompatibel ist.
             *
             * Wirtschaft und Recht: (like RM)
             * IT18 Teilzeit: Dies gehört zur Modulgruppe IT4. Sie können bis zu zwei dieser Module wählen. Das dritte Modul ist ein METU-Modul, das können Sie hier noch nicht vorwählen.
             * IT19 Teilzeit: Dies gehört zur Modulgruppe IT5. Sie können bis zu zwei dieser Module für das kommende Studienjahr wählen.
             */
        } else {
            isValid =  isOverflownEmpty(moduleElection)
                    && isCreditSumValid(moduleElection, studentDTO)
                    && validContextModuleElection(moduleElection)
                    && validSubjectModuleElection(moduleElection, studentDTO)
                    && validInterdisciplinaryModuleElection(moduleElection)
                    && validIpModuleElection(moduleElection, studentDTO);
            // todo: test double modules like MC1/MC2 (not one missing at least two of them in VT)
        }
        return isValid;
    }

    private boolean validIpModuleElection(ModuleElection moduleElection, StudentDTO studentDTO) {
        var isValid = true;
        if(studentDTO.isIP()) {
            var sum = moduleElection.getElectedModules().stream()
                    .filter(module -> "Englisch".equals(module.getLanguage()))
                    .mapToInt(Module::getCredits)
                    .sum();
            // todo: ask for dispensations in ip
            isValid = sum + studentDTO.getWpmDispensation() >= NUM_ENGLISH_CREDITS;
        }
        return isValid;
    }

    private long countModuleCategory(ModuleElection moduleElection, ModuleCategory moduleCategory) {
        return moduleElection.getElectedModules()
                .stream()
                .map(module -> ModuleCategory.parse(module.getShortModuleNo(), module.getModuleGroup()))
                .filter(category -> category == moduleCategory)
                .count();
    }

    private boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.INTERDISCIPLINARY_MODULE);
        return count == NUM_INTERDISCIPLINARY_MODULES;
    }

    private boolean validSubjectModuleElection(ModuleElection moduleElection, StudentDTO studentDTO) {
        var count = countModuleCategory(moduleElection, ModuleCategory.SUBJECT_MODULE);
        var dispensCount = studentDTO.getWpmDispensation() / CREDIT_PER_SUBJECT_MODULE;
        return count + dispensCount == NUM_SUBJECT_MODULES;
    }

    private boolean validContextModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.CONTEXT_MODULE);
        return count == NUM_CONTEXT_MODULES;
    }

    private boolean isCreditSumValid(ModuleElection moduleElection, StudentDTO studentDTO) {
        // PA dispensation für die rechnung irrelevant
        var electedModulesCreditSum = moduleElection.getElectedModules()
                .stream()
                .mapToInt(Module::getCredits)
                .sum();
        var sum = electedModulesCreditSum + studentDTO.getWpmDispensation();
        return sum == MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA;
    }

    private boolean isOverflownEmpty(ModuleElection moduleElection) {
        if(moduleElection.getOverflowedElectedModules() == null) {
            return false;
        }
        return moduleElection.getOverflowedElectedModules().size() == 0;
    }
}
