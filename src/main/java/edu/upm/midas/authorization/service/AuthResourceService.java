package edu.upm.midas.authorization.service;

import edu.upm.midas.authorization.model.ValidationResponse;

/**
 * Created by gerardo on 08/08/2017.
 *
 * @author Gerardo Lagunes G. ${EMAIL}
 * @version ${<VERSION>}
 * @project eidw
 * @className AuthResourceService
 * @see
 */
public interface AuthResourceService {

    ValidationResponse validationServiceByToken(String token);

}
