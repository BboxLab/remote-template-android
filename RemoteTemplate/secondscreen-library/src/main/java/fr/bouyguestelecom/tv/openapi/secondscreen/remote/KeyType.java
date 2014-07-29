package fr.bouyguestelecom.tv.openapi.secondscreen.remote;

/**
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public enum KeyType {

    KEY_PRESSED("keypressed"),
    KEY_DOWN("keydown"),
    KEY_UP("keyup");

    private String value;

    private KeyType(String value) {
        this.value = value;
    }

    public static KeyType valueFor(String value) {
        for (KeyType keyType : KeyType.values()) {
            if (keyType.toString() == value) {
                return keyType;
            }
        }
        return null;
    }

    public String toString() {
        return value;
    }

}
