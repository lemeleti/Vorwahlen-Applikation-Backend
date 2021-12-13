package ch.zhaw.vorwahlen.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class ModuleElectionDTO {

    private long id;
    @Email(message = "{validation.email.invalid}")
    private String studentEmail;
    @Getter(onMethod_=@JsonProperty)
    @Setter(onMethod_=@JsonIgnore)
    private boolean electionValid;
    @NotNull(message = "{validation.elected.modules.null}")
    private Set<String> electedModules;
    private ValidationSettingDTO validationSettingDTO;

}
