package client.worker;

import client.Writer;
import packet.RequestPacket;

import java.nio.charset.StandardCharsets;

public class LogoutWorker implements Worker{
    private Writer writer;

    public LogoutWorker(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void doWork() {
        RequestPacket requestPacket = new RequestPacket(
                "Logout",
                "Temp".getBytes(StandardCharsets.UTF_8),
                "".getBytes(StandardCharsets.UTF_8)
        );
        this.writer.writeToChannel(requestPacket.requestPacketList);
    }
}
