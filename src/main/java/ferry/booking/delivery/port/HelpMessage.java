package ferry.booking.delivery.port;

public class HelpMessage {
    private String[] payload;

    public HelpMessage(String[] payload) {
        this.payload = payload;
    }

    public void print(UserCommunication userCommunication) {
        userCommunication.display(payload);
    }
}
