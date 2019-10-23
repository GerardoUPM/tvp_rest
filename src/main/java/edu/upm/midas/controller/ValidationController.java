package edu.upm.midas.controller;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.upm.midas.authorization.token.service.TokenAuthorization;
import edu.upm.midas.model.MatchNLP;
import edu.upm.midas.model.Request;
import edu.upm.midas.model.Response;
import edu.upm.midas.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mobile.device.Device;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * Created by gerardo on 05/07/2017.
 *
 * @author Gerardo Lagunes G. ${EMAIL}
 * @version ${<VERSION>}
 * @project validation_medical_term
 * @className ValidationController
 * @see
 */
@RestController
@RequestMapping("${my.service.rest.request.mapping.general.url}")
public class ValidationController {

    private static final Logger logger = LoggerFactory.getLogger(ValidationController.class);



    @RequestMapping(path = { "${my.service.rest.request.mapping.validate.path}" }, //Term Validation Procedure
            method = RequestMethod.POST)
    public Response validate(@RequestBody @Valid Request request, HttpServletRequest httpRequest, Device device) throws Exception {
        ValidationService validationService = new ValidationService();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Response response = new Response();
        //System.out.println(httpRequest.getMethod());
        //Response response = tokenAuthorization.validateService(request.getToken(), httpRequest.getServletPath(), httpRequest.getServletPath(), device);
        //if (response.isAuthorized()) {
        if (request.getConcepts().size() > 0) {
            List<MatchNLP> conceptsValidated = validationService.doValidation(request.getConcepts());
            response.setValidatedConcepts(conceptsValidated);
            if (conceptsValidated.size() > 0){
//                System.out.println("OK");
                response.setToken(request.getToken());
                response.setAuthorized(true);
                response.setAuthorizationMessage("Authorization out of use");
                logger.info("Saving json...");
                validationService.writeJSONFile(gson.toJson(response), request);
                logger.info("Saving json ready!...");
            }
        }
        //}

        return response;
    }

    @RequestMapping(path = { "${my.service.rest.request.mapping.validate.test.path}" }, //Term Validation Procedure
            method = RequestMethod.POST)
    public Response validateTest(@RequestBody @Valid Request request, HttpServletRequest httpRequest, Device device) throws Exception {
        ValidationService validationService = new ValidationService();

        Response response = new Response();
        //System.out.println(httpRequest.getMethod());

        System.out.println("TEST...");
        System.out.println("Saving json...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        validationService.writeJSONFile(gson.toJson(response), request);
        System.out.println("Saving json ready!...");
        response.setToken(request.getToken());
        response.setAuthorized(true);
        response.setAuthorizationMessage("Authorization out of use");

        return response;
    }

}
