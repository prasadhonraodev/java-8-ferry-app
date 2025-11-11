package ferry.booking.delivery.port;

public class UserMessage {
    private String[] descriptions;

    public UserMessage(String description) {
        this.descriptions = new String[]{description};
    }

    public UserMessage(String[] descriptions) {
        this.descriptions = descriptions;
    }

    public void print(UserCommunication userCommunication) {
        userCommunication.display(this.descriptions);
    }
}
