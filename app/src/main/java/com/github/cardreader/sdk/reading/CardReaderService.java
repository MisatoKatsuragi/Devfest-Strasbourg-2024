package com.github.cardreader.sdk.reading;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import com.github.cardreader.sdk.reading.emv.EmvTagExtractor;
import com.github.cardreader.sdk.reading.helper.ChallengeHelper;
import com.github.cardreader.sdk.reading.helper.HexHelper;
import com.github.cardreader.sdk.exceptions.CardNotSupported;
import com.github.cardreader.sdk.exceptions.CardReaderException;
import com.github.cardreader.sdk.exceptions.WrongApduStatus;
import com.github.cardreader.sdk.reading.emv.EmvCardScheme;
import com.github.cardreader.sdk.reading.emv.EmvReader;
import com.github.cardreader.sdk.reading.model.CardRecords;
import com.github.cardreader.sdk.reading.model.DiscoverData;
import com.github.cardreader.sdk.reading.model.ReadData;
import com.github.cardreader.sdk.reading.model.Card;
import com.github.cardreader.sdk.reading.model.apdu.ApduResponse;
import com.github.cardreader.sdk.reading.model.providers.SodexoCard;
import com.github.cardreader.sdk.reading.model.providers.UnknownCard;
import com.github.cardreader.sdk.reading.model.providers.Paypass;
import com.github.cardreader.sdk.reading.model.providers.Paywave;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import io.github.binaryfoo.DecodedData;
import lombok.extern.java.Log;

@Log
public class CardReaderService {
  private final Tag tag;
  private final EmvReader emvReader;
  private final EmvTagExtractor emvTagExtractor;
  private final HexHelper hexHelper;
  private final ChallengeHelper challengeHelper = new ChallengeHelper();

  public CardReaderService(Tag tag) {
    this.tag = tag;
    this.emvReader = new EmvReader();
    this.emvTagExtractor = new EmvTagExtractor();
    this.hexHelper = new HexHelper();
  }

  /**
   * Etape 1 : select PPSE. Obtention de la liste des applications disponibles sur la carte.
   * Etape 2 : send select. Sélectionne l'application sans contact en fonction du type de carte
   * (Paywave pour Visa et Paypass pour MasterCard).
   */
  public DiscoverData firstContactWithNewCard()
      throws CardNotSupported, IOException, WrongApduStatus, DecoderException {
    if (!Arrays.asList(tag.getTechList()).contains(IsoDep.class.getName())) {
      throw new CardNotSupported("NFC Object not supported");
    }

    System.out.println(Arrays.toString(tag.getTechList()));

    Card card = new UnknownCard(tag);

    ApduResponse ppseResponse;
    EmvCardScheme cardScheme;

    try {
      ppseResponse = card.sendSelectPpseAndGetCardScheme();
      var response = hexHelper.hexToString(ppseResponse.data());

      cardScheme = EnumSet.allOf(EmvCardScheme.class)
              .stream()
              .filter(cardType -> response.contains(cardType.getAid()))
              .findFirst()
              .orElseThrow(() -> new CardNotSupported("Card is not supported (only Visa and MasterCard are supported), select PPSE response: " + hexHelper.hexToString(ppseResponse.rawResponse())));
    } catch (IOException e) {
      log.info("Card not supported, ending process.");
      throw new CardNotSupported(e.getMessage());
    } finally {
      card.closeIsoDep();
    }

    switch (cardScheme) {
      case MASTERCARD:
        card = new Paypass(tag);
        log.info("Mastercard card found!");
        break;
      case VISA:
        card = new Paywave(tag);
        log.info("Visa card found!");
        break;
      case SODEXO:
        card = new SodexoCard(tag);
        log.info("Sodexo card found!");
        break;
      default:
        log.warning("Unknown card found");
        card.closeIsoDep(); // remove ?
        throw new CardNotSupported("Unknown card found (only Visa and MasterCard (and Sodexo?) are supported)");
    }

    ApduResponse selectResponse = card.sendSelect();

    if ((selectResponse.sw() & 0x9000) == 0) {
      log.info("status: " + selectResponse.sw());
      card.closeIsoDep();
      throw new WrongApduStatus("Wrong status for select command: " + selectResponse.sw());
    }

    DiscoverData discoverData = new DiscoverData(ppseResponse, selectResponse, card, cardScheme);
    log.info("dataDiscoverDTO = " + discoverData);
    return discoverData;
  }

  /**
   * Etape 3 : Get processing options (obtention de la liste des enregistrements - AFL)
   * Etape 4 : Read records
   */
  public ReadData readCardAfl(DiscoverData discoverData)
          throws IOException, DecoderException, WrongApduStatus {
    List<DecodedData> decodedSelect = emvTagExtractor.decodeResponse(discoverData.selectResponse().data());
    String pdol = emvTagExtractor.getPdol(decodedSelect);
    String gpoPayload = "";

    if (!pdol.isEmpty()) {
      log.info("PDOL present, forging GPO...");
      gpoPayload = emvReader.forgeGpo(pdol);
    }

    log.info("GPOPayload generated: " + gpoPayload);

    ApduResponse gpoResponse = discoverData.card().sendGpo(gpoPayload);
    log.info("GPO Response: " + hexHelper.hexToString(gpoResponse.data()));

    if ((gpoResponse.sw() & 0x9000) == 0) {
      log.info("status: " + gpoResponse.sw());
      throw new WrongApduStatus("Wrong status for GPO command: " + gpoResponse.sw());
    }

    List<DecodedData> decodedGpos = emvTagExtractor.decodeResponse(gpoResponse.data());

    String afl = emvTagExtractor.getAfl(decodedGpos);
    CardRecords cardRecords = emvReader.readRecords(discoverData.card(), hexHelper.stringToHex(afl));
    List<ApduResponse> apduResponses = cardRecords.records();

    List<List<DecodedData>> decodedRecords = apduResponses
        .stream()
        .map(apdu -> emvTagExtractor.decodeResponse(apdu.data()))
        .peek(decodedData -> log.info("Decoded data: " + decodedData))
        .collect(Collectors.toList());

    List<DecodedData> decodedPpse = emvTagExtractor.decodeResponse(discoverData.ppseInfo().rawResponse());

    return new ReadData(
        gpoPayload,
        cardRecords,
        decodedRecords,
        decodedGpos,
        decodedPpse,
        emvTagExtractor.getPan(decodedRecords));
  }
}
