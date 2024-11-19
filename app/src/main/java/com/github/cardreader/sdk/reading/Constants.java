package com.github.cardreader.sdk.reading;

import com.github.cardreader.sdk.reading.helper.HexHelper;

public class Constants {
  private static final HexHelper hexHelper = new HexHelper();
  private static final String PPSE_CONTACTLESS = "2PAY.SYS.DDF01";

  // ISO 3166-1
  public static final String ISO_FRANCE_NUM_CODE = "0250";

  // RID
  public static final String RID_MASTERCARD = "A000000004";
  public static final String RID_VISA       = "A000000003";
  public static final String RID_SODEXO     = "D250000012";

  // AID
//  public static final String AID_PPSE = "325041592E5359532E4444463031";
  public static final String AID_PPSE = hexHelper.hexToString(PPSE_CONTACTLESS.getBytes());
  public static final String AID_MASTERCARD = "A0000000041010";
  public static final String AID_VISA = "A0000000031010";
  public static final String AID_SODEXO     = "D2500000121010";
}
