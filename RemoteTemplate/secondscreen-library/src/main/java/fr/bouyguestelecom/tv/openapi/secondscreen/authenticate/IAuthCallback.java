package fr.bouyguestelecom.tv.openapi.secondscreen.authenticate;

/**
 * This callback is triggered at the end of the authentication process
 * Created by fab on 01/09/2014.
 */
public interface IAuthCallback {
    /**
     *
     * @param statusCode : the http status sent back either by the distant authentication PFS or by the bbox
     * @param reason : the reason why the authentication failed
     */
    public void onAuthResult(int statusCode, String reason);
}
