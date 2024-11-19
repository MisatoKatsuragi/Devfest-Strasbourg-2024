package com.github.cardreader.sdk.reading.model.providers;

import java.io.IOException;

import android.nfc.Tag;

import com.github.cardreader.sdk.reading.model.Card;
import com.github.cardreader.sdk.reading.model.apdu.ApduResponse;

public class UnknownCard extends Card {
  public UnknownCard(Tag tag) throws IOException {
    super(tag);
  }

  @Override
  public ApduResponse sendSelect() {
    throw new UnsupportedOperationException("Unimplemented method 'sendSelect'");
  }

  @Override
  public ApduResponse sendGpo(String gpoPayload) {
    throw new UnsupportedOperationException("Unimplemented method 'sendGpo'");
  }

  @Override
  public String getName() {
    return "Unknown";
  }
}
