package com.github.cardreader.sdk.reading.model.providers;

import static com.github.cardreader.sdk.reading.model.apdu.ApduClaInsParameters.GPO;
import static com.github.cardreader.sdk.reading.model.apdu.ApduClaInsParameters.SELECT;

import android.nfc.Tag;

import com.github.cardreader.sdk.reading.model.Card;
import com.github.cardreader.sdk.reading.model.apdu.ApduCommand;
import com.github.cardreader.sdk.reading.model.apdu.ApduResponse;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;

public class Paywave extends Card {
  public Paywave(Tag tag) throws IOException {
    super(tag);
    RID = "A000000003";
  }

  @Override
  public ApduResponse sendSelect() throws IOException, DecoderException {
    return send(new ApduCommand(SELECT, "07", RID + "1010", "00"));
  }

  @Override
  public ApduResponse sendGpo(String gpoPayload) throws IOException, DecoderException {
    String payloadLength = Integer.toHexString((gpoPayload.length() + 8) / 2);
    String payloadWithInsLength = Integer.toHexString((gpoPayload.length() + 12) / 2);
    String unpredictableNumber = "00000000"; // no need to perform an authentication

    return send(new ApduCommand(
        GPO,
        payloadWithInsLength,
        "83" + payloadLength + gpoPayload + unpredictableNumber,
        "00"));
  }

  @Override
  public String getName() {
    return "Visa";
  }
}
