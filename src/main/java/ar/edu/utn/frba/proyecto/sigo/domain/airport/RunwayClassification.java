package ar.edu.utn.frba.proyecto.sigo.domain.airport;

import ar.edu.utn.frba.proyecto.sigo.domain.SigoDomain;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true, exclude = "runwayDirection")
@Entity
@Table(name = "public.tbl_runway_classification")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public abstract class RunwayClassification extends SigoDomain<Long> {

    @Id
    @SequenceGenerator(
            name = "runwayClassificationGenerator",
            sequenceName = "runwayClassificationGenerator_SEQUENCE",
            allocationSize = 1,
            initialValue = 2000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "runwayClassificationGenerator"
    )
    @Column(name = "classification_id")
    protected Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direction_id", foreignKey = @ForeignKey(name = "DIRECTION_CLASSIFICATION_FK"))
    protected RunwayDirection runwayDirection;

    public abstract <T> T accept(RunwayClassificationVisitor<T> visitor);
}
