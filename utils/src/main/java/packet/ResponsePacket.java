package packet;

import util.Function;

import java.util.ArrayList;

public class ResponsePacket extends ProtocolPacket {
    public ArrayList<byte[]> responsePacketList = new ArrayList<byte[]>();

    public ResponsePacket(byte responseCode, byte functionNum, byte[] contents, byte[] optionalInfo) {
        super(functionNum, contents);
        System.arraycopy(totalPacketNumByteArray, 0, this.optionalInfo, 0, totalPacketNumByteArray.length);
        this.optionalInfo[totalPacketNumByteArray.length] = responseCode;
        System.arraycopy(optionalInfo, 0, this.optionalInfo, totalPacketNumByteArray.length + 1, optionalInfo.length);

        makePacketList();
    }

    @Override
    public byte[] makePacketByteArray(int currentPacketNum) {
        int destPos = 0;
        boolean lastFlag = this.totalPacketNum -1 == currentPacketNum;
        byte thisContentsLength = (byte) (lastFlag ? this.contentsLength - 80 * currentPacketNum : 80);
        byte lastAndLength = (byte) (lastFlag ? (byte)(1 << 7) + thisContentsLength : thisContentsLength);
        byte[] responsePacketByteArray = new byte[120];

        // 0                    functionNum
        responsePacketByteArray[destPos] = functionNum;
        destPos += 1;

        // 1                    last flag & contents length
        responsePacketByteArray[destPos] = lastAndLength;
        destPos += 1;

        // 2 ~ 81           contents
        System.arraycopy(contents, 80 * currentPacketNum, responsePacketByteArray, destPos, thisContentsLength);
        destPos += 80;

        // 82 ~ 119       optional information (Total Packet Number + Response Code + etc)
        System.arraycopy(this.optionalInfo, 38 * currentPacketNum, responsePacketByteArray, destPos, 38);
        destPos += 38;

        return responsePacketByteArray;
    }

    @Override
    public void makePacketList() {
        for(int i = 0; i < totalPacketNum; i ++) {
            responsePacketList.add(makePacketByteArray(i));
        }
    }
}