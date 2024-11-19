package com.github.cardreader.sdk.reading.model.providers;

import static com.github.cardreader.sdk.reading.Constants.AID_SODEXO;
import static com.github.cardreader.sdk.reading.Constants.RID_SODEXO;
import static com.github.cardreader.sdk.reading.model.apdu.ApduClaInsParameters.GPO;
import static com.github.cardreader.sdk.reading.model.apdu.ApduClaInsParameters.SELECT;

import android.nfc.Tag;

import com.github.cardreader.sdk.reading.model.Card;
import com.github.cardreader.sdk.reading.model.apdu.ApduCommand;
import com.github.cardreader.sdk.reading.model.apdu.ApduResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class SodexoCard extends Card {
  public SodexoCard(Tag tag) throws IOException {
    super(tag);
    RID = RID_SODEXO;
  }

  @Override
  public ApduResponse sendSelect() throws IOException, DecoderException {
    return send(new ApduCommand(SELECT, "07", AID_SODEXO, "00"));
  }

  @Override
  public ApduResponse sendGpo(String gpoPayload) throws IOException, DecoderException {
    String payloadLength = StringUtils.leftPad(Integer.toHexString((gpoPayload.length()) / 2), 2, "0");
    String lc = StringUtils.leftPad(Integer.toHexString((gpoPayload.length() + 4) / 2), 2, "0");
    return send(new ApduCommand(GPO, lc, "83" + payloadLength + gpoPayload, "00"));
  }

  @Override
  public String getName() {
    return "Sodexo";
  }
}
