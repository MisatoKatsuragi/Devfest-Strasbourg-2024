package com.github.cardreader.sdk.reading.model;

import java.util.List;

import io.github.binaryfoo.DecodedData;

public record ReadData(String gpoPayload,
                       CardRecords cardRecords,
                       List<List<DecodedData>> decodedRecords,
                       List<DecodedData> decodedGpos,
                       List<DecodedData> decodedPpse,
                       String pan) {
}
