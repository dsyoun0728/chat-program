package server;

//import server.worker.Worker;
//import util.Constants;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.UUID;


public class Writer {
    private Client client;

    public Writer(Client client) {
        this.client = client;
    }

//    public Runnable writeToChannel() {
//        Runnable runnable = () -> {
//            for (UUID uuid : this.client.getResponsePacketListMap().keySet()) {
//                try {
//                    for (byte[] packet : this.client.getResponsePacketList(uuid)) {
//                        int byteCount = 0;
//                        ByteBuffer byteBuffer = ByteBuffer.wrap(packet);
//                        while (byteCount < Constants.PACKET_TOTAL_SIZE) {
//                            byteCount += this.client.getSocketChannel().write(byteBuffer);
//                        }
//                    }
//                } catch (IOException e) {
//                    System.out.println("Writer IOException\t\t\t");
//                    e.printStackTrace();
//                    Worker.handleClientOut(this.client, uuid);
//                } catch (Exception e) {
//                    System.out.println("Writer Exception\n\n\n");
//                    e.printStackTrace();
//                }
//                this.client.clearRequestPacketList(uuid);
//                this.client.clearResponsePacketList(uuid);
//            }
//        };
//        return runnable;
//    }
}