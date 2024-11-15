package backend.academy;

import java.time.ZonedDateTime;

public class LogRecord {
    private String ip;
    private String user;
    private ZonedDateTime time;
    private String request;
    private int status;
    private int size;
    private String referer;
    private String agent;

    public LogRecord(String ip, String user, ZonedDateTime time, String request, int status, int size, String referer, String agent) {
        this.ip = ip;
        this.user = user;
        this.time = time;
        this.request = request;
        this.status = status;
        this.size = size;
        this.referer = referer;
        this.agent = agent;
    }

    public String getIp() {
        return ip;
    }

    public String getUser() {
        return user;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public String getRequest() {
        return request;
    }

    public int getStatus() {
        return status;
    }

    public int getSize() {
        return size;
    }

    public String getReferer() {
        return referer;
    }

    public String getAgent() {
        return agent;
    }

    public String getRequestMethod() {
        String[] parts = request.split(" ");
        return parts.length > 0 ? parts[0] : "";
    }

    public String getRequestResource() {
        String[] parts = request.split(" ");
        return parts.length > 1 ? parts[1] : "";
    }
}
