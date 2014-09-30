package fr.bouyguestelecom.tv.openapi.secondscreen.httputils;

/**
 * Interface to get Http status code responded by the BboxAPI
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public interface CallbackHttpStatus {
    public void onResult(int status);
}