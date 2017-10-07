package ar.edu.utn.frba.proyecto.sigo.domain;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.MultiPolygon;
import lombok.*;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "public.tbl_political_location_types")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class PoliticalLocationType extends SigoDomain {
    @Id
    @SequenceGenerator(name = "PoliticalLocationTypeGenerator", sequenceName = "POLITICA_LOCATION_TYPE_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PoliticalLocationTypeGenerator")
    @Column(name = "type_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "index")
    private Long index;

}