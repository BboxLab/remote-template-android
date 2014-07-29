package fr.bouyguestelecom.tv.openapi.secondscreen.remote;

/**
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public enum KeyName {
    TV_DEC("TV/DEC"),
    VOD("VOD"),
    MATV("M@TV"),
    GUIDE("GUIDE"),
    LIST("LIST"),
    EXIT("EXIT"),
    AV("AV"),
    SLEEP("SLEEP"),
    HOME("HOME"),
    V_PLUS("V+"),
    MUTE("MUTE"),
    V_MOINS("V-"),
    P_MOINS("P-"),
    P_PLUS("P+"),
    UP("UP"),
    DOWN("DOWN"),
    LEFT("LEFT"),
    RIGHT("RIGHT"),
    OK("OK"),
    BACK("BACK"),
    ZOOM("ZOOM"),
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    PLAY("PLAY"),
    STOP("STOP"),
    FF("FF"),
    RW("RW"),
    REC("REC"),
    INFO("INFO"),
    SPACE("SPACE");

    private String value;

    private KeyName(String value) {
        this.value = value;
    }

    public static KeyName valueFor(String value) {
        for (KeyName remoteKey : KeyName.values()) {
            if (remoteKey.toString() == value) {
                return remoteKey;
            }
        }
        return null;
    }

    public String toString() {
        return value;
    }
}
