package parser;
import util.UniqueID;

public class Parser {
    String uniqueID;
    public int lastFlag;
    public int contentsLength;
    public byte[] contents;
    public byte[] optionalInfo;

    public Parser(byte[] uniqueIDByteArray, byte lastAndLength, byte[] contents, byte[] optionalInfo) {
        this.uniqueID = UniqueID.getFullUniqueID(uniqueIDByteArray);
        this.lastFlag = lastAndLength >> 7 == -1 ? 1 : 0;
        this.contentsLength = lastAndLength & 127;
        this.contents = contents;
        this.optionalInfo = optionalInfo;
    }

    public static byte[] sliceByteArray(byte[] byteArr, int offset, int length) {
        byte[] returnByteArray = new byte[length];
        for (int i=0; i< length; i++) {
            returnByteArray[i] = byteArr[offset + i];
        }
        return  returnByteArray;
    }
}
