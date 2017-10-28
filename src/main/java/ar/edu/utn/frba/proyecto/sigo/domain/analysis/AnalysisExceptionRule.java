package ar.edu.utn.frba.proyecto.sigo.domain.analysis;


import ar.edu.utn.frba.proyecto.sigo.service.analysis.AnalysisExceptionVisitor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "public.tbl_exception_rule_value_spe")
@PrimaryKeyJoinColumn(name = "exception_id")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class AnalysisExceptionRule extends AnalysisException {

    @Builder
    public AnalysisExceptionRule(
            Long id,
            AnalysisCase analysisCase,
            Long olsRuleId,
            String property,
            Double value
    ){
        super(id, analysisCase);

        this.olsRuleId = olsRuleId;
        this.property = property;
        this.value = value;
    }

    @Id
    @SequenceGenerator(name = "exceptionRuleGenerator", sequenceName = "EXCEPTION_RULE_SEQUENCE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exceptionRuleGenerator")
    @Column(name = "exception_rule_id")
    private Long id;

    @Column(name = "ols_rule_id")
    private Long olsRuleId;

    @Column(name = "property")
    private String property;

    @Column(name="value")
    private Double value;

    @Override
    public <T> T accept(AnalysisExceptionVisitor<T> visitor) {
        return visitor.visitAnalysisExceptionRule(this);
    }
}