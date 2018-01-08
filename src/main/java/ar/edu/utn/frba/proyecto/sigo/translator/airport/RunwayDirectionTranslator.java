package ar.edu.utn.frba.proyecto.sigo.translator.airport;

import ar.edu.utn.frba.proyecto.sigo.domain.airport.Runway;
import ar.edu.utn.frba.proyecto.sigo.domain.airport.RunwayDirection;
import ar.edu.utn.frba.proyecto.sigo.domain.airport.RunwayDirectionPositions;
import ar.edu.utn.frba.proyecto.sigo.dto.airport.RunwayDirectionDTO;
import ar.edu.utn.frba.proyecto.sigo.exception.InvalidParameterException;
import ar.edu.utn.frba.proyecto.sigo.service.airport.RunwayService;
import ar.edu.utn.frba.proyecto.sigo.translator.Translator;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class RunwayDirectionTranslator extends Translator<RunwayDirection, RunwayDirectionDTO> {

    private RunwayService runwayService;

    @Inject
    public RunwayDirectionTranslator(
            Gson gson,
            RunwayService runwayService
    ){
        this.runwayService = runwayService;
        this.objectMapper = gson;
        this.dtoClass = RunwayDirectionDTO.class;
        this.domainClass = RunwayDirection.class;
    }

    @Override
    public RunwayDirectionDTO getAsDTO(RunwayDirection domain) {
        return RunwayDirectionDTO.builder()
                .id(domain.getId())
                .number(domain.getNumber())
                .position(domain.getPosition().ordinal())
                .runwayId(domain.getRunway().getId())
                .name(domain.getIdentifier())
                .azimuth(domain.getAzimuth())
                .height(domain.getHeight())
                .build();
    }

    @Override
    public RunwayDirection getAsDomain(RunwayDirectionDTO dto) {
        RunwayDirection.RunwayDirectionBuilder builder = RunwayDirection.builder();

        // basic properties
        builder
                .id(dto.getId())
                .number(dto.getNumber())
                .position(RunwayDirectionPositions.values()[dto.getPosition()])
                .azimuth(dto.getAzimuth())
                .height(dto.getHeight());

        // relation: runway
        Runway runway = Optional
                .ofNullable(runwayService.get(dto.getRunwayId()))
                .orElseThrow(()-> new InvalidParameterException("ruwnayId == " + dto.getRunwayId()));

        builder.runway(runway);

        return builder.build();
    }
}