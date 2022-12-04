package my;

import java.util.Random;

public class Command {
    public CommandStates state;
    public int to;
    public int from;
    public char name;

    public Command(CommandStates state, int to, int from) {
        this.state = state;
        this.to = to;
        this.from = from;
        this.name = (char) ('A' + new Random().nextInt(50));
    }

    public enum CommandStates {
        REGISTERING(0),
        WAITING(1),
        IN_ELEVATOR(2),
        COMPLETED(3);

        public Integer value;

        CommandStates(Integer position) {
            this.value = position;
        }
    }

}
