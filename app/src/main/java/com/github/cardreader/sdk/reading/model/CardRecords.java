package com.github.cardreader.sdk.reading.model;

import com.github.cardreader.sdk.reading.model.apdu.ApduResponse;

import java.util.List;

public record CardRecords(List<ApduResponse> records) {
}
