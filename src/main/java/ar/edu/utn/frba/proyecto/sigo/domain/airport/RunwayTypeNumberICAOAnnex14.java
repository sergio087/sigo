package ar.edu.utn.frba.proyecto.sigo.domain.airport;


import javax.persistence.*;
import java.util.List;

import ar.edu.utn.frba.proyecto.sigo.domain.SigoDomain;
import lombok.*;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPolygon;

@Entity
@Table(name = "public.tbl_runway_type_number_ICAOAnnex14")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data

public class RunwayTypeNumberICAOAnnex14 extends SigoDomain {
    @Id
    @SequenceGenerator(name = "runwayTypeNumberICAOAnnex14Generator", sequenceName = "RunwayTypeNumberICAOAnnex14_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "runwayTypeNumberICAOAnnex14Generator")
    @Column(name = "code_id")
    private Long id;

    @Column(name = "length_max")
    private Double lengthMax;

    @Column(name = "length_min")
    private Double lengthMin;
}