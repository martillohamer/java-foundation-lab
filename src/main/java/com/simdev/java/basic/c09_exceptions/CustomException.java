package com.simdev.java.basic.c09_exceptions;

/*
Clase 71 - Excepciones personalizadas
Vídeo: https://youtu.be/JOAqpdM36wI?t=29181
*/

public class CustomException extends Exception {

    public CustomException(String message) {
        super(message);
    }
}
