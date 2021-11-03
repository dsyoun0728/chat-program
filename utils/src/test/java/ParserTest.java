import org.junit.Assert;
import org.junit.Test;
import parser.*;

public class ParserTest {
    Parser requestParser = new RequestParser();
    Parser responseParser = new ResponseParser();

    @Test
    public void requestParserIsLastTrueTest(){
        byte[] testByteArray = new byte[120];
        testByteArray[1] = (byte) 128;
        Assert.assertTrue(requestParser.isLast(testByteArray));
    }

    @Test
    public void requestParserIsLastFalseTest() {
        byte[] testByteArray = new byte[120];
        Assert.assertFalse(requestParser.isLast(testByteArray));
    }

    @Test
    public void responseParserIsLastTrueTest(){
        byte[] testByteArray = new byte[120];
        testByteArray[1] = (byte) 128;
        Assert.assertTrue(responseParser.isLast(testByteArray));
    }

    @Test
    public void responseParserIsLastFalseTest() {
        byte[] testByteArray = new byte[120];
        Assert.assertFalse(responseParser.isLast(testByteArray));
    }
}
