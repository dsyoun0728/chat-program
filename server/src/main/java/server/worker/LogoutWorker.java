package server.worker;

import server.Client;

public class LogoutWorker implements Worker {
    private Client client;

    public LogoutWorker(Client client) {
        this.client = client;
    }

    @Override
    public void doWork() {
        System.out.println("LogoutWorker");
        System.out.println(client);
    }
}
