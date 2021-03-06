package ar.edu.utn.frba.proyecto.sigo.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class PlacedObjectDTO {
    private Long id;
    private String name;
    private Integer typeId;
    private String subtype;
    private Boolean verified;
    private Long locationId;
    private Long ownerId;
    private Double heightAgl;
    private Double heightAmls;
    private Boolean temporary;
    private Integer lightingId;
    private Integer markIndicatorId;
}
