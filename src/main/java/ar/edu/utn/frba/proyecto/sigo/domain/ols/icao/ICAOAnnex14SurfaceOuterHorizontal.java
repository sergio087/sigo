package ar.edu.utn.frba.proyecto.sigo.domain.ols.icao;

import ar.edu.utn.frba.proyecto.sigo.domain.regulation.icao.ICAOAnnex14RunwayCategories;
import ar.edu.utn.frba.proyecto.sigo.domain.regulation.icao.ICAOAnnex14RunwayClassifications;
import ar.edu.utn.frba.proyecto.sigo.domain.regulation.icao.ICAOAnnex14RunwayCodeNumbers;
import com.vividsolutions.jts.geom.Polygon;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tbl_icao14_surface_outer_horizontal")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class ICAOAnnex14SurfaceOuterHorizontal extends ICAOAnnex14Surface<Polygon> {

    @Column
    private Double radius;

    @Column
    private Double height;

    @Column(name = "initial_height")
    private Double initialHeight;

    @Column(name="geom")
    private Polygon geometry;

    @Override
    public ICAOAnnex14Surfaces getEnum() {
        return ICAOAnnex14Surfaces.OUTER_HORIZONTAL;
    }

    @Override
    public String getName() {
        return ICAOAnnex14Surfaces.OUTER_HORIZONTAL.description();
    }

    @Builder
    public ICAOAnnex14SurfaceOuterHorizontal(Long id, ICAOAnnex14RunwayClassifications classification, ICAOAnnex14RunwayCategories category, ICAOAnnex14RunwayCodeNumbers code, Polygon geometry, Double height, Double radius) {
        super(id, classification, category, code);
        this.height = height;
        this.radius = radius;
        this.geometry = geometry;
    }
}
