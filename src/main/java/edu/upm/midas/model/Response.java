package edu.upm.midas.model;

import java.util.List;

/**
 * Created by gerardo on 26/09/2017.
 *
 * @author Gerardo Lagunes G. ${EMAIL}
 * @version ${<VERSION>}
 * @project tvp_rest
 * @className Response
 * @see
 */
public class Response {

    private String token;
    private boolean authorization;
    private String authorizationMessage;
    List<MatchNLP> validatedConcepts;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isAuthorization() {
        return authorization;
    }

    public void setAuthorization(boolean authorization) {
        this.authorization = authorization;
    }

    public String getAuthorizationMessage() {
        return authorizationMessage;
    }

    public void setAuthorizationMessage(String authorizationMessage) {
        this.authorizationMessage = authorizationMessage;
    }

    public List<MatchNLP> getValidatedConcepts() {
        return validatedConcepts;
    }

    public void setValidatedConcepts(List<MatchNLP> validatedConcepts) {
        this.validatedConcepts = validatedConcepts;
    }
}
