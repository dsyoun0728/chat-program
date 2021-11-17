package parser;

import util.Constants;

import java.util.ArrayList;

public class ResponseParser implements Parser {
    @Override
    public ParsedMsg parseMessage(ArrayList<byte[]> byteArrayList) {
        byte[] optionalInfo = Parser.getOptionalInfo(byteArrayList);
        return new ParsedMsg(
                Parser.getUUID(byteArrayList.get(0)),
                Parser.getFunctionName(byteArrayList.get(0)),
                Parser.getContents(byteArrayList),
                optionalInfo,
                Parser.getTotalPacketNumber(optionalInfo),
                Constants.getStatus(byteArrayList.get(0)[Constants.PACKET_TOTAL_PACKET_NUMBER_SIZE])
        );
    }
}
