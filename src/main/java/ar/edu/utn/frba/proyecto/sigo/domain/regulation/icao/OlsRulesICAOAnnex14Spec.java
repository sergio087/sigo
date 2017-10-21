package ar.edu.utn.frba.proyecto.sigo.domain.regulation.icao;

import javax.persistence.*;
import java.util.List;

import ar.edu.utn.frba.proyecto.sigo.domain.SigoDomain;
import ar.edu.utn.frba.proyecto.sigo.domain.regulation.OlsRule;
import lombok.*;

@Entity
@Table(name = "public.tbl_OLS_rules_ICAOAnnex14")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class OlsRulesICAOAnnex14Spec extends SigoDomain {

    @Id
    @SequenceGenerator(name = "olsRuleIcaoGenerator", sequenceName = "OLS_RULE_ICAO_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "olsRuleIcaoGenerator")
    @Column(name = "rule_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private OlsRule rule;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "surface_name")
    private ICAOAnnex14Surfaces surface;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "runway_classification")
    private ICAOAnnex14RunwayClassifications runwayClassification;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "runway_category")
    private ICAOAnnex14RunwayCategories runwayCategory;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "runway_code")
    private ICAOAnnex14RunwayCodes runwayCode;

    @Column(name = "property")
    private String property;

    @Column(name = "value")
    private Double value;
}
