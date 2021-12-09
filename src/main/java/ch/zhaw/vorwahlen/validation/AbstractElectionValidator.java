package ch.zhaw.vorwahlen.validation;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.model.modules.ModuleElectionStatus;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.service.ModuleService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract election validator with blueprint to what needs to be checked.
 * It should check following point:
 * - a student must elect 3 context modules
 * - a student must elect 1 interdisciplinary modules
 * - a student must elect 2 times 2 consecutive modules
 * - a student must have elected a total of 8 modules (subject + consecutive)
 * - a student in international profile must have elected at least of 20 credits worth of english modules
 * - a student in international profile must have elected to following modules: WVK.ICAM-EN
 *
 * For a fulltime student the above applies.
 * For a parttime student the also applies but only in total.
 * Since the parttime student elects two times over two years and we don't store the election of the first year,
 * we skip some validation.
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractElectionValidator implements ElectionValidator {
    public static final int NUM_ENGLISH_CREDITS = 20;
    public static final int CREDIT_PER_SUBJECT_MODULE = 4;
    public static final int CREDITS_PER_CONTEXT_MODULE = 2;
    public static final int MISSING_2_CONSECUTIVE_PAIRS = 2;
    public static final int MISSING_1_CONSECUTIVE_PAIR = 1;

    private final ModuleElectionStatus moduleElectionStatus = new ModuleElectionStatus();
    private final Student student;

    @Override
    public ModuleElectionStatus validate(ModuleElection election) {
        var status = getModuleElectionStatus();

        var subjectValidation = status.getSubjectValidation();
        var contextValidation = status.getContextValidation();
        var interdisciplinaryValidation = status.getInterdisciplinaryValidation();
        var additionalValidation = status.getAdditionalValidation();

        if(election.getValidationSetting().isRepetent()) {
            subjectValidation.setValid(true);
            contextValidation.setValid(true);
            interdisciplinaryValidation.setValid(true);
            additionalValidation.setValid(true);
        } else {
            subjectValidation.setModuleCategory(ModuleCategory.SUBJECT_MODULE);
            subjectValidation.setValid(validSubjectModuleElection(election));
            subjectValidation.andValid(validConsecutiveModulePairsInElection(election));

            contextValidation.setModuleCategory(ModuleCategory.CONTEXT_MODULE);
            contextValidation.setValid(validContextModuleElection(election));

            interdisciplinaryValidation.setModuleCategory(ModuleCategory.INTERDISCIPLINARY_MODULE);
            interdisciplinaryValidation.setValid(validInterdisciplinaryModuleElection(election));

            additionalValidation.setValid(isCreditSumValid(election));
            additionalValidation.andValid(validIpModuleElection(election));
        }

        return status;
    }

    protected boolean validConsecutiveModulePairsInElection(ModuleElection moduleElection) {
        var consecutiveMap = calculateConsecutiveMap(moduleElection);
        return consecutiveModuleExtraChecks(moduleElection, consecutiveMap);
    }

    protected Map<Module, Module> calculateConsecutiveMap(ModuleElection moduleElection) {
        var consecutiveMap = new HashMap<Module, Module>();
        for(var m1: moduleElection.getElectedModules()) {
            for(var m2: moduleElection.getElectedModules()) {
                if(!m1.equals(m2) && areModulesConsecutive(m1, m2)) {
                    consecutiveMap.putIfAbsent(m1, null);
                    if (ModuleService.doTheModulesDifferOnlyInTheNumber(m1, m2)){
                        consecutiveMap.put(m1, m2);
                    }
                }
            }
        }

        // remove one fo the duplicate entries -> k: AI1, v: AI2 / k: AI2, v: AI1
        consecutiveMap.entrySet().removeIf(entrySet -> consecutiveMap.containsValue(entrySet.getKey()));
        return consecutiveMap;
    }

    protected abstract boolean consecutiveModuleExtraChecks(ModuleElection moduleElection, Map<Module, Module> consecutiveMap);

    protected boolean containsSpecialConsecutiveModules(ModuleElection moduleElection) {
        return containsModule(moduleElection.getElectedModules(), "WV.PSPP")
                && containsModule(moduleElection.getElectedModules(), "WV.FUP");
    }

    protected boolean areModulesConsecutive(Module m1, Module m2) {
        return m1.getConsecutiveModuleNo() != null && !m1.getConsecutiveModuleNo().isBlank()
                && m2.getConsecutiveModuleNo() != null && !m2.getConsecutiveModuleNo().isBlank();
    }

    protected boolean containsModule(Set<Module> modules, String moduleNo) {
        return modules.stream()
                .filter(module -> moduleNo.equals(module.getShortModuleNo()))
                .count() == 1;
    }

    protected long countModuleCategory(ModuleElection moduleElection, ModuleCategory moduleCategory) {
        return moduleElection.getElectedModules()
                .stream()
                .map(module -> ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()))
                .filter(category -> category == moduleCategory)
                .count();
    }

    protected abstract boolean validIpModuleElection(ModuleElection moduleElection);

    protected boolean validModuleElectionCountByCategory(ModuleElection moduleElection, int neededModules, ModuleCategory moduleCategory) {
        var count = countModuleCategory(moduleElection, moduleCategory);
        var isValid = count == neededModules;
        if (!isValid) {
            switch (moduleCategory) {
                case CONTEXT_MODULE -> addReasonWhenCountByCategoryNotValid(moduleCategory, moduleElectionStatus.getContextValidation(), count, neededModules);
                case SUBJECT_MODULE -> addReasonWhenCountByCategoryNotValid(moduleCategory, moduleElectionStatus.getSubjectValidation(), count, neededModules);
                case INTERDISCIPLINARY_MODULE -> addReasonWhenCountByCategoryNotValid(moduleCategory, moduleElectionStatus.getInterdisciplinaryValidation(), count, neededModules);
                default -> {}
            }
        }
        return isValid;
    }

    protected void addReasonWhenCountByCategoryNotValid(ModuleCategory moduleCategory, ModuleElectionStatus.ModuleElectionStatusElement statusElement, long count, int neededModules) {
        var category = switch (moduleCategory) {
            case CONTEXT_MODULE -> ResourceBundleMessageLoader.getMessage("election_status.context");
            case SUBJECT_MODULE -> ResourceBundleMessageLoader.getMessage("election_status.subject");
            case INTERDISCIPLINARY_MODULE -> ResourceBundleMessageLoader.getMessage("election_status.interdisciplinary");
            default -> "";
        };
        statusElement.addReason(count > neededModules
                                        ? String.format(ResourceBundleMessageLoader.getMessage("election_status.too_much_modules_of_category"), (count - neededModules), category)
                                        : String.format(ResourceBundleMessageLoader.getMessage("election_status.too_less_modules_of_category"), (neededModules - count), category));
    }

    protected void addReasonWhenCreditSumNotValid(int sum, int minNeededCredits, int maxNeededCredits) {
        var status = getModuleElectionStatus().getAdditionalValidation();
        if(sum > maxNeededCredits) {
            status.addReason(String.format(ResourceBundleMessageLoader.getMessage("election_status.too_much_credits"), (sum - maxNeededCredits)));
        } else if(sum < minNeededCredits) {
            status.addReason(String.format(ResourceBundleMessageLoader.getMessage("election_status.too_less_credits"), (minNeededCredits - sum)));
        }
    }

    protected abstract boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection);

    protected abstract boolean validSubjectModuleElection(ModuleElection moduleElection);

    protected abstract boolean validContextModuleElection(ModuleElection moduleElection);

    protected abstract boolean isCreditSumValid(ModuleElection moduleElection);

    protected int sumCreditsInclusiveDispensation(ModuleElection moduleElection, int dispensations) {
        // PA dispensation für die rechnung irrelevant
        var electedModulesCreditSum = moduleElection.getElectedModules()
                .stream()
                .mapToInt(Module::getCredits)
                .sum();
        return electedModulesCreditSum + dispensations;
    }

}
