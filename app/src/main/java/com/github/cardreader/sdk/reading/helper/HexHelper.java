package com.github.cardreader.sdk.reading.helper;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class HexHelper {
  public String hexToString(byte[] data) {
    return new String(Hex.encodeHex(data)).toUpperCase();
  }

  public byte[] stringToHex(String data) throws DecoderException {
    return Hex.decodeHex(data.toCharArray());
  }
}
