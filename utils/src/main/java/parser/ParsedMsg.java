package parser;

import java.util.UUID;

public class ParsedMsg {
    private UUID uuid;
    private String functionName;
    private int contentsLength;
    private byte[] contents;
    private byte[] optionalInfo;
    private int totalPacketNumber;
    private String responseStatus;

    public ParsedMsg(UUID uuid, String functionName, byte[] contents, byte[] optionalInfo, int totalPacketNumber, String responseStatus) {
        this.uuid = uuid;
        this.functionName = functionName;
        this.contentsLength = contents.length;
        this.contents = contents;
        this.optionalInfo = optionalInfo;
        this.totalPacketNumber = totalPacketNumber;
        this.responseStatus = responseStatus;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getFunctionName() {
        return functionName;
    }

    public int getContentsLength() {
        return contentsLength;
    }

    public byte[] getContents() {
        return contents;
    }

    public byte[] getOptionalInfo() {
        return optionalInfo;
    }

    public int getTotalPacketNumber() {
        return totalPacketNumber;
    }

    public String getResponseStatus() {
        return responseStatus;
    }
}
