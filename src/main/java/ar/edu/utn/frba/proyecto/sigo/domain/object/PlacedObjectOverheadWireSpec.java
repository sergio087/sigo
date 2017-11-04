package ar.edu.utn.frba.proyecto.sigo.domain.object;


import javax.persistence.*;
import java.util.List;
import lombok.*;
import com.vividsolutions.jts.geom.MultiLineString;


@Entity
@Table(name = "public.tbl_placed_object_overhead_wire_spec")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class PlacedObjectOverheadWireSpec {
    @Id
    @SequenceGenerator(name = "placedObjectOverheadWireSpecGenerator", sequenceName="PLACED_OBJECT_OVERHEAD_WIRE_SPEC_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "placedObjectOverheadWireSpecGenerator")

    @Column(name = "spec_id")
    private Long id;

    @Column(name = "geom")
    private MultiLineString geom;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id")
    private PlacedObject placedObject;
}