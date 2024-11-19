package com.github.cardreader.sdk.reading.model.providers;

import static com.github.cardreader.sdk.reading.model.apdu.ApduClaInsParameters.GPO;
import static com.github.cardreader.sdk.reading.model.apdu.ApduClaInsParameters.SELECT;

import android.nfc.Tag;

import com.github.cardreader.sdk.reading.model.Card;
import com.github.cardreader.sdk.reading.model.apdu.ApduCommand;
import com.github.cardreader.sdk.reading.model.apdu.ApduResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class Paypass extends Card {
  public Paypass(Tag tag) throws IOException {
    super(tag);
    RID = "A000000004";
  }

  @Override
  public ApduResponse sendSelect() throws IOException, DecoderException {
    return send(new ApduCommand(SELECT, "07", RID + "1010", "00"));
  }

  @Override
  public ApduResponse sendGpo(String gpoPayload) throws IOException, DecoderException {
    String payloadLength = StringUtils.leftPad(Integer.toHexString((gpoPayload.length()) / 2), 2, "0");
    String lc = StringUtils.leftPad(Integer.toHexString((gpoPayload.length() + 4) / 2), 2, "0");
    return send(new ApduCommand(GPO, lc, "83" + payloadLength + gpoPayload, "00"));
  }

  @Override
  public String getName() {
    return "MasterCard";
  }
}
