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

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class ModuleElectionDTO {

    @Getter(onMethod_=@JsonProperty)
    @Setter(onMethod_=@JsonIgnore)
    private boolean isElectionValid;

    private Set<String> electedModules;
    private Set<String> overflowedElectedModules;

    private ValidationSettingDTO validationSettingDTO;

}
