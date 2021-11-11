package server.Worker;

import server.Client;

public class SendFilesWorker implements Worker {
    private Client client;

    public SendFilesWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        System.out.println("SendFilesWorker");
        System.out.println(client);
    }
}
