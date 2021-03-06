package ar.edu.utn.frba.proyecto.sigo.service.analysis;

import ar.edu.utn.frba.proyecto.sigo.domain.analysis.Analysis;
import ar.edu.utn.frba.proyecto.sigo.domain.analysis.AnalysisCase;
import ar.edu.utn.frba.proyecto.sigo.domain.analysis.AnalysisObject;
import ar.edu.utn.frba.proyecto.sigo.domain.object.*;
import ar.edu.utn.frba.proyecto.sigo.service.SigoService;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.vividsolutions.jts.geom.Geometry;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.inject.Inject;
import javax.persistence.criteria.*;
import java.util.Optional;
import java.util.stream.Stream;

public class AnalysisCaseService extends SigoService <AnalysisCase, Analysis> {

    private AnalysisExceptionClone clone;

    @Inject
    public AnalysisCaseService(
            SessionFactory sessionFactory,
            AnalysisExceptionClone clone
    ) {
        super(AnalysisCase.class, sessionFactory);
        this.clone = clone;
    }

    @Override
    protected void preCreateActions(AnalysisCase object, Analysis parent) {
        super.preCreateActions(object, parent);

        object.setExceptions(Sets.newHashSet());
        object.setObjects(Sets.newHashSet());
    }

    @Override
    protected void postCreateActions(AnalysisCase analysisCase, Analysis parent) {

        super.postCreateActions(analysisCase, parent);

        initializeAnalyzedObjects(analysisCase);

        initializeExceptions(analysisCase, parent);
    }

    private void initializeAnalyzedObjects(AnalysisCase analysisCase) {

        this.collectElevatedObjects(analysisCase)
                .map(o -> AnalysisObject.builder()
                        .analysisCase(analysisCase)
                        .elevatedObject(o)
                        .included(o.getType().equals(ElevatedObjectTypes.LEVEL_CURVE) || hasAlreadyBeenAnalyzed(analysisCase, o))
                        .build()
                )
                .forEach(o -> currentSession().save(o));
    }

    private boolean hasAlreadyBeenAnalyzed(AnalysisCase analysisCase, ElevatedObject object) {
        return !object.getType().equals(ElevatedObjectTypes.LEVEL_CURVE)
                    && Optional.ofNullable(analysisCase.getAnalysis().getParent()).map( p -> p.hasAlreadyBeenAnalyzed(object)).orElse(Boolean.FALSE);
    }

    private void discardAnalyzedObjects(AnalysisCase analysisCase) {
        analysisCase.getObjects().forEach( o -> currentSession().delete(o));
        analysisCase.getObjects().clear();
    }

    public void updateAnalyzedObjects(AnalysisCase analysisCase, Double radius, Boolean includeTerrain){

        analysisCase.setSearchRadius(radius);
        analysisCase.setIncludeTerrain(includeTerrain);

        discardAnalyzedObjects(analysisCase);

        initializeAnalyzedObjects(analysisCase);
    }

    private void initializeExceptions(AnalysisCase analysisCase, Analysis parent) {

        Optional.ofNullable(parent.getParent()).ifPresent( p ->

            p.getAnalysisCase().getExceptions()
                    .stream()
                    .map(e -> e.accept(clone))
                    .forEach(e -> {
                        e.setAnalysisCase(analysisCase);
                        currentSession().save(e);
                    })
        );

    }

    private Stream<ElevatedObject> collectElevatedObjects(AnalysisCase analysisCase) {

        Geometry buffer = analysisCase.getAerodrome().getGeom().buffer(analysisCase.getSearchRadius());

        buffer.setSRID(4326);

        return Streams.concat(
                collectElevatedObjectsOnArea(PlacedObjectIndividual.class, buffer),
                collectElevatedObjectsOnArea(PlacedObjectBuilding.class, buffer),
                collectElevatedObjectsOnArea(PlacedObjectOverheadWire.class, buffer),
                collectElevatedObjectsOnArea(TrackSection.class, buffer),
                (analysisCase.getIncludeTerrain()) ? collectElevatedObjectsOnArea(TerrainLevelCurve.class, buffer) : Stream.empty()
        );

    }

    private <T extends ElevatedObject> Stream<T> collectElevatedObjectsOnArea(Class<T> clazz, Geometry buffer) {

        CriteriaBuilder builder = currentSession().getCriteriaBuilder();

        CriteriaQuery<T> criteria = builder.createQuery(clazz);

        Root<T> placedObject = criteria.from(clazz);


        ParameterExpression bufferParam = builder.parameter(Geometry.class);


        Expression<Boolean> st_intersects = builder.function("st_intersects", Boolean.class, placedObject.get("geom"), bufferParam);

        criteria.where(builder.isTrue(st_intersects));

        Query<T> query = currentSession().createQuery(criteria);
        query.setParameter(bufferParam, buffer);

        return query.getResultStream();
    }
}
