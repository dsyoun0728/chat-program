import org.junit.Assert;
import org.junit.Test;
import packet.*;
import parser.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PacketAndParserIntegrationTest {
    Parser requestParser = new RequestParser();
    Parser responseParser = new ResponseParser();

    @Test
    public void requestPacketContentsTest() {
        String longStr = "1가나다라마바사아자차카타파하2가나다라마바사아자차카타파하3가나다라마바사아자차카타파하4가나다라마바사아자차카타파하";
        byte[] byteArray = longStr.getBytes(StandardCharsets.UTF_8);
        RequestPacket requestPacket = new RequestPacket("SendText", byteArray, "".getBytes(StandardCharsets.UTF_8));
        byte[] testByteArray = new byte[byteArray.length];
        try {
            testByteArray = requestParser.getContents(requestPacket.requestPacketList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(longStr.equals(new String(testByteArray)));
    }

    @Test
    public void requestPacketOptionalInfoTest() {
        String longStr = "1가나다라마바사아자차카타파하2가나다라마바사아자차카타파하3가나다라마바사아자차카타파하4가나다라마바사아자차카타파하";
        String shortStr = "1가나다라마바사아자차카타파하2가나다라마바사아자차카타파하";
        byte[] contentsByteArray = longStr.getBytes(StandardCharsets.UTF_8);
        byte[] optionalInfoByteArray = shortStr.getBytes(StandardCharsets.UTF_8);
        RequestPacket requestPacket = new RequestPacket("SendText", contentsByteArray, optionalInfoByteArray);
        byte[] testByteArray = new byte[0];
        testByteArray = requestParser.getOptionalInfo(requestPacket.requestPacketList);
        Assert.assertTrue(shortStr.equals(new String(Parser.sliceByteArray(testByteArray, 4, optionalInfoByteArray.length))));
    }

    @Test
    public void requestPacketTotalPakcetNumTest() {
        String longStr = "1가나다라마바사아자차카타파하2가나다라마바사아자차카타파하3가나다라마바사아자차카타파하4가나다라마바사아자차카타파하";
        String shortStr = "1가나다라마바사아자차카타파하2가나다라마바사아자차카타파하";
        byte[] contentsByteArray = longStr.getBytes(StandardCharsets.UTF_8);
        byte[] optionalInfoByteArray = shortStr.getBytes(StandardCharsets.UTF_8);
        RequestPacket requestPacket = new RequestPacket("SendText", contentsByteArray, optionalInfoByteArray);
        byte[] testByteArray = new byte[0];
        testByteArray = requestParser.getOptionalInfo(requestPacket.requestPacketList);
        byte[] totalPacketNumByteArray = Parser.sliceByteArray(testByteArray, 0, 4);
        Assert.assertTrue((int) Math.ceil((double) contentsByteArray.length / 80) == byteArrayToInt(totalPacketNumByteArray));
    }

    @Test
    public void responsePacketContentsTest() {
        String contentsStr = "가나다라마바사아자차카타파하";
        byte[] byteArray = contentsStr.getBytes(StandardCharsets.UTF_8);
        ResponsePacket responsePacket = new ResponsePacket((byte)20, (byte)4, byteArray, "".getBytes(StandardCharsets.UTF_8));
        byte[] testByteArray = new byte[byteArray.length];
        try {
            testByteArray = responseParser.getContents(responsePacket.responsePacketList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(new String(testByteArray));
        Assert.assertTrue(contentsStr.equals(new String(testByteArray)));
    }

    @Test
    public void responsePacketOptionalInfoTest() {
        String contentsStr = "가나다라마바사아자차카타파하";
        String optionalInfoStr = "abcdefg";
        byte[] contentsByteArray = contentsStr.getBytes(StandardCharsets.UTF_8);
        byte[] optionalInfoByteArray = optionalInfoStr.getBytes(StandardCharsets.UTF_8);
        ResponsePacket responsePacket = new ResponsePacket((byte)20, (byte)4, contentsByteArray, optionalInfoByteArray);
        byte[] testByteArray = new byte[0];
        testByteArray = requestParser.getOptionalInfo(responsePacket.responsePacketList);
        Assert.assertTrue(optionalInfoStr.equals(new String(Parser.sliceByteArray(testByteArray, 5, optionalInfoByteArray.length))));
    }

    @Test
    public void responsePacketTotalPakcetNumTest() {
        String contentsStr = "가나다라마바사아자차카타파하";
        String optionalInfoStr = "abcdefg";
        byte[] contentsByteArray = contentsStr.getBytes(StandardCharsets.UTF_8);
        byte[] optionalInfoByteArray = optionalInfoStr.getBytes(StandardCharsets.UTF_8);
        ResponsePacket responsePacket = new ResponsePacket((byte)20, (byte)4, contentsByteArray, optionalInfoByteArray);
        byte[] testByteArray = new byte[0];
        testByteArray = requestParser.getOptionalInfo(responsePacket.responsePacketList);
        byte[] totalPacketNumByteArray = Parser.sliceByteArray(testByteArray, 0, 4);
        Assert.assertTrue((int) Math.ceil((double) contentsByteArray.length / 80) == byteArrayToInt(totalPacketNumByteArray));
    }

    @Test
    public void responsePacketResponseCodeTest() {
        String contentsStr = "가나다라마바사아자차카타파하";
        String optionalInfoStr = "abcdefg";
        byte responseCode = 20;
        byte[] contentsByteArray = contentsStr.getBytes(StandardCharsets.UTF_8);
        byte[] optionalInfoByteArray = optionalInfoStr.getBytes(StandardCharsets.UTF_8);
        ResponsePacket responsePacket = new ResponsePacket(responseCode, (byte)4, contentsByteArray, optionalInfoByteArray);
        byte[] testByteArray = new byte[0];
        testByteArray = requestParser.getOptionalInfo(responsePacket.responsePacketList);
        Assert.assertTrue(testByteArray[4] == responseCode);
    }

    public int byteArrayToInt(byte [] b) {
        return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
    }
}
