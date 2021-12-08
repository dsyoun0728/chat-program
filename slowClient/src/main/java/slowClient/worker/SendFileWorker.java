package slowClient.worker;

import slowClient.Writer;
import packet.RequestPacket;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class SendFileWorker implements Worker{
    private Scanner scanner;
    private Writer writer;

    public SendFileWorker(Scanner scanner, Writer writer) {
        this.scanner = scanner;
        this.writer = writer;
    }

    @Override
    public void doWork() {
        System.out.print("업로드 할 파일 경로를 입력하세요 > ");
        String filePath;
        filePath = this.scanner.nextLine();

        File file = new File(filePath);
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(file.toPath());
            String[] filePathArray = filePath.split("/");
            String fileName = filePathArray[filePathArray.length - 1];

            RequestPacket requestPacket = new RequestPacket(
                    "SendFile",
                    fileContent,
                    fileName.getBytes(StandardCharsets.UTF_8)
            );
            this.writer.writeToChannel(requestPacket.requestPacketList);
        } catch (IOException e) {
            System.out.println("존재하지 않는 파일입니다. 파일 경로를 다시 입력해주세요.");
        }
    }
}
