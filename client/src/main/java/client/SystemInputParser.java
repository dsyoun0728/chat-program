package client;

import client.worker.*;

import java.util.Scanner;

public class SystemInputParser {
    private Client client;
    private Scanner scanner;
    private Writer writer;

    public SystemInputParser(Client client, Scanner scanner) {
        this.client = client;
        this.scanner = scanner;
        this.writer = new Writer(this.client);
    }

    public void startClient() {
        System.out.println("안녕하세요. 로그인 후 다른 기능들을 이용 가능합니다.");
        System.out.print("접속 서버 IP 주소와 Port, userNick를 입력하세요(ex. Login 192.168.14.51:5001 홍길동) > ");

        String firstInput = this.scanner.nextLine();
        while (!firstInput.split(" ")[0].toLowerCase().equals("login")) {
            System.out.print("다시 입력하세요(ex. Login 192.168.14.51:5001 홍길동) > ");
            firstInput = this.scanner.nextLine();
        }
        this.client.startClient(this.writer, firstInput);

        String functionStr;
        Worker worker = null;

        while (true) {
            functionStr = this.scanner.nextLine();

            switch (functionStr.toLowerCase()) {
                case "sendfile":
                    worker = new SendFileWorker(this.scanner, this.writer);
                    break;
                case "showfilelist":
                    worker = new ShowFileListWorker(this.scanner, this.writer);
                    break;
                case "downloadfile":
                    worker = new DownloadFileWorker(this.scanner, this.writer);
                    break;
                case "logout":
                    worker = new LogoutWorker(this.writer);
                    break;
                default:
                    worker = new SendTextWorker(this.scanner, this.writer, functionStr);
            }

            worker.doWork();
        }
    }
}
