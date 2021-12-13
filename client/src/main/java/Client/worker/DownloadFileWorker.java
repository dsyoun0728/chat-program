package Client.worker;

import Client.Writer;
import packet.RequestPacket;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class DownloadFileWorker implements Worker {
    private Scanner scanner;
    private Client.Writer writer;

    public DownloadFileWorker(Scanner scanner, Writer writer) {
        this.scanner = scanner;
        this.writer = writer;
    }

    @Override
    public void doWork() {
        System.out.print("다운로드 할 파일 이름을 입력하세요 > ");
        String fileName = this.scanner.nextLine();

        RequestPacket requestPacket = new RequestPacket(
                "DownloadFile",
                fileName.getBytes(StandardCharsets.UTF_8),
                "".getBytes(StandardCharsets.UTF_8)
        );
        this.writer.writeToChannel(requestPacket.requestPacketList);
    }
}
