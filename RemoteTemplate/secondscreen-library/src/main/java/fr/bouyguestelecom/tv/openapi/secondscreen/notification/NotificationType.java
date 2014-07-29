package fr.bouyguestelecom.tv.openapi.secondscreen.notification;

/**
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public enum NotificationType {

    MESSAGE("Message"),
    USER_INPUT("UserInterface/RemoteController"),
    APPLICATIONS("Application"),
    MEDIA("Media");

    private String value;

    private NotificationType(String value) {
        this.value = value;
    }

    public static NotificationType valueFor(String value) {
        for (NotificationType notificationType : NotificationType.values()) {
            if (notificationType.toString() == value) {
                return notificationType;
            }
        }
        return null;
    }

    public String toString() {
        return value;
    }

}
