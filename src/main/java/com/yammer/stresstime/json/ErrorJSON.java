package com.yammer.stresstime.json;

class ErrorMessage {
    String message;

    public ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

public class ErrorJSON {
    private ErrorMessage error;

    public ErrorJSON(String message) {
        this.error = new ErrorMessage(message);
    }

    public ErrorMessage getError() {
        return error;
    }
}
