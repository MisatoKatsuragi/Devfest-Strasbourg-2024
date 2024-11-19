package com.github.cardreader.sdk.reading.model.apdu;

import lombok.Getter;

@Getter
public enum ApduClaInsParameters {
  SELECT("00", "A4", "04", "00"),
  READ_RECORD("00", "B2"),
  GPO("80", "A8", "00", "00"),
  GENERATE_AC("80", "AE", "90", "00"),
  ICC("00", "84", "00", "00");

  private final String cla;
  private final String ins;
  private final String p1;
  private final String p2;

  ApduClaInsParameters(String cla, String ins, String p1, String p2) {
    this.cla = cla;
    this.ins = ins;
    this.p1 = p1;
    this.p2 = p2;
  }

  ApduClaInsParameters(String cla, String ins) {
    this(cla, ins, "", "");
  }

  @Override
  public String toString() {
    return cla + ins + p1 + p2;
  }
}
