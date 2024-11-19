package com.github.cardreader.sdk.reading.emv;

import static com.github.cardreader.sdk.reading.Constants.AID_MASTERCARD;
import static com.github.cardreader.sdk.reading.Constants.AID_SODEXO;
import static com.github.cardreader.sdk.reading.Constants.AID_VISA;
import static com.github.cardreader.sdk.reading.Constants.RID_MASTERCARD;
import static com.github.cardreader.sdk.reading.Constants.RID_SODEXO;
import static com.github.cardreader.sdk.reading.Constants.RID_VISA;

import lombok.Getter;

@Getter
public enum EmvCardScheme {
  MASTERCARD(RID_MASTERCARD, AID_MASTERCARD),
  VISA(RID_VISA, AID_VISA),
  SODEXO(RID_SODEXO, AID_SODEXO);

  private final String rid;
  private final String aid;

  EmvCardScheme(String rid, String aid) {
    this.rid = rid;
    this.aid = aid;
  }
}
