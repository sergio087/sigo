package ar.edu.utn.frba.proyecto.sigo.service.object;

import ar.edu.utn.frba.proyecto.sigo.domain.location.geographic.Region;
import ar.edu.utn.frba.proyecto.sigo.domain.location.political.PoliticalLocation;
import ar.edu.utn.frba.proyecto.sigo.domain.object.LightingTypes;
import ar.edu.utn.frba.proyecto.sigo.domain.object.MarkIndicatorTypes;
import ar.edu.utn.frba.proyecto.sigo.domain.object.PlacedObject;
import ar.edu.utn.frba.proyecto.sigo.domain.object.PlacedObjectOwner;
import ar.edu.utn.frba.proyecto.sigo.domain.object.PlacedObjectTypes;
import ar.edu.utn.frba.proyecto.sigo.dto.object.PlacedObjectDTO;
import ar.edu.utn.frba.proyecto.sigo.exception.InvalidParameterException;
import ar.edu.utn.frba.proyecto.sigo.service.Translator;
import ar.edu.utn.frba.proyecto.sigo.service.location.PoliticalLocationService;
import ar.edu.utn.frba.proyecto.sigo.service.location.RegionService;
import com.google.gson.Gson;

import javax.inject.Inject;
import java.util.Optional;

public class PlacedObjectTranslator extends Translator<PlacedObject, PlacedObjectDTO>{

    private PlacedObjectOwnerService ownerService;
    private PoliticalLocationService locationService;
    private RegionService regionService;

    @Inject
    public PlacedObjectTranslator(
            Gson gson,
            PlacedObjectOwnerService ownerService,
            PoliticalLocationService locationService,
            RegionService regionService
    ){
        this.locationService = locationService;
        this.regionService = regionService;
        this.objectMapper = gson;
        this.ownerService = ownerService;
    };

    @Override
    public PlacedObjectDTO getAsDTO(PlacedObject domain) {
        return PlacedObjectDTO.builder()
                    .id(domain.getId())
                    .heightAgl(domain.getHeightAgl())
                    .heightAmls(domain.getHeightAmls())
                    .lighting(domain.getLighting().ordinal())
                    .locationId(domain.getPoliticalLocation().getId())
                    .regionId(domain.getRegion().getId())
                    .markIndicator(domain.getMarkIndicator().ordinal())
                    .name(domain.getName())
                    .ownerId(domain.getOwner().getId())
                    .specId(domain.getSpecId())
                    .type(domain.getType().ordinal())
                    .subtype(domain.getSubtype())
                    .verified(domain.getVerified())
                    .temporary(domain.getTemporary())
                    .build();
    }

    @Override
    public PlacedObject getAsDomain(PlacedObjectDTO dto) {

        PlacedObject.PlacedObjectBuilder builder = PlacedObject.builder();

        // basic properties
        builder
                .id(dto.getId())
                .heightAgl(dto.getHeightAgl())
                .heightAmls(dto.getHeightAmls())
                .lighting(LightingTypes.values()[dto.getLighting()])
                .markIndicator(MarkIndicatorTypes.values()[dto.getMarkIndicator()])
                .name(dto.getName())
                .type(PlacedObjectTypes.values()[dto.getType()])
                .subtype(dto.getSubtype())
                .verified(dto.getVerified())
                .temporary(dto.getTemporary());

        // relation: owner
        PlacedObjectOwner owner = Optional
                .ofNullable(this.ownerService.get(dto.getOwnerId()))
                .orElseThrow(()-> new InvalidParameterException("ownerId == " + dto.getOwnerId()));

        builder.owner(owner);


        // relation: location
        PoliticalLocation location = Optional
                .ofNullable(this.locationService.get(dto.getLocationId()))
                .orElseThrow(()-> new InvalidParameterException("locationId == " + dto.getLocationId()));

        builder.politicalLocation(location);

        // relation: region
        Region region = Optional
                .ofNullable(this.regionService.get(dto.getRegionId()))
                .orElseThrow(()-> new InvalidParameterException("regionId == " + dto.getRegionId()));

        builder.region(region);

        // relation: spec
        //.specId(domain.getSpecId())

        return builder.build();
    }
}
