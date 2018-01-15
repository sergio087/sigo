package ar.edu.utn.frba.proyecto.sigo.translator.analysis;

import ar.edu.utn.frba.proyecto.sigo.domain.airport.RunwayDirection;
import ar.edu.utn.frba.proyecto.sigo.domain.analysis.AnalysisObstacle;
import ar.edu.utn.frba.proyecto.sigo.domain.object.ElevatedObject;
import ar.edu.utn.frba.proyecto.sigo.domain.ols.ObstacleLimitationSurface;
import ar.edu.utn.frba.proyecto.sigo.dto.analysis.AnalysisObstacleDTO;
import ar.edu.utn.frba.proyecto.sigo.translator.Translator;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Optional;

public class AnalysisObstacleTranslator extends Translator<AnalysisObstacle, AnalysisObstacleDTO> {

    @Override
    public AnalysisObstacleDTO getAsDTO(AnalysisObstacle domain) {
        AnalysisObstacleDTO.AnalysisObstacleDTOBuilder builder = AnalysisObstacleDTO.builder();

        ElevatedObject placedObject = domain.getObject().getElevatedObject();
        ObstacleLimitationSurface surface = domain.getSurface().getSurface();
        Coordinate objectCoordinate = placedObject.getGeom().getCoordinate();

        RunwayDirection direction = domain.getSurface().getDirection();

        builder
            .id(domain.getId())
            .caseId(domain.getAnalysisCase().getId())
            .objectId(placedObject.getId())
            .objectName(placedObject.getName())
            .objectType(placedObject.getType().ordinal())
            .coordinate(Lists.newArrayList(objectCoordinate.x, objectCoordinate.y))
            .objectHeight(domain.getObjectHeight())
            .surfaceHeight(domain.getSurfaceHeight())
            .penetration(domain.getPenetration())
            .surfaceId(surface.getId())
            .surfaceName(surface.getName())
            .directionId(direction.getId())
            .directionName(direction.getIdentifier());

        Optional.ofNullable(domain.getResult())
                .ifPresent(r -> builder
                        .resultId(domain.getResult().getId())
                        .resultSummary(r.getSummary())
                );

        Optional.ofNullable(domain.getException())
                .ifPresent(e -> builder.exceptionId(e.getId()));

        return builder.build();
    }

    @Override
    public AnalysisObstacle getAsDomain(AnalysisObstacleDTO analysisObstacleDTO) {
        throw new NotImplementedException();
    }
}
