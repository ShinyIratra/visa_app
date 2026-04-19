package app.visa.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VisaRequestDto {

    private Long typeDemandeId;

    @JsonProperty("etat civil")
    private Map<String, Object> etatCivil;

    private Map<String, Object> passeport;

    private Map<String, Object> visaTransformable;

    private List<Long> dossiersFournis;
}
