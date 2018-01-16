package ar.edu.utn.frba.proyecto.sigo.domain.analysis;

import ar.edu.utn.frba.proyecto.sigo.domain.SigoDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "public.tbl_analysis_results")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AnalysisResult extends SigoDomain {

    @Id
    @SequenceGenerator(name = "analysisResultGenerator", sequenceName = "ANALYSIS_RESULT_SEQUENCE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysisResultGenerator")
    @Column(name = "result_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "obstacle_id")
    private AnalysisObstacle obstacle;

    @Column(name = "is_obstacle")
    private Boolean isObstacle;

    @Column(name = "must_keep")
    private Boolean mustKeep;

    @ManyToOne
    @JoinColumn(name = "reason_id")
    private AnalysisResultReason reason;

    @Column
    private String reasonDetail;

    public String getSummary() {
        return String.format("Obstacle: '%s'. Keep: '%s'. Reason: '%s'.", isObstacle, mustKeep, reason.getDescription());
    }
}