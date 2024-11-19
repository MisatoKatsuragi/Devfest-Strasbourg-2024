package com.github.cardreader.sdk.reading.model.apdu;

public record ApduResponse(int sw, int sw1, int sw2, byte[] command, byte[] data, int nr, byte[] rawResponse) {
}
