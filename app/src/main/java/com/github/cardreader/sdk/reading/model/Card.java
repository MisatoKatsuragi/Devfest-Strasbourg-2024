package com.github.cardreader.sdk.reading.model;

import static com.github.cardreader.sdk.reading.Constants.AID_PPSE;
import static com.github.cardreader.sdk.reading.model.apdu.ApduClaInsParameters.READ_RECORD;
import static com.github.cardreader.sdk.reading.model.apdu.ApduClaInsParameters.SELECT;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import com.github.cardreader.sdk.reading.helper.HexHelper;
import com.github.cardreader.sdk.reading.helper.ArrayHelper;
import com.github.cardreader.sdk.reading.model.apdu.ApduCommand;
import com.github.cardreader.sdk.reading.model.apdu.ApduResponse;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;

import lombok.extern.java.Log;

@Log
public abstract class Card {
  private static final ApduCommand PPSE_INFO_COMMAND = new ApduCommand(SELECT, "0E", AID_PPSE, "00");

  private final IsoDep isoDep;
  private final HexHelper hexHelper = new HexHelper();
  private final ArrayHelper arrayHelper = new ArrayHelper();

  public String RID;

  protected Card(Tag tag) throws IOException {
    isoDep = IsoDep.get(tag);
    log.info("IsoDep tag : " + isoDep.getTag().toString());
    log.info("Card : setTimeout...");
    isoDep.setTimeout(5000);
    log.info("Card : tag isConnected ? " + isoDep.isConnected());
    log.info("Card : connect...");

    try {
      isoDep.connect();
    } catch (IOException e) {
      e.printStackTrace();
      isoDep.close();
    }
  }

  public abstract ApduResponse sendSelect() throws IOException, DecoderException;

  public abstract ApduResponse sendGpo(String gpoPayload)
      throws IOException, DecoderException;

  public ApduResponse send(byte[] apdu) throws IOException {
    int sw = 0x6100;
    int sw1 = 0x00;
    int sw2 = 0x00;
    byte[] data = new byte[0];
    byte[] resp = {};

    while ((sw & 0xff00) == 0x6100) {
      resp = isoDep.transceive(apdu);
      log.info("send data, APDU = " + hexHelper.hexToString(apdu));
      log.info("send data, response = " + hexHelper.hexToString(resp));

      if (resp.length == 0) {
        throw new IOException("Response is empty");
      }

      sw = ((0xff & resp[resp.length - 2]) << 8) | (0xff & resp[resp.length - 1]);
      sw1 = ((0xff & resp[resp.length - 2]) << 8);
      sw2 = (0xff & resp[resp.length - 1]);
      data = arrayHelper.concat(data, resp, resp.length - 2);
    }

    return new ApduResponse(sw, sw1, sw2, apdu, data, data.length, resp);
  }

  public String getName() {
    return "Chip Card";
  }

  public ApduResponse send(ApduCommand apdu) throws IOException, DecoderException {
    return send(hexHelper.stringToHex(apdu.toString()));
  }

  public ApduResponse sendSelectPpseAndGetCardScheme() throws IOException, DecoderException {
    return send(PPSE_INFO_COMMAND);
  }

  public ApduResponse sendReadRecord(String recordToRead) throws IOException, DecoderException {
    return send(new ApduCommand(READ_RECORD, recordToRead, "", ""));
  }

  public void closeIsoDep() throws IOException {
    isoDep.close();
  }
}
