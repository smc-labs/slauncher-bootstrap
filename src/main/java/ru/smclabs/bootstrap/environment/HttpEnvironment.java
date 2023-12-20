package ru.smclabs.bootstrap.environment;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class HttpEnvironment {

    private final String userAgent;
    private final String hostname;

    private String zone;
    private String protocol;

    public HttpEnvironment(String version) {
        this.userAgent = "S-LAUNCHER@" + version;
        this.hostname = "slauncher.simpleminecraft.net";
        this.changeZone("ru");
        this.changeProtocol(this.hostname.startsWith("localhost") ? "http" : "https");
    }

    public void changeZone(String zone) {
        this.zone = "." + zone;
    }

    public void changeProtocol(String protocol) {
        this.protocol = protocol + "://";
    }

    public boolean isZoneChanged() {
        return zone.equals(".net");
    }

    public boolean isProtocolChanged() {
        return protocol.equals("http://");
    }
}
