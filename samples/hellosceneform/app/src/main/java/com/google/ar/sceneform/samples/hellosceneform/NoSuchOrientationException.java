package com.google.ar.sceneform.samples.hellosceneform;

public class NoSuchOrientationException extends RuntimeException {
    public NoSuchOrientationException() {
        super(String.format("No orientation!"));
    }
}
