package com.github.cardreader.sdk.reading.emv;

import static java.util.logging.Level.INFO;

import com.github.cardreader.sdk.reading.Constants;
import com.github.cardreader.sdk.reading.helper.HexHelper;
import com.github.cardreader.sdk.reading.model.Card;
import com.github.cardreader.sdk.reading.model.CardRecords;
import com.github.cardreader.sdk.reading.model.apdu.ApduResponse;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.binaryfoo.DecodedData;
import io.github.binaryfoo.RootDecoder;
import io.github.binaryfoo.decoders.DOLParser;
import lombok.extern.java.Log;

@Log
public class EmvReader {
  private final RootDecoder rootDecoder;
  private final HexHelper hexHelper;

  public EmvReader() {
    this.rootDecoder = new RootDecoder();
    this.hexHelper = new HexHelper();
  }

  /*
   * @see "https://paymentcardtools.com/emv-tag-decoders/ttq"
   */
  public String forgeGpo(String pdol) throws DecoderException {
    var dolParser = new DOLParser();
    var sb = new StringBuilder();

    var dolElements = dolParser.parse(hexHelper.stringToHex(pdol));
    log.log(INFO, "PDOL elements : {0}", dolElements);

    for (var dolElement : dolElements) {
      var tag = dolElement.getTag().toString();
      var length = dolElement.getLength();
      var emvTag = NamedEmvTag.emvTagOptional(tag).orElse(null);

      if (emvTag == null) {
        sb.append(zeroPadding(length));
        continue;
      }

      switch (emvTag) {
        case TERMINAL_TRANSACTION_QUALIFIERS: {
          sb.append("21000000");
          break;
        }
        case TERMINAL_COUNTRY_CODE:
        case TRANSACTION_CURRENCY_CODE: {
          sb.append(Constants.ISO_FRANCE_NUM_CODE);
          break;
        }
        case AMOUNT_AUTHORISED_NUMERIC: {
          sb.append("000000000001");
          break;
        }
        case UNPREDICTABLE_NUMBER:
          // Do nothing
          break;
        default:
          sb.append(zeroPadding(length));
      }
    }

    return sb.toString();
  }

  private String zeroPadding(int length) {
    var count = Math.max(0, length);
    return "00".repeat(count);
  }

  public CardRecords readRecords(Card card, byte[] afl) throws IOException, DecoderException {
    StringBuilder sb = new StringBuilder();
    log.info("Reading Records from AFL...");
    int numberOfSfi = afl.length / 4;
    log.info("Number of SFI : " + numberOfSfi);
    List<ApduResponse> apduResponses = new ArrayList<>();

    for (int i = 0; i < numberOfSfi; i++) {
      int sfi = (afl[i * 4] & 0xFF);
      log.info("SFI: " + sfi);
      int startRecord = (afl[i * 4 + 1] & 0xFF);
      int endRecord = (afl[i * 4 + 2] & 0xFF);
      int authRange = (afl[i * 4 + 3] & 0xFF);

      for (int j = startRecord; j <= endRecord; j++) {
        String recordNumber = Integer.toHexString(j);
        recordNumber = recordNumber.length() == 1 ? "0" + recordNumber : recordNumber;
        String sfiFlag = Integer.toHexString(sfi + 4);
        sfiFlag = sfiFlag.length() == 1 ? "0" + sfiFlag : sfiFlag;

        ApduResponse apduResponse = card.sendReadRecord(recordNumber + sfiFlag + "00");
        apduResponses.add(apduResponse);

        // Book 3 section 10.2 & 10.3
        if ((j - startRecord) < authRange) {
          log.info("Adding response to authData...");
          var response = apduResponse.data();

          if ((sfi >> 3) > 0x10) {
            sb.append(hexHelper.hexToString(response));
          } else {
            sb.append(formatAuthString(response));
          }
        }
      }
    }

    return new CardRecords(apduResponses);
  }

  public List<DecodedData> decodeResponse(byte[] response) {
    return rootDecoder.decode(hexHelper.hexToString(response), "EMV", "constructed");
  }

  public String formatAuthString(byte[] authBytes) {
    List<DecodedData> decodedData = decodeResponse(authBytes);
    return decodedData.get(0).getFullDecodedData();
  }
}
