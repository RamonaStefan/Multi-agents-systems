package my;

import base.Agent;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FloorAgent implements Agent {
    public int id;
    public ArrayList<Command> people = new ArrayList<>();
    public ArrayList<ArrayList<Integer>> registeredCommands = new ArrayList<>();

    public FloorAgent(int id){
        this.id = id;
    }

    public void registerCommands(Command cmd){
        ArrayList<Integer> crtCommand = new ArrayList<>();
        crtCommand.add(cmd.state.value);
        crtCommand.add(cmd.from);
        crtCommand.add(cmd.to);
        people.add(cmd);
        registeredCommands.add(crtCommand);
    }

    public Set<AgentMessage> response(Set<AgentMessage> perceptions){
        Set<AgentMessage> messages = new HashSet<>();

        if(registeredCommands.size() > 0) {
//            System.out.println("Currently registered commands at floor " + this.id);
            Set<Command> toDelete = new HashSet<>();
            for (Command command : people) {
                if (command.state.equals(Command.CommandStates.COMPLETED)) {
                    toDelete.add(command);
                }
//                System.out.println(command);
//                people.add(new Command(Command.CommandStates.values()[(command.get(0))], command.get(2), command.get(1)));
            }

            people.removeAll(toDelete);
        }
        return messages;
    }

    public int getId() {
        return id;
    }

    public String getCommands() {
        StringBuilder ppl = new StringBuilder();
        ArrayList<Command> copyPeople = new ArrayList<>(people);
        for (Command i : copyPeople) {
            if (i.state.equals(Command.CommandStates.WAITING)) {
                ppl.append(i.name);
            }
        }
        ppl.append("    | ");
        for (Command i : copyPeople) {
            if (i.state.equals(Command.CommandStates.REGISTERING)) {
                ppl.append(i.name);
                ppl.append(i.from > i.to ? "(v) ":"(^) ");
            }
        }
        return ppl.toString();
    }

    public ArrayList<Command> getPeople() {
        return people;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
