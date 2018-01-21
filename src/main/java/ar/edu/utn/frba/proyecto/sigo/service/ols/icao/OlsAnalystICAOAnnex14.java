package ar.edu.utn.frba.proyecto.sigo.service.ols.icao;

import ar.edu.utn.frba.proyecto.sigo.domain.airport.Runway;
import ar.edu.utn.frba.proyecto.sigo.domain.airport.RunwayDirection;
import ar.edu.utn.frba.proyecto.sigo.domain.airport.icao.RunwayClassificationICAOAnnex14;
import ar.edu.utn.frba.proyecto.sigo.domain.analysis.AnalysisCase;
import ar.edu.utn.frba.proyecto.sigo.domain.analysis.AnalysisExceptionRule;
import ar.edu.utn.frba.proyecto.sigo.domain.analysis.AnalysisSurface;
import ar.edu.utn.frba.proyecto.sigo.domain.ols.icao.*;
import ar.edu.utn.frba.proyecto.sigo.domain.regulation.icao.OlsRuleICAOAnnex14;
import ar.edu.utn.frba.proyecto.sigo.exception.SigoException;
import ar.edu.utn.frba.proyecto.sigo.persistence.HibernateUtil;
import ar.edu.utn.frba.proyecto.sigo.service.ols.OlsAnalyst;
import ar.edu.utn.frba.proyecto.sigo.service.regulation.OlsRuleICAOAnnex14Service;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.vividsolutions.jts.geom.Point;
import org.apache.commons.lang3.StringUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OlsAnalystICAOAnnex14 extends OlsAnalyst {

    private OlsRuleICAOAnnex14Service definitionService;
    private ICAOAnnex14SurfaceGeometriesHelper geometryHelper;
    private ICAOAnnex14SurfaceHeightsHelper heightsHelper;

    @Inject
    public OlsAnalystICAOAnnex14(
            OlsRuleICAOAnnex14Service service,
            ICAOAnnex14SurfaceGeometriesHelper geometryHelper,
            HibernateUtil hibernateUtil,
            ICAOAnnex14SurfaceHeightsHelper heightsHelper,
            @Assisted AnalysisCase analysisCase
    ) {
        super(analysisCase, hibernateUtil.getSessionFactory());

        this.definitionService = service;
        this.geometryHelper = geometryHelper;
        this.heightsHelper = heightsHelper;
    }

    @Override
    protected void applyExceptions() {

    }

    @Override
    public Double determineHeightForAnalysisSurface(AnalysisSurface analysisSurface, Point point) {

        Double surfaceHeight = this.heightsHelper.determineHeightAt((ICAOAnnex14Surface)analysisSurface.getSurface(), point);

        return analysisSurface.getDirection().getHeight() + surfaceHeight;
    }

    @Override
    protected void initializeSurfaces() {

        this.analysisCase.setSurfaces(createAnalysisSurfaces());
    }


    private Set<AnalysisSurface> createAnalysisSurfaces() {

        return this.analysisCase.getAerodrome().getRunways()
                .stream()
                .map(Runway::getDirections)
                .flatMap(Collection::stream)
                .map(this::createAnalysisSurfaces)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<AnalysisSurface> createAnalysisSurfaces(RunwayDirection direction){

        RunwayClassificationICAOAnnex14 classification = (RunwayClassificationICAOAnnex14) direction.getClassification();

        List<ICAOAnnex14Surface> surfacesDefinitions = this.definitionService.getSurfaces(classification.getRunwayClassification(), classification.getRunwayCategory(), classification.getRunwayTypeNumber(), false);

        switch (classification.getRunwayClassification()) {
            case NON_INSTRUMENT:
                return createAnalysisSurfacesForNonInstrument(direction, surfacesDefinitions);
            case NON_PRECISION_APPROACH:
                return createAnalysisSurfacesForNonPrecision(direction, surfacesDefinitions); //TODO createAnalysisSurfacesForNonPrecision
            case PRECISION_APPROACH:
                return createAnalysisSurfacesForPrecision(direction, surfacesDefinitions); //TODO createAnalysisSurfacesForPrecision
        }

        throw new SigoException("Invalid classification of runway direction");
    }

    private Set<AnalysisSurface> createAnalysisSurfacesForPrecision(RunwayDirection direction, List<ICAOAnnex14Surface> surfacesDefinitions) {
        Set<AnalysisSurface> analysisSurfaces = Sets.newHashSet();

        //1. Strip
        ICAOAnnex14SurfaceStrip strip = (ICAOAnnex14SurfaceStrip) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.STRIP).findFirst().get();
        analysisSurfaces.add(
                createStripAnalysisSurface(
                        direction,
                        strip
                )
        );

        //2. InnerHorizontal
        ICAOAnnex14SurfaceInnerHorizontal innerHorizontal = (ICAOAnnex14SurfaceInnerHorizontal) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.INNER_HORIZONTAL).findFirst().get();
        analysisSurfaces.add(
                createInnerHorizontalAnalysisSurface(
                        direction,
                        innerHorizontal,
                        strip)
        );

        //3. Conical
        ICAOAnnex14SurfaceConical conical = (ICAOAnnex14SurfaceConical) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.CONICAL).findFirst().get();
        analysisSurfaces.add(
                createConicalAnalysisSurface(
                        direction,
                        conical,
                        innerHorizontal,
                        strip
                )
        );

        //4. ApproachFirstSection
        ICAOAnnex14SurfaceApproach approach = (ICAOAnnex14SurfaceApproach) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.APPROACH).findFirst().get();
        ICAOAnnex14SurfaceApproachFirstSection approachFirstSection = (ICAOAnnex14SurfaceApproachFirstSection) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.APPROACH_FIRST_SECTION).findFirst().get();
        analysisSurfaces.add(
                createApproachFirstSectionAnalysisSurface(
                        direction,
                        approach,
                        approachFirstSection,
                        strip
                )
        );

        //4. ApproachSecondSection
        ICAOAnnex14SurfaceApproachSecondSection approachSecondSection = (ICAOAnnex14SurfaceApproachSecondSection) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.APPROACH_SECOND_SECTION).findFirst().get();
        analysisSurfaces.add(
                createApproachSecondSectionAnalysisSurface(
                        direction,
                        approachSecondSection,
                        approach,
                        approachFirstSection
                )
        );

        //5. Transitional
        ICAOAnnex14SurfaceTransitional transitional = (ICAOAnnex14SurfaceTransitional) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.TRANSITIONAL).findFirst().get();
        analysisSurfaces.add(
                createTransitionalAnalysisSurface(
                        direction,
                        transitional,
                        strip,
                        approachFirstSection,
                        innerHorizontal
                )
        );

        //6. TakeoffClimb
        ICAOAnnex14SurfaceTakeoffClimb takeoffClimb = (ICAOAnnex14SurfaceTakeoffClimb) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.TAKEOFF_CLIMB).findFirst().get();
        analysisSurfaces.add(
                createTakeoffClimbAnalysisSurface(
                        direction,
                        takeoffClimb
                )
        );

        //7. horizontal externa

        return analysisSurfaces;
    }

    private Set<AnalysisSurface> createAnalysisSurfacesForNonPrecision(RunwayDirection direction, List<ICAOAnnex14Surface> surfacesDefinitions) {
        //TODO
        throw new NotImplementedException();
    }

    private Set<AnalysisSurface> createAnalysisSurfacesForNonInstrument(RunwayDirection direction, List<ICAOAnnex14Surface> surfacesDefinitions) {

        Set<AnalysisSurface> analysisSurfaces = Sets.newHashSet();

        //1. Strip
        ICAOAnnex14SurfaceStrip strip = (ICAOAnnex14SurfaceStrip) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.STRIP).findFirst().get();
        analysisSurfaces.add(
                createStripAnalysisSurface(
                        direction,
                        strip
                )
        );

        //2. InnerHorizontal
        ICAOAnnex14SurfaceInnerHorizontal innerHorizontal = (ICAOAnnex14SurfaceInnerHorizontal) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.INNER_HORIZONTAL).findFirst().get();
        analysisSurfaces.add(
                createInnerHorizontalAnalysisSurface(
                        direction,
                        innerHorizontal,
                        strip)
        );

        //3. Conical
        ICAOAnnex14SurfaceConical conical = (ICAOAnnex14SurfaceConical) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.CONICAL).findFirst().get();
        analysisSurfaces.add(
                createConicalAnalysisSurface(
                        direction,
                        conical,
                        innerHorizontal,
                        strip
                )
        );

        //4. ApproachFirstSection
        ICAOAnnex14SurfaceApproach approach = (ICAOAnnex14SurfaceApproach) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.APPROACH).findFirst().get();
        ICAOAnnex14SurfaceApproachFirstSection approachFirstSection = (ICAOAnnex14SurfaceApproachFirstSection) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.APPROACH_FIRST_SECTION).findFirst().get();
        analysisSurfaces.add(
                createApproachFirstSectionAnalysisSurface(
                        direction,
                        approach,
                        approachFirstSection,
                        strip
                )
        );

        //5. Transitional
        ICAOAnnex14SurfaceTransitional transitional = (ICAOAnnex14SurfaceTransitional) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.TRANSITIONAL).findFirst().get();
        analysisSurfaces.add(
                createTransitionalAnalysisSurface(
                        direction,
                        transitional,
                        strip,
                        approachFirstSection,
                        innerHorizontal
                )
        );

        //6. TakeoffClimb
        ICAOAnnex14SurfaceTakeoffClimb takeoffClimb = (ICAOAnnex14SurfaceTakeoffClimb) surfacesDefinitions.stream().filter(d -> d.getEnum() == ICAOAnnex14Surfaces.TAKEOFF_CLIMB).findFirst().get();
        analysisSurfaces.add(
                createTakeoffClimbAnalysisSurface(
                        direction,
                        takeoffClimb
                )
        );

        //7. horizontal externa

        return analysisSurfaces;
    }

    private AnalysisSurface createStripAnalysisSurface(RunwayDirection direction, ICAOAnnex14SurfaceStrip stripSurface) {

        applyRuleException(stripSurface);

        stripSurface.setGeometry(geometryHelper.createStripSurfaceGeometry(direction, stripSurface));

        return AnalysisSurface.builder()
                .analysisCase(this.analysisCase)
                .surface(stripSurface)
                .direction(direction)
                .build();
    }

    private AnalysisSurface createInnerHorizontalAnalysisSurface(
            RunwayDirection direction,
            ICAOAnnex14SurfaceInnerHorizontal innerHorizontalSurface,
            ICAOAnnex14SurfaceStrip stripSurface
    ) {

        applyRuleException(innerHorizontalSurface);

        innerHorizontalSurface.setGeometry(geometryHelper.createInnerHorizontalSurfaceGeometry(direction, innerHorizontalSurface, stripSurface));

        return AnalysisSurface.builder()
                .analysisCase(this.analysisCase)
                .surface(innerHorizontalSurface)
                .direction(direction)
                .build();
    }

    private AnalysisSurface createConicalAnalysisSurface(
            RunwayDirection direction,
            ICAOAnnex14SurfaceConical conicalSurface,
            ICAOAnnex14SurfaceInnerHorizontal innerHorizontalSurface,
            ICAOAnnex14SurfaceStrip stripSurface
    ){

        //1. acondiciono superficie
        applyRuleException(conicalSurface);

        conicalSurface.setGeometry(geometryHelper.createConicalSurfaceGeometry(direction, conicalSurface, innerHorizontalSurface, stripSurface));

        conicalSurface.setInitialHeight(innerHorizontalSurface.getHeight());

        //2. creo el analisis de la superfie
        return AnalysisSurface.builder()
                .analysisCase(this.analysisCase)
                .surface(conicalSurface)
                .direction(direction)
                .build();
    }

    private AnalysisSurface createApproachFirstSectionAnalysisSurface(
            RunwayDirection direction,
            ICAOAnnex14SurfaceApproach approach,
            ICAOAnnex14SurfaceApproachFirstSection approachFirstSection,
            ICAOAnnex14SurfaceStrip strip
    ) {

        approachFirstSection.setInitialHeight(direction.getApproachSection().getThresholdElevation());

        approachFirstSection.setGeometry(geometryHelper.createApproachFirstSectionSurfaceGeometry(direction,approach, approachFirstSection, strip));

        return AnalysisSurface.builder()
                .analysisCase(this.analysisCase)
                .surface(approachFirstSection)
                .direction(direction)
                .build();
    }

    private AnalysisSurface createApproachSecondSectionAnalysisSurface(
            RunwayDirection direction,
            ICAOAnnex14SurfaceApproachSecondSection approachSecondSection,
            ICAOAnnex14SurfaceApproach approach,
            ICAOAnnex14SurfaceApproachFirstSection approachFirstSection) {

        double adjacent = approachFirstSection.getLength();
        double degrees = Math.atan(approachFirstSection.getSlope() / 100);
        double hypotenuse = adjacent / Math.cos(degrees);
        double opposite = Math.sqrt(Math.pow(hypotenuse,2) - Math.pow(adjacent,2));
        approachSecondSection.setInitialHeight(approachFirstSection.getInitialHeight() + opposite);

        approachSecondSection.setGeometry(geometryHelper.createApproachSecondSectionSurfaceGeometry(direction, approachSecondSection, approach, approachFirstSection));

        return AnalysisSurface.builder()
                .analysisCase(this.analysisCase)
                .surface(approachSecondSection)
                .direction(direction)
                .build();
    }

    private AnalysisSurface createTransitionalAnalysisSurface(
            RunwayDirection direction,
            ICAOAnnex14SurfaceTransitional transitional,
            ICAOAnnex14SurfaceStrip strip,
            ICAOAnnex14SurfaceApproachFirstSection approach,
            ICAOAnnex14SurfaceInnerHorizontal innerHorizontal)
    {
        transitional.setInitialHeight(direction.getHeight());

        double opposite = innerHorizontal.getHeight();
        double angle = Math.atan(transitional.getSlope()/100);
        double hypotenuse = opposite / Math.sin(angle);
        double adjacent = Math.sqrt(Math.pow(hypotenuse,2)-Math.pow(opposite,2));
        transitional.setWidth(adjacent);

        transitional.setGeometry(geometryHelper.createTransitionalSurfaceGeometry(direction, transitional, strip, approach, innerHorizontal));

        return AnalysisSurface.builder()
                .analysisCase(this.analysisCase)
                .surface(transitional)
                .direction(direction)
                .build();
    }

    private AnalysisSurface createTakeoffClimbAnalysisSurface(
            RunwayDirection direction,
            ICAOAnnex14SurfaceTakeoffClimb takeoffClimb)
    {
        takeoffClimb.setInitialHeight(direction.getHeight()); //TODO en realidad es el otro extremo

        takeoffClimb.setGeometry(geometryHelper.createTakeoffClimbSurfaceGeometry(direction, takeoffClimb));

        return AnalysisSurface.builder()
                .analysisCase(this.analysisCase)
                .surface(takeoffClimb)
                .direction(direction)
                .build();
    }

    private <T extends ICAOAnnex14Surface> void applyRuleException(T surface) {

        this.analysisCase.getRuleExceptions()
                .filter( s -> ((OlsRuleICAOAnnex14)s.getRule()).getSurface().equals(surface.getEnum()))
                .findFirst()
                .ifPresent(s -> applyRuleException(surface, s));
    }

    private <T extends ICAOAnnex14Surface> void applyRuleException(T surface, AnalysisExceptionRule exception) {

        String setter = String.format("set%s", StringUtils.capitalize(((OlsRuleICAOAnnex14)exception.getRule()).getPropertyCode()));

        try {
            surface.getClass().getMethod(setter,Double.class).invoke(surface,exception.getValue());

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
