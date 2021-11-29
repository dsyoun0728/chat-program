package client;

import parser.Parser;
import util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Writer {
    private Client client;

    public Writer(Client client) {
        this.client = client;
    }

    public void writeToChannel(ArrayList<byte[]> byteArrayList) {
        Runnable writeRunnable = () -> {
            try {
                String fn = Parser.getFunctionName(byteArrayList.get(0));
                if (fn.equals("SendFile")) {
                    System.out.println("파일을 서버에 전송 중입니다....");
                }
                for (byte[] byteArray : byteArrayList) {
                    int sendCount = 0;

                    client.getByteBuffer().clear();
                    client.getByteBuffer().put(byteArray);
                    client.getByteBuffer().flip();
                    while (sendCount < Constants.PACKET_TOTAL_SIZE) {
                        sendCount += client.getSocketChannel().write(client.getByteBuffer());
                    }
                    client.getByteBuffer().clear();
                }
                if (fn.equals("SendFile")) {
                    System.out.println("서버가 처리 중입니다....");
                }
            } catch (IOException e) {
                System.out.println("client send IOException\n\n\n" + e + "\n\n\n");
                this.client.stopClient();
            } catch (Exception e) {
                System.out.println("client send Exception\n\n\n" + e + "\n\n\n");
                this.client.stopClient();
            }
        };
        this.client.getExecutorService().submit(writeRunnable);
    }
}
