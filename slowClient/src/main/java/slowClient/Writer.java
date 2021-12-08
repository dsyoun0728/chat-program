package slowClient;

import parser.Parser;
import util.Constants;

import java.io.IOException;
import java.util.ArrayList;

public class Writer {
    private slowClient slowClient;

    public Writer(slowClient slowClient) {
        this.slowClient = slowClient;
    }

    public void writeToChannel(ArrayList<byte[]> byteArrayList) {
        Runnable writeRunnable = () -> {
            try {
                String fn = Parser.getFunctionName(byteArrayList.get(0));
                if (fn.equals("SendFile")) {
                    System.out.println("파일을 서버에 전송 중입니다....");
                }
                for (byte[] byteArray : byteArrayList) {

                    //100byte씩 쪼개보내기
                    byte[][] packet = new byte[5][100];
                    for(int i=0;i<5;i++) {
                        int sendCount = 0;
                        System.arraycopy(byteArray, 100*i, packet[i], 0, 100);

                        slowClient.getWriteByteBuffer().clear();
                        slowClient.getWriteByteBuffer().put(packet[i]);
                        slowClient.getWriteByteBuffer().flip();
                        while (sendCount < 100) {
                            sendCount += slowClient.getSocketChannel().write(slowClient.getWriteByteBuffer());
                        }
                        slowClient.getWriteByteBuffer().clear();
                    }

                }
                 if (fn.equals("SendFile")) {
                  System.out.println("서버가 처리 중입니다....");
                 }
            } catch (IOException e) {
                System.out.println("client send IOException\n\n\n" + e + "\n\n\n");
                this.slowClient.stopClient();
            } catch (Exception e) {
                System.out.println("client send Exception\n\n\n" + e + "\n\n\n");
                this.slowClient.stopClient();
            }
        };
        this.slowClient.getExecutorService().submit(writeRunnable);
    }
}
