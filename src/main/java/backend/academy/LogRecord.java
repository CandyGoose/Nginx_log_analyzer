package backend.academy;

import java.time.ZonedDateTime;

public class LogRecord {
    private final String ip;
    private final String user;
    private final ZonedDateTime time;
    private final String request;
    private final int status;
    private final int size;
    private final String referer;
    private final String agent;

    private LogRecord(Builder builder) {
        this.ip = builder.ip;
        this.user = builder.user;
        this.time = builder.time;
        this.request = builder.request;
        this.status = builder.status;
        this.size = builder.size;
        this.referer = builder.referer;
        this.agent = builder.agent;
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

    public static class Builder {
        private String ip;
        private String user;
        private ZonedDateTime time;
        private String request;
        private int status;
        private int size;
        private String referer;
        private String agent;

        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public Builder setTime(ZonedDateTime time) {
            this.time = time;
            return this;
        }

        public Builder setRequest(String request) {
            this.request = request;
            return this;
        }

        public Builder setStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder setReferer(String referer) {
            this.referer = referer;
            return this;
        }

        public Builder setAgent(String agent) {
            this.agent = agent;
            return this;
        }

        public LogRecord build() {
            return new LogRecord(this);
        }
    }
}
