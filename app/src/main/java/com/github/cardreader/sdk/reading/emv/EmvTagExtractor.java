package com.github.cardreader.sdk.reading.emv;

import com.github.cardreader.sdk.reading.helper.HexHelper;
import com.github.cardreader.sdk.exceptions.CardReaderException;

import org.apache.commons.codec.DecoderException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.binaryfoo.DecodedData;
import io.github.binaryfoo.RootDecoder;
import io.github.binaryfoo.tlv.Tag;

public class EmvTagExtractor {
  private final RootDecoder rootDecoder;
  private final HexHelper hexHelper;

  public EmvTagExtractor() {
    this.rootDecoder = new RootDecoder();
    this.hexHelper = new HexHelper();
  }

  public List<DecodedData> decodeResponse(byte[] response) {
    return rootDecoder.decode(hexHelper.hexToString(response), "EMV", "constructed");
  }

  public String findForTag(List<List<DecodedData>> responses, NamedEmvTag namedEmvTag) {
    List<DecodedData> decodedDatas = responses.stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());

    return findForTagFromList(decodedDatas, namedEmvTag);
  }

  public String findForTagFromList(List<DecodedData> responses, NamedEmvTag namedEmvTag) {
    byte[] decodedTag;

    try {
      decodedTag = hexHelper.stringToHex(namedEmvTag.getId());
    } catch (DecoderException e) {
      throw new CardReaderException(e);
    }

    var tag = new Tag(decodedTag, true);

    return responses.stream()
        .map(decodedData -> DecodedData.findForTag(tag, decodedData.getChildren()))
        .filter(Objects::nonNull)
        .map(DecodedData::getFullDecodedData)
        .findFirst()
        .orElseGet(String::new);
  }

  public String getPdol(List<DecodedData> decodedSelect) {
    return findForTagFromList(decodedSelect, NamedEmvTag.PDOL);
  }

  public String getAip(List<DecodedData> decodedGpo) {
    return findForTagFromList(decodedGpo, NamedEmvTag.AIP);
  }

  public String getAfl(List<DecodedData> decodedGpo) {
    return findForTagFromList(decodedGpo, NamedEmvTag.AFL);
  }

  public String getPan(List<List<DecodedData>> decodedRecords) {
    return findForTag(decodedRecords, NamedEmvTag.PAN);
  }

}
