package slowClient.worker;

import slowClient.Writer;
import packet.RequestPacket;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ShowFileListWorker implements Worker {
    private Scanner scanner;
    private Writer writer;

    public ShowFileListWorker(Scanner scanner, Writer writer) {
        this.scanner = scanner;
        this.writer = writer;
    }

    @Override
    public void doWork() {
        RequestPacket requestPacket = new RequestPacket(
                "ShowFileList",
                "Temp".getBytes(StandardCharsets.UTF_8),
                "".getBytes(StandardCharsets.UTF_8)
        );
        this.writer.writeToChannel(requestPacket.requestPacketList);
    }
}
