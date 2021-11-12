package server;

import server.worker.*;

public class Reader {
    private Client client;

    public Reader(Client client) {
        this.client = client;
    }

    public void deployWorker(String functionName) {
        Worker worker = null;

        switch(functionName) {
            case "Login":
                worker = new LoginWorker(this.client);
                break;
            case "Logout":
                worker = new LogoutWorker(this.client);
                break;
            case "SendWhisper":
                worker = new SendWhisperWorker(this.client);
                break;
            case "SendText":
                worker = new SendTextWorker(this.client);
                break;
            case "SendFile":
                worker = new SendFileWorker(this.client);
                break;
            case "SendFiles":
                worker = new SendFilesWorker(this.client);
                break;
            case "ShowFileList":
                worker = new ShowFileListWorker(this.client);
                break;
            case "DownloadFile":
                worker = new DownloadFileWorker(this.client);
                break;
            case "DownloadFiles":
                worker = new DownloadFilesWorker(this.client);
                break;
            case "DeleteFile":
                worker = new DeleteFileWorker(this.client);
                break;
            case "DeleteFiles":
                worker = new DeleteFilesWorker(this.client);
                break;
            default:
                System.out.println("wrong functionName");
                break;
        }

        worker.doWork();
    }
}
