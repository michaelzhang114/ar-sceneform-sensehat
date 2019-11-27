package com.google.ar.sceneform.samples.hellosceneform;

public class ServerErrorException extends RuntimeException {
    public ServerErrorException() {
        super(String.format("Condition not found!"));
    }
}
