package parser;
import util.UniqueID;

public class Parser {
    String uniqueID;
    int lastFlag;
    public int contentsLength;
    public String contents;
    public String optionalInfo;

    public Parser(byte[] uniqueIDByteArray, byte lastAndLength, byte[] contents, byte[] optionalInfo) {
        this.uniqueID = UniqueID.getFullUniqueID(uniqueIDByteArray);
        this.lastFlag = lastAndLength >> 7 == -1 ? 1 : 0;
        this.contentsLength = lastAndLength & 127;
        this.contents = new String(contents);
        this.optionalInfo = new String(optionalInfo);
    }

    public static byte[] sliceByteArray(byte[] byteArr, int offset, int length) {
        byte[] returnByteArray = new byte[length];
        for (int i=0; i< length; i++) {
            returnByteArray[i] = byteArr[offset + i];
        }
        return  returnByteArray;
    }
}
