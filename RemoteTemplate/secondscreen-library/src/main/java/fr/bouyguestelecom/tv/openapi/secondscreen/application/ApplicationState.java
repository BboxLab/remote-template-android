package fr.bouyguestelecom.tv.openapi.secondscreen.application;

/**
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public enum ApplicationState {

    FOREGROUND("foreground"),
    BACKGROUND("background"),
    STOPPED("stopped"),
    UNKNOWN_STATE("unknown state");

    private String state;

    private ApplicationState(String state) {
        this.state = state;
    }

    public static ApplicationState valueForState(String state) {
        for (ApplicationState applicationState : ApplicationState.values()) {
            if (applicationState.toString().equals(state)) {
                return applicationState;
            }
        }
        return ApplicationState.UNKNOWN_STATE;
    }

    public String toString() {
        return state;
    }
}