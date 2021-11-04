import org.junit.Assert;
import org.junit.Test;
import packet.*;
import parser.*;

import java.io.IOException;
import java.nio.ByteBuffer;
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
        System.out.println(new String(testByteArray));
        Assert.assertTrue(longStr.equals(new String(testByteArray)));
    }

    @Test
    public void responsePacketContentsTest() {
        String longStr = "1가나다라마바사아자차카타파하2가나다라마바사아자차카타파하3가나다라마바사아자차카타파하4가나다라마바사아자차카타파하";
        byte[] byteArray = longStr.getBytes(StandardCharsets.UTF_8);
        ResponsePacket responsePacket = new ResponsePacket((byte)20, (byte)4, byteArray, "".getBytes(StandardCharsets.UTF_8));
        byte[] testByteArray = new byte[byteArray.length];
        try {
            testByteArray = responseParser.getContents(responsePacket.responsePacketList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(new String(testByteArray));
        Assert.assertTrue(longStr.equals(new String(testByteArray)));
    }
}
