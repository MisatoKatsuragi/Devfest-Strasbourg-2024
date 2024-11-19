package com.github.cardreader.sdk.reading.model;

import com.github.cardreader.sdk.reading.emv.EmvCardScheme;
import com.github.cardreader.sdk.reading.model.apdu.ApduResponse;

public record DiscoverData(ApduResponse ppseInfo, ApduResponse selectResponse, Card card, EmvCardScheme cardScheme) {
}
