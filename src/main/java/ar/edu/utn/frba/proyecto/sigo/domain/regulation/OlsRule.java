package ar.edu.utn.frba.proyecto.sigo.domain.regulation;

import javax.persistence.*;

import ar.edu.utn.frba.proyecto.sigo.domain.SigoDomain;
import ar.edu.utn.frba.proyecto.sigo.domain.regulation.faa.OlsRulesFAASpec;
import ar.edu.utn.frba.proyecto.sigo.domain.regulation.icao.OlsRulesICAOAnnex14;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;


@Entity
@Table(name = "public.tbl_ols_rules")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class OlsRule extends SigoDomain {

    @Id
    @SequenceGenerator(name = "olsRuleGenerator", sequenceName = "OLS_RULE_SEQUENCE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "olsRuleGenerator")
    @Column(name = "rule_id")
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "regulation_id")
    private Regulations regulation;

    @OneToOne(
            mappedBy = "rule",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @LazyToOne( LazyToOneOption.NO_PROXY )
    private OlsRulesICAOAnnex14 icaoRule;

    @OneToOne(
            mappedBy = "rule",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @LazyToOne( LazyToOneOption.NO_PROXY )
    private OlsRulesFAASpec faaRule;

}


