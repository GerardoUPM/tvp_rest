package edu.upm.midas.service;

import com.google.gson.Gson;
import edu.upm.midas.constants.Constants;
import edu.upm.midas.model.*;
import edu.upm.midas.common.util.StaticUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerardo on 04/07/2017.
 *
 * @author Gerardo Lagunes G. ${EMAIL}
 * @version ${<VERSION>}
 * @project validation_medical_term
 * @className ValidationService
 * @see
 */
@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);


    /**
     * Método para realizar la validación.
     *
     * @param conceptList
     * @return
     * @throws Exception
     */
    public List<MatchNLP> doValidation(List<Concept> conceptList) throws Exception {//
        FindingsManager findingsManager = new FindingsManager();
        List<ValidationFinding> validationFindings = findingsManager.loadAllFindings( );
        List<MatchNLP> matchNLPList = new ArrayList<>();
        int conceptCount = 1;int conceptListSize = conceptList.size();
        for (Concept concept: conceptList) {
            logger.info(conceptCount +". to " + conceptListSize + " => " + concept.getCui() + " | " + concept.getName());
            matchNLPList.add( validate(concept, validationFindings) );
            conceptCount++;
        }
        return matchNLPList;
    }

    public String getFilePath(){
        return getClass().getResource(Constants.VALIDATION_FILE).getPath();
    }


    /**
     * Método para validar un término encontrado por NLP..
     *
     * @param concept
     *            Recibe el término.
     * @param validationFindings
     *            Recibe lista de findings.
     * @return Devuelve la lista de matches encontrados.
     * @throws Exception
     *             Puede lanzar una excepción.
     */
    public MatchNLP validate(Concept concept, List<ValidationFinding> validationFindings) throws Exception{
        // tenemos el finding obtenido por NLP
        // recorremos la lista de findings con los que validar
        //
        boolean validated = false;
        MatchNLP matchNLP = new MatchNLP( concept );
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < validationFindings.size(); i++) {
            ValidationFinding validationFinding = validationFindings.get(i);
            if (validationFinding.getCuis().contains(concept.getCui())) {
                // Si se valida por CUI.. salimos.
                String cuiV = "NLPF[" + concept.getName() + "]\tVF["
                        + validationFinding.getName() + "][" + validationFinding.getSource() + "]";
//                System.out.println("\t\tValidated: " + cuiV + " - CUI METHOD");
                Match match = new Match(validationFinding, validationFinding.getName(), "CUI"); 					/*CREA UN MATCH SIEMPRE:(isMatch=TRUE)*/
                matches.add(match);
                validated = true;
            } else {
                // si no se valida por CUI, intentamos validar por exactitud en
                // el nombre del concept
                // con el nombre del vf o sus sinonimos
                BooleanAndString validateVFAndSynonyms = equalsVFNameOrSynonymsWithNLPFinding(
                        validationFinding, concept);
                if (validateVFAndSynonyms.getBool()) {
                    String eqV = "NLPF[" + concept.getName()
                            + "]\tValidation via equals\tVF["
                            + validateVFAndSynonyms.getString() + "][" + validationFinding.getSource() + "]";
//                    System.out.println("\t\tValidated: " + eqV + " - EQUALS METHOD");
                    Match match = new Match(validationFinding,
                            validateVFAndSynonyms.getString(), "Equals"); 					/*CREA UN MATCH*/
                    matches.add(match);
                    validated = true;
                } else {
                    // si no hay validación por coincidencia exacta, usamos
                    // algoritmos de similitud
                    Match match = validateBySimilarityAlgorithms(validationFinding, concept);
                    if (match != null){
                        matches.add(match);
                        validated = true;
                    }
                }
            }
        }
        matchNLP.setMatches(matches); /* this.hasMatches = matches.size() > 0; */
        if (!validated) {
//            System.out.println("\t\tNot validated!");
        }

        return matchNLP;
    }


    /**
     * @param validationFinding
     * @param concept
     * @return
     */
    public Match validateBySimilarityAlgorithms(ValidationFinding validationFinding, Concept concept){
        Match match = null;
        BooleanAndString validateVFAndSynonymsWithSimilarity = hasEnoughSimilarityNameOrSynonymsWithNLPFinding(
                validationFinding, concept, new Levenshtein());
        if (validateVFAndSynonymsWithSimilarity.getBool()) {
            String eqV = "NLPF["
                    + concept.getName()
                    + "]\tValidation via similarity\tVF["
                    + validateVFAndSynonymsWithSimilarity
                    .getString() + "][" + validationFinding.getSource() + "]";
//            System.out.println("\t\tValidated: " + eqV + " - SIMILARITY METHOD");
            match = new Match(
                    validationFinding,
                    validateVFAndSynonymsWithSimilarity.getString(), "Similarity");
        }
        return match;
    }


    /**
     * Método para saber si un término de validación (o sus sinónimos) es
     * equivalente (equals) al término de NLP.
     *
     * @param vf
     *            Recibe el término de validación.
     * @param concept
     *            Recibe el término NLP.
     * @return Devuelve un objeto.
     */
    private BooleanAndString equalsVFNameOrSynonymsWithNLPFinding(ValidationFinding vf, Concept concept) {
        String vfString = StaticUtils.removeStopWords(vf.getName());
        vfString = StaticUtils.orderWords(vfString);
        String nlpFindingString = StaticUtils.removeStopWords(concept.getName());
        nlpFindingString = StaticUtils.orderWords(nlpFindingString);
        if (vfString.equalsIgnoreCase(nlpFindingString)) {
            return new BooleanAndString(true, vf.getName() + " {" + vfString + "}");
        } else {
            for (int i = 0; i < vf.getSynonyms().size(); i++) {
                String vfSynom = StaticUtils.removeStopWords(vf.getSynonyms().get(i));
                vfSynom = StaticUtils.orderWords(vfSynom);
                if (vfSynom.equalsIgnoreCase(nlpFindingString)) {
                    return new BooleanAndString(true, vf.getSynonyms().get(i) + " {" + vfSynom + "}");
                }
            }
        }
        return new BooleanAndString(false, "");
    }


    /**
     * Método para saber si un criterio de validación (el primario o sus
     * sinonimos) tiene suficiente similitud con el término NLP.
     *
     * @param vf
     *            Recibe el término de validación.
     * @param concept
     *            Recibe el término NLP.
     * @param metric
     *            Recibe la métrica a usar para la similitud.
     * @return Devuelve un objeto.
     */
    private BooleanAndString hasEnoughSimilarityNameOrSynonymsWithNLPFinding(ValidationFinding vf, Concept concept, AbstractStringMetric metric) {

        String vfString = StaticUtils.removeStopWords(vf.getName());
        vfString = StaticUtils.orderWords(vfString);
        String nlpFindingString = StaticUtils.removeStopWords(concept.getName());
        nlpFindingString = StaticUtils.orderWords(nlpFindingString);

        float result = metric.getSimilarity(vfString, nlpFindingString);
        if (result > Constants.SIMILARITY_MIN_VALUE) {
            return new BooleanAndString(true, vf.getName() + " {" + vfString + "}");
        } else {
            for (int i = 0; i < vf.getSynonyms().size(); i++) {
                String vfSynom = StaticUtils.removeStopWords(vf.getSynonyms().get(i));
                vfSynom = StaticUtils.orderWords(vfSynom);
                result = metric.getSimilarity(vfSynom, nlpFindingString);
                if (result > Constants.SIMILARITY_MIN_VALUE) {
                    return new BooleanAndString(true, vf.getSynonyms().get(i) + " {" + vfSynom + "}");
                }
            }
        }
        return new BooleanAndString(false, "");
    }

    /**
     * @param jsonBody
     * @param request
     * @throws IOException
     */
    public void writeJSONFile(String jsonBody, Request request) throws IOException {
        String fileName = request.getSnapshot() + Constants.UNDER_SCORE + request.getSource() + Constants.TVP_RETRIEVAL_FILE_NAME + Constants.DOT_JSON;
        String path = Constants.TVP_RETRIEVAL_HISTORY_FOLDER + fileName;
        try {
            InputStream in = getClass().getResourceAsStream(path);
            //BufferedReader bL = new BufferedReader(new InputStreamReader(in));
            File file = new File(path);
            BufferedWriter bW;

            if (!file.exists()) {
                bW = new BufferedWriter(new FileWriter(file));
                bW.write(jsonBody);
                bW.close();
            }
        }catch (Exception e){
            logger.error("Error while writing json file {} ", path, e);
        }
    }


    /**
     * @param snapshot
     * @param source
     * @return
     * @throws Exception
     */
    public Response readJSONFile(String snapshot, String source) throws Exception {
        Response response = new Response();
        Gson gson = new Gson();
        String fileName = snapshot + Constants.UNDER_SCORE + source + Constants.TVP_RETRIEVAL_FILE_NAME + Constants.DOT_JSON;
        String path = Constants.TVP_RETRIEVAL_HISTORY_FOLDER + fileName;
        logger.info("Read JSON!..." + path);

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            response = gson.fromJson(br, Response.class);
        }catch (Exception e){
            logger.error("Error to read or convert JSON! ({})", path, e);
        }

        /*for (edu.upm.midas.data.validation.metamap.model.response.Text text: resp.getTexts()) {
            System.out.println("TextId: " + text.getId() + " | Concepts: " + text.getConcepts().toString());
        }*/
        //System.out.println("source: "+source);

        return response;
    }


}
