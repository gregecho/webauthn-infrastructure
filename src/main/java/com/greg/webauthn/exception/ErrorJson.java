package com.greg.webauthn.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorJson {

    private Integer status;
    private String  error;
    private String  message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    private Date timeStamp;

    private String              trace;
    private Map<String, String> messages;

    public static ErrorJsonBuilder newBuilder() {
        return new ErrorJsonBuilder();
    }

    public static class ErrorJsonBuilder {
        private final ErrorJson obj = new ErrorJson();

        public ErrorJsonBuilder setStatus(Integer status)               { obj.status    = status;    return this; }
        public ErrorJsonBuilder setError(String error)                  { obj.error     = error;     return this; }
        public ErrorJsonBuilder setMessage(String message)              { obj.message   = message;   return this; }
        public ErrorJsonBuilder setTimeStamp(Date ts)                   { obj.timeStamp = ts;        return this; }
        public ErrorJsonBuilder setTrace(String trace)                  { obj.trace     = trace;     return this; }
        public ErrorJsonBuilder setMessages(Map<String, String> msgs)   { obj.messages  = msgs;      return this; }

        public ErrorJson done() { return obj; }
    }
}
