package packet;

import util.Function;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RequestPacket extends ProtocolPacket {

    public ArrayList<byte[]> requestPacketList = new ArrayList<byte[]>();
    int optionalLength;

    public RequestPacket(String functionName, byte[] contents, byte[] optionalInfo) {
        super(Function.getFunctionByte(functionName), contents);

        /*System.arraycopy(totalPacketNumByteArray, 0, this.optionalInfo, 0, totalPacketNumByteArray.length);
        System.arraycopy(optionalInfo, 0, this.optionalInfo, totalPacketNumByteArray.length, optionalInfo.length);*/
        this.optionalInfo = optionalInfo;
        this.optionalLength = optionalInfo.length;

        makePacketList();
    }

    @Override
    public byte[] makePacketByteArray(int currentPacketNum) {
        int destPos = 0;
        boolean lastFlag = this.totalPacketNum -1 == currentPacketNum;
        byte thisContentsLength = (byte) (lastFlag ? this.contentsLength - 80 * currentPacketNum : 80);

        byte thisOptionalLength = (byte) (lastFlag ? this.optionalLength - 38 * currentPacketNum : 38);

        byte lastAndLength = (byte) (lastFlag ? (byte)(1 << 7) + thisContentsLength : thisContentsLength);
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

        // 82 ~ 119       optional information (Total Packet Number + etc)
        //System.arraycopy(optionalInfo,38 * currentPacketNum, requestPacketByteArray, destPos, thisOptionalLength);
        //System.arraycopy(totalPacketNumByteArray, 38 * currentPacketNum, requestPacketByteArray, destPos, totalPacketNumByteArray.length);
        destPos += 38;

        return requestPacketByteArray;
    }

    @Override
    public void makePacketList() {
        for(int i = 0; i < totalPacketNum; i ++) {
            requestPacketList.add(makePacketByteArray(i));
        }
    }
}
