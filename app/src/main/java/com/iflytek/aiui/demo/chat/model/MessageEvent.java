package com.iflytek.aiui.demo.chat.model;

public class MessageEvent {
    public String question;
    public String answer;
    public String message;

    public MessageEvent(String question, String answer, String message) {
        this.question = question;
        this.answer = answer;
        this.message = message;
    }
}
