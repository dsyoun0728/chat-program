import org.junit.Assert;
import org.junit.Test;
import packet.*;
import parser.*;
import util.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PacketAndParserIntegrationTest {
    String longStr = "1가나다라마바사아자차카타파하2가나다라마바사아자차카타파하3가나다라마바사아자차카타파하4가나다라마바사아자차카타파하";
    String shortStr = "가나다라마바사아자차카타파하";

    byte[] longStrByteArray = longStr.getBytes(StandardCharsets.UTF_8);
    byte[] shortStrByteArray = shortStr.getBytes(StandardCharsets.UTF_8);

    RequestPacket requestPacket = new RequestPacket("SendText", longStrByteArray, shortStrByteArray);

    RequestParser requestParser = new RequestParser();

    @Test
    public void requestPacketContentsTest() {
        byte[] testByteArray = Parser.getContents(requestPacket.requestPacketList);
        Assert.assertTrue(longStr.equals(new String(testByteArray)));
    }

    @Test
    public void requestPacketUUIDTest() {
        ParsedMsg parsedMsg = requestParser.parseMessage(requestPacket.requestPacketList);
        System.out.println(parsedMsg.getUuid().toString());
    }
}
