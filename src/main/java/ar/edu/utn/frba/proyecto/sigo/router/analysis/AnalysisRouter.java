package ar.edu.utn.frba.proyecto.sigo.router.analysis;

import ar.edu.utn.frba.proyecto.sigo.domain.analysis.Analysis;
import ar.edu.utn.frba.proyecto.sigo.domain.analysis.AnalysisStages;
import ar.edu.utn.frba.proyecto.sigo.exception.MissingParameterException;
import ar.edu.utn.frba.proyecto.sigo.persistence.HibernateUtil;
import ar.edu.utn.frba.proyecto.sigo.router.SigoRouter;
import ar.edu.utn.frba.proyecto.sigo.service.analysis.AnalysisService;
import ar.edu.utn.frba.proyecto.sigo.service.analysis.AnalysisTranslator;
import ar.edu.utn.frba.proyecto.sigo.spark.JsonTransformer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import spark.Route;
import spark.RouteGroup;

import javax.inject.Inject;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.patch;
import static spark.Spark.post;

public class AnalysisRouter extends SigoRouter {

    private JsonTransformer jsonTransformer;
    private AnalysisService analysisService;
    private AnalysisTranslator analysisTranslator;

    @Inject
    public AnalysisRouter(
            HibernateUtil hibernateUtil,
            Gson objectMapper,
            JsonTransformer jsonTransformer,
            AnalysisService analysisService,
            AnalysisTranslator analysisTranslator
    ){
        super(objectMapper, hibernateUtil);

        this.jsonTransformer = jsonTransformer;
        this.analysisService = analysisService;
        this.analysisTranslator = analysisTranslator;
    }

    /**
     * Search analysis instances filtered by parameters
     */
    private final Route searchAnalysis = doInTransaction(false, (request, response) -> {
        return this.analysisService.find(request.queryMap())
                .stream()
                .map(c -> analysisTranslator.getAsDTO(c))
                .collect(Collectors.toList());
    });

    /**
     * Create a case depending on older case.
     */
    private final Route createAnalysis = doInTransaction(true, (request, response) -> {

        JsonObject jsonObject = objectMapper.fromJson(request.body(), JsonObject.class);

        if(!jsonObject.has("parentId"))
            throw new MissingParameterException("parentId");

        Analysis baseCase = this.analysisService.get(jsonObject.get("parentId").getAsLong());

        Analysis analysisCase = this.analysisService.create(new Analysis(), baseCase);

        return analysisTranslator.getAsDTO(analysisCase);
    });


    /**
     * Get an analysis instance by its identifier
     */
    private final Route fetchAnalysis = doInTransaction(false, (request, response) -> {

        Analysis analysis = this.analysisService.get(getParamAnalysisId(request));

        return analysisTranslator.getAsDTO(analysis);
    });

    /**
     * Update analysis' status
     */
    private final Route updateAnalysis = doInTransaction(true, (request, response) -> {

        Analysis analysis = this.analysisService.get(getParamAnalysisId(request));

        JsonObject body = objectMapper.fromJson(request.body(), JsonObject.class);

        if(!body.has("stageId"))
            throw new MissingParameterException("stageId");

        AnalysisStages newStage = AnalysisStages.values()[body.get("stageId").getAsInt()];

        this.analysisService.changeStatus(analysis, newStage);

        return analysisTranslator.getAsDTO(analysis);
    });

    @Override
    public RouteGroup routes() {
        return ()->{
            post("", createAnalysis, jsonTransformer);
            get("", searchAnalysis, jsonTransformer);

            get("/:" + ANALYSIS_ID_PARAM, fetchAnalysis, jsonTransformer);
            patch("/:" + ANALYSIS_ID_PARAM, updateAnalysis, jsonTransformer);
        };
    }

    @Override
    public String path() {
        return "/analysis";
    }
}
