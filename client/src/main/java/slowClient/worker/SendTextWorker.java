package slowClient.worker;

import slowClient.Writer;
import packet.RequestPacket;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SendTextWorker implements Worker{
    private Scanner scanner;
    private Writer writer;
    private String toSendStr;

    public SendTextWorker(Scanner scanner, Writer writer, String toSendStr) {
        this.scanner = scanner;
        this.writer = writer;
        this.toSendStr = toSendStr;
    }

    @Override
    public void doWork() {
        RequestPacket requestPacket = new RequestPacket(
                "SendText",
                this.toSendStr.getBytes(StandardCharsets.UTF_8),
                "1".getBytes(StandardCharsets.UTF_8)
        );
        this.writer.writeToChannel(requestPacket.requestPacketList);
    }
}
