package packet;

import java.util.ArrayList;

public class ResponsePacket extends ProtocolPacket {
    byte responseCode;
    public ArrayList<byte[]> responsePacketList = new ArrayList<byte[]>();

    public ResponsePacket(byte responseCode, byte functionNum, byte[] contents, byte[] optionalInfo) {
        super(functionNum, contents, optionalInfo);
        this.responseCode = responseCode;

        makePacketList();
    }

    @Override
    public byte[] makePacketByteArray(int currentPacketNum) {
        int destPos = 0;
        boolean lastFlag = this.totalPacketNum -1 == currentPacketNum;
        byte thisContentsLength = (byte) (lastFlag ? this.contentsLength - 80 * currentPacketNum : 80);
        byte lastAndLength = (byte) (lastFlag ? 1 << 7 + thisContentsLength : thisContentsLength);
        byte[] responsePacketByteArray = new byte[120];

        // 0                   response code
        responsePacketByteArray[destPos] = responseCode;
        destPos += 1;

        // 1                    last flag & contents length
        responsePacketByteArray[destPos] = lastAndLength;
        destPos += 1;

        // 2 ~ 81           contents
        System.arraycopy(contents, 80 * currentPacketNum, responsePacketByteArray, destPos, thisContentsLength);
        destPos += 80;

        // 82 ~ 85       optional information (Total Packet Number)
        System.arraycopy(totalPacketNumByteArray, 0, responsePacketByteArray, destPos, totalPacketNumByteArray.length);
        destPos += 4;

        // 86               optional information (functionNum)
        responsePacketByteArray[destPos] = functionNum;
        destPos += 1;

        // 87 ~ 119     optional information
        System.arraycopy(optionalInfo, 0, responsePacketByteArray, destPos, optionalInfo.length);
        destPos += 33;

        return responsePacketByteArray;
    }

    @Override
    public void makePacketList() {
        for(int i = 0; i < totalPacketNum; i ++) {
            responsePacketList.add(makePacketByteArray(i));
        }
    }
}
