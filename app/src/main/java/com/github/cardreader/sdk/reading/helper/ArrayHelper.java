package com.github.cardreader.sdk.reading.helper;

public class ArrayHelper {
  public byte[] concat(byte[] a, byte[] b, int length) {
    byte[] res = new byte[a.length + length];
    System.arraycopy(a, 0, res, 0, a.length);
    System.arraycopy(b, 0, res, a.length, length);
    return res;
  }
}
