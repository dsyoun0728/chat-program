package packet;

import util.*;

import java.util.ArrayList;
import java.util.UUID;

public class ResponsePacket extends ProtocolPacket {
    public ArrayList<byte[]> responsePacketList = new ArrayList<byte[]>();

    public ResponsePacket(UUID uuid, byte responseCode, String functionName, byte[] contents, byte[] optionalInfo) {
        super(uuid, Function.getFunctionNum(functionName), contents);
        System.arraycopy(totalPacketNumByteArray, 0, this.optionalInfo, 0, totalPacketNumByteArray.length);
        this.optionalInfo[totalPacketNumByteArray.length] = responseCode;
        System.arraycopy(optionalInfo, 0, this.optionalInfo, totalPacketNumByteArray.length + 1, optionalInfo.length);

        makePacketList();
    }

    @Override
    public byte[] makePacketByteArray(int currentPacketNum) {
        int destPos = 0;
        boolean isLast = this.totalPacketNum -1 == currentPacketNum;
        int thisContentsLength = isLast ?
                this.totalContentsLength - Constants.PACKET_CONTENTS_SIZE * currentPacketNum
                : Constants.PACKET_CONTENTS_SIZE;

        byte[] responsePacketByteArray= new byte[Constants.PACKET_TOTAL_SIZE];

        // 0                    Last Flag
        responsePacketByteArray[destPos] = (byte) (isLast ? 1 : 0);
        destPos += Constants.PACKET_LAST_FLAG_SIZE;

        // 1 ~ 16           UUID
        System.arraycopy(this.UUIDByteArr, 0, responsePacketByteArray, destPos, Constants.PACKET_UUID_SIZE);
        destPos += Constants.PACKET_UUID_SIZE;

        // 17 ~ 20        Function 정보
        System.arraycopy(this.function, 0, responsePacketByteArray, destPos, Constants.PACKET_FUNCTION_SIZE);
        destPos += Constants.PACKET_FUNCTION_SIZE;

        // 21 ~ 24        Contents Length
        System.arraycopy(this.intToByteArray(thisContentsLength), 0, responsePacketByteArray, destPos, Constants.PACKET_CONTENTS_LENGTH_SIZE);
        destPos += Constants.PACKET_CONTENTS_LENGTH_SIZE;

        // 25 ~ 424     Contents
        System.arraycopy(this.contents, Constants.PACKET_CONTENTS_SIZE * currentPacketNum, responsePacketByteArray, destPos, thisContentsLength);
        destPos += Constants.PACKET_CONTENTS_SIZE;

        // 425 ~ 499   Optional information (Total Packet Number + etc)
        System.arraycopy(this.optionalInfo, Constants.PACKET_OPTIONAL_INFO_SIZE * currentPacketNum, responsePacketByteArray, destPos, Constants.PACKET_OPTIONAL_INFO_SIZE);
        destPos += Constants.PACKET_OPTIONAL_INFO_SIZE;

        return responsePacketByteArray;
    }

    @Override
    public void makePacketList() {
        for(int i = 0; i < totalPacketNum; i ++) {
            responsePacketList.add(makePacketByteArray(i));
        }
    }
}
