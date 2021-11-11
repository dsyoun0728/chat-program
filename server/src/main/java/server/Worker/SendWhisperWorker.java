package server.Worker;

import server.Client;

public class SendWhisperWorker implements Worker {
    private Client client;

    public SendWhisperWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        System.out.println("SendWhisperWorker");
        System.out.println(client);
    }
}
