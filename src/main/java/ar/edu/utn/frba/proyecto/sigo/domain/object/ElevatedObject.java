package ar.edu.utn.frba.proyecto.sigo.domain.object;

import ar.edu.utn.frba.proyecto.sigo.domain.SigoDomain;
import ar.edu.utn.frba.proyecto.sigo.domain.Spatial;
import com.vividsolutions.jts.geom.Geometry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@AllArgsConstructor
@Data
public abstract class ElevatedObject<T extends Geometry>
    extends SigoDomain
    implements Spatial<T>
{

    @Id
    @SequenceGenerator(name = "elevatedObjectGenerator", sequenceName = "ELEVATED_OBJECT_SEQUENCE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "elevatedObjectGenerator")
    @Column(name = "object_id")
    protected Long id;

    @Column(name = "name")
    protected String name;

    @Column(name = "height_agl")
    protected Double heightAgl;

    @Column(name = "height_amls")
    protected Double heightAmls;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type")
    protected ElevatedObjectTypes type;

    @Column(name = "geom")
    private T geom;
}
