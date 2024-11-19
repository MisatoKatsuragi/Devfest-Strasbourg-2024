package com.github.cardreader.sdk.reading.model.apdu;

public class ApduCommand {
  private final ApduClaInsParameters header;
  private final String lc;
  private final String data;
  private final String le;

  public ApduCommand(ApduClaInsParameters header, String lc, String data, String le) {
    this.header = header;
    this.lc = lc;
    this.data = data;
    this.le = le;
  }

  @Override
  public String toString() {
    return header + lc + data + le;
  }
}
