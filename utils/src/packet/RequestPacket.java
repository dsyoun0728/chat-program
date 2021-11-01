package packet;

import util.Function;

import java.util.ArrayList;

public class RequestPacket implements ProtocolPacket {
    byte functionNum;
    int contentsLength;
    byte[] contents;
    int totalPacketNum;
    byte[] totalPacketNumByteArray;
    byte[] optionalInfo = new byte[34];
    public ArrayList<byte[]> requestPacketList = new ArrayList<byte[]>();

    public RequestPacket(String functionName, byte[] contents, byte[] optionalInfo) {
        this.functionNum = Function.getFunctionByte(functionName);
        this.contentsLength = contents.length;
        this.contents = contents;
        this.totalPacketNum = (int) Math.ceil((double) this.contentsLength / 80);
        this.totalPacketNumByteArray = intToByteArray(totalPacketNum);
        this.optionalInfo = optionalInfo;

        makePacketList();
    }

    @Override
    public byte[] makePacketByteArray(int currentPacketNum) {
        int destPos = 0;
        boolean lastFlag = this.totalPacketNum -1 == currentPacketNum;
        byte thisContentsLength = (byte) (lastFlag ? this.contentsLength - 80 * currentPacketNum : 80);
        byte lastAndLength = (byte) (lastFlag ? 1 << 7 + thisContentsLength : thisContentsLength);
        byte[] requestPacketByteArray = new byte[120];

        // 0                    function 정보
        requestPacketByteArray[destPos] = functionNum;
        destPos += 1;

        // 1                    last flag & contents length
        requestPacketByteArray[destPos] = lastAndLength;
        destPos += 1;

        // 2 ~ 81           contents
        System.arraycopy(contents, 80 * currentPacketNum, requestPacketByteArray, destPos, thisContentsLength);
        destPos += 80;

        // 82 ~ 85       optional information (Total Packet Number)
        System.arraycopy(totalPacketNumByteArray, 0, requestPacketByteArray, destPos, totalPacketNumByteArray.length);
        destPos += 4;

        // 86 ~ 119     optional information
        System.arraycopy(optionalInfo, 0, requestPacketByteArray, destPos, optionalInfo.length);
        destPos += 34;
        return requestPacketByteArray;
    }

    @Override
    public void makePacketList() {
        for(int i = 0; i < totalPacketNum; i ++) {
            requestPacketList.add(makePacketByteArray(i));
        }
    }

    public byte[] intToByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) (value >> 24);
        byteArray[1] = (byte) (value >> 16);
        byteArray[2] = (byte) (value >> 8);
        byteArray[3] = (byte) (value);
        return  byteArray;
    }
}
