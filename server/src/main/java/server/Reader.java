package server;

import parser.Parser;
import server.worker.*;

import java.util.UUID;

public class Reader {
    private Client client;

    public Reader(Client client) {
        this.client = client;
    }

    public void deployWorker(UUID uuid) {
        Worker worker = null;
        String functionName = Parser.getFunctionName(this.client.getRequestPacketList(uuid).get(0));

        switch(functionName) {
            case "Login":
                worker = new LoginWorker(this.client, uuid);
                break;
            case "Logout":
                worker = new LogoutWorker(this.client, uuid);
                break;
            case "SendWhisper":
                worker = new SendWhisperWorker(this.client, uuid);
                break;
            case "SendText":
                worker = new SendTextWorker(this.client, uuid);
                break;
            case "SendFile":
                worker = new SendFileWorker(this.client, uuid);
                break;
            case "SendFiles":
                worker = new SendFilesWorker(this.client, uuid);
                break;
            case "ShowFileList":
                worker = new ShowFileListWorker(this.client, uuid);
                break;
            case "DownloadFile":
                worker = new DownloadFileWorker(this.client, uuid);
                break;
            case "DownloadFiles":
                worker = new DownloadFilesWorker(this.client, uuid);
                break;
            case "DeleteFile":
                worker = new DeleteFileWorker(this.client, uuid);
                break;
            case "DeleteFiles":
                worker = new DeleteFilesWorker(this.client, uuid);
                break;
            default:
                System.out.println("wrong functionName");
                break;
        }

        worker.doWork();
    }
}
