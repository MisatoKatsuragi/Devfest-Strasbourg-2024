package com.github.cardreader.sdk.exceptions;

public class CardReaderException extends RuntimeException {
    public CardReaderException(String message) {
        super(message);
    }
    public CardReaderException(Exception exception) {
        super(exception);
    }
}
