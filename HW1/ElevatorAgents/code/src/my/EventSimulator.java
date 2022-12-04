package my;

import base.Agent;
import base.Environment;
import base.Tester;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class EventSimulator implements Environment,Agent {
    public static long floors;
    public static long elevators;
    public static long capacity;
    public static long Tdoors;
    public static long Tclose;
    public static long Ttransit;
    public static long Tslow;
    public static long Taccel;
    public static long Tfast;

    public static ArrayList<ArrayList<Long>> normalScenarios = new ArrayList();
    public static ArrayList<ArrayList<Long>> morningScenarios = new ArrayList();
    public static ArrayList<ArrayList<Long>> intensiveScenarios = new ArrayList();

    public int COMMAND_IDX = 0;
    public int ORIGIN_IDX = 1;
    public int DESTINATION_IDX = 2;

    public static ArrayList<FloorAgent> floorAgents = new ArrayList<>();
    public static ArrayList<ElevatorAgent> elevatorAgents = new ArrayList<>();
    public boolean stop = false;
    public int maxIteration = 2000;
    public long steps = 0;

    Set<AgentMessage> messageBox = new HashSet<>();
    HashMap<Agent, Set<AgentMessage>> perceptions = new HashMap<>();
    Set<Command> people = new HashSet<>();


    private static void readScenario(String scenarioName, JSONObject jo, ArrayList<ArrayList<Long>> scenarioArray) {
        JSONArray normal = (JSONArray)((JSONObject) jo.get("scenarios")).get(scenarioName);

        Iterator itr2 = normal.iterator();

        while (itr2.hasNext())
        {
            Map scen = (Map)itr2.next();
            ArrayList<Long> crt = new ArrayList<>();
            crt.add((long)scen.get("Tfrom"));
            crt.add((long)scen.get("Tto"));
            crt.add((long)scen.get("Ofrom"));
            crt.add((long)scen.get("Oto"));
            crt.add((long)scen.get("Dfrom"));
            crt.add((long)scen.get("Dto"));
            crt.add((long)scen.get("Period"));
            scenarioArray.add(crt);
        }
    }

    public static void readInput() throws Exception{
        Object obj = new JSONParser().parse(new FileReader("code/tests/7floor.json"));

        // typecasting obj to JSONObject
        JSONObject jo = (JSONObject) obj;
        floors = (long)jo.get("F");
        elevators = (long)jo.get("E");
        capacity = (long)jo.get("C");
        Map dynamic = (Map)jo.get("dynamic");
        Tdoors = (long)dynamic.get("Tdoors");
        Tclose = (long)dynamic.get("Tclose");
        Ttransit = (long)dynamic.get("Ttransit");
        Tslow = (long)dynamic.get("Tslow");
        Taccel = (long)dynamic.get("Taccel");
        Tfast = (long)dynamic.get("Tfast");
        readScenario("normal", jo, normalScenarios);
        readScenario("morning", jo, morningScenarios);
        readScenario("intensive", jo, intensiveScenarios);
    }

    public EventSimulator() throws Exception {
        readInput();
        // Register Floor agents
        for (int i = 0 ; i < floors; i++){
            floorAgents.add(new FloorAgent(i+1));
        }

        // Register elevator agents
        for (int i = 0 ; i < elevators; i++){
            elevatorAgents.add(new ElevatorAgent(i+1, Tdoors, Tclose, Ttransit, Taccel, Tslow, Tfast, capacity));
        }

    }

    public boolean step() {
        environmentToString();
//        ArrayList<Long> crtScenario = normalScenarios.get(0);
        ArrayList<Long> crtScenario = intensiveScenarios.get(0);
        if (steps >= crtScenario.get(ScenarioParams.T_FROM.value).intValue() && steps <= crtScenario.get(ScenarioParams.T_TO.value)) {
            // Register commands to Floor agents
            if (steps % crtScenario.get(ScenarioParams.PERIOD.value) == 0) {
                int minStartingPoint = crtScenario.get(ScenarioParams.O_FROM.value).intValue();
                int maxStartingPoint = crtScenario.get(ScenarioParams.O_TO.value).intValue();
                int minEndingPoint = crtScenario.get(ScenarioParams.D_FROM.value).intValue();
                int maxEndingPoint = crtScenario.get(ScenarioParams.D_TO.value).intValue();

                int chosenStartingPoint = 0;
                int chosenEndingPoint = 0;

                while (chosenStartingPoint == chosenEndingPoint) {
                    chosenStartingPoint = minStartingPoint + (int) (Math.random() * (maxStartingPoint - minStartingPoint));
                    chosenEndingPoint = minEndingPoint + (int) (Math.random() * (maxEndingPoint - minEndingPoint));
                }
                Command cmd = new Command(Command.CommandStates.REGISTERING, chosenEndingPoint, chosenStartingPoint);
                people.add(cmd);
                floorAgents.get(chosenStartingPoint - 1).registerCommands(cmd);
                for (int i = 0 ; i < elevators; i++) {
                    messageBox.add(new AgentMessage(this, elevatorAgents.get(i), cmd, "ASK"));
                }
            }
        }

        Set<AgentMessage> messages = AgentMessage.filterMessagesFor(messageBox, this);
        Map<Command, Integer> mapCommands = new HashMap<>();
        Set<Command> messagesToIgnore = new HashSet<>();
        for (AgentMessage msg : messages) {
            Command command = (Command) msg.getContent();
            String[] affirmativePos = msg.info.split(" ");
            if (affirmativePos.length == 2) {
                if (affirmativePos[0].equals("AFIRMATIVE")) {
                    int pos = Integer.parseInt(affirmativePos[1]);
                    if (mapCommands.containsKey(command)) {
                        if (Math.abs(command.from - mapCommands.get(command)) > Math.abs(command.from - pos)) {
                            mapCommands.put(command, pos);
                        }
                    }
                    else {
                        mapCommands.put(command, pos);
                    }
                }
            }
        }

        for(Command command: mapCommands.keySet()){
            for (AgentMessage msg : messages) {
                Command command2 = (Command) msg.getContent();
                if (command == command2) {
                    String[] affirmativePos = msg.info.split(" ");
                    if (affirmativePos.length == 2) {
                        int pos = Integer.parseInt(affirmativePos[1]);
                        if (affirmativePos[0].equals("AFIRMATIVE") && pos == mapCommands.get(command) && !checkCommandAnswered(command, messagesToIgnore)) {
                            command.state = Command.CommandStates.WAITING;
                            messageBox.add(new AgentMessage(this, msg.getSender(), command, "DO"));
                            messagesToIgnore.add(command);
                        }
                    }
                }
            }
        }

        for (int j = 0; j < floorAgents.size(); j++) {
            perceptions.put(floorAgents.get(j), AgentMessage.filterMessagesFor(messageBox, floorAgents.get(j)));
        }
        for (int j = 0; j < elevatorAgents.size(); j++) {
            perceptions.put(elevatorAgents.get(j), AgentMessage.filterMessagesFor(messageBox, elevatorAgents.get(j)));
        }

        messageBox.clear();

        //  Step for floor agents
        for (int j = 0; j < floorAgents.size(); j++) {
            messageBox.addAll(floorAgents.get(j).response(perceptions.get(floorAgents.get(j))));
        }

        // Step for elevator agents
        for (int j = 0; j < elevatorAgents.size(); j++) {
            messageBox.addAll(elevatorAgents.get(j).response(perceptions.get(elevatorAgents.get(j))));
        }
        //  environmentToString();
        if (maxIteration == 0) {
            stop = true;
        }
        else {
            maxIteration--;
        }
        steps++;


        return true;
    }

    public List<ElevatorAgent> isAnyAgentAtFloor(int floor) {
        List<ElevatorAgent> agents = new ArrayList<>();
        for (ElevatorAgent elevAgent: elevatorAgents) {
            if (elevAgent.getPosition() == floor && !elevAgent.getState().equals(ElevatorAgent.ElevatorStates.IN_TRANSIT)) {
                agents.add(elevAgent);
            }
        }
        return agents;
    }

    public List<ElevatorAgent> isAnyAgentCommingAtFloor(int floor) {
        List<ElevatorAgent> agents = new ArrayList<>();
        for (ElevatorAgent elevAgent: elevatorAgents) {
            if (elevAgent.getPosition() == floor && elevAgent.getState().equals(ElevatorAgent.ElevatorStates.IN_TRANSIT)) {
                agents.add(elevAgent);
            }
        }
        return agents;
    }

    public boolean checkCommandAnswered(Command command, Set<Command> commands) {
        for (Command c : commands) {
            if (c.to == command.to && c.from == command.from) {
                return true;
            }
        }
        return false;
    }

    public void environmentToString() {
        StringBuilder env = new StringBuilder();
        env.append("Step " + steps + "\n");
        long length = capacity + (capacity % 2 == 0 ? 9 : 8);
        // elevators
        for(int i = 1; i <= elevators; i++) {
            appendCharacter(" ", length/2, env);
            env.append(i);
            appendCharacter(" ", length/2, env);
        }
        appendCharacter(" ", 50, env);
        env.append("\n");
        appendCharacter("=", elevators * length + 50, env);
        env.append("\n");
        // floors
        for(int i = (int) floors; i > 0; i--) {
            env.append(" |  ");
            List<ElevatorAgent> agents = isAnyAgentAtFloor(i);
            List<ElevatorAgent> commingAgents = isAnyAgentCommingAtFloor(i);
            for(int j = 0; j < elevators; j++) {
                if(agents.contains(elevatorAgents.get(j))) {
                    appendCharacter("-", capacity + 2, env);
                    appendCharacter(" ", 1, env);
                }
                else {
                    appendCharacter(" ", capacity + 3, env);
                }
                appendCharacter(" ", 4, env);
            }
            env.append("\n");
            env.append(i + "|  ");
            for(int j = 0; j < elevators; j++) {
                if(agents.contains(elevatorAgents.get(j))) {
                    env.append("|");
                    env.append(elevatorAgents.get(j).getCommands());

                    appendCharacter(" ", capacity-elevatorAgents.get(j).getCommands().length(), env);

                    String arrow = elevatorAgents.get(j).getDirection() == 1 ? "^" : "v";
                    if (elevatorAgents.get(j).getState().equals(ElevatorAgent.ElevatorStates.DOORS_CLOSING)) {
                        env.append("\\" + arrow);
                    }
                    else if (elevatorAgents.get(j).getState().equals(ElevatorAgent.ElevatorStates.DOORS_OPENING)){
                        env.append("/" + arrow);
                    }
                    else {
                        env.append("|" + arrow);
                    }
                }
                else if (commingAgents.contains(elevatorAgents.get(j))){
                    appendCharacter("-", capacity + 2, env);
                    appendCharacter(" ", 1, env);
                }
                else {
                    appendCharacter(" ", capacity + 3, env);
                }
                appendCharacter(" ", 4, env);
            }
            env.append(floorAgents.get(i-1).getCommands());
            env.append("\n");
            env.append(" |  ");
            for(int j = 0; j < elevators; j++) {
                if(agents.contains(elevatorAgents.get(j))) {
                    appendCharacter("-", capacity + 2, env);
                    appendCharacter(" ", 1, env);
                }
                else if (commingAgents.contains(elevatorAgents.get(j))){
                    env.append("|");
                    env.append(elevatorAgents.get(j).getCommands());
                    appendCharacter(" ", capacity-elevatorAgents.get(j).getCommands().length(), env);

                    String arrow = elevatorAgents.get(j).getDirection() == 1 ? "^" : "v";
                    if (elevatorAgents.get(j).getState().equals(ElevatorAgent.ElevatorStates.DOORS_CLOSING)) {
                        env.append("\\" + arrow);
                    }
                    else if (elevatorAgents.get(j).getState().equals(ElevatorAgent.ElevatorStates.DOORS_OPENING)){
                        env.append("/" + arrow);
                    }
                    else {
                        env.append("|" + arrow);
                    }
                }
                else {
                    appendCharacter(" ", capacity + 3, env);
                }
                appendCharacter(" ", 4, env);
            }
            appendCharacter("-", 50, env);
//            if (i != 1) {
                env.append("\n");
                env.append(" |  ");
                for (int j = 0; j < elevators; j++) {
                    if (commingAgents.contains(elevatorAgents.get(j))) {
                        appendCharacter("-", capacity + 2, env);
                        appendCharacter(" ", 1, env);
                    } else {
                        appendCharacter(" ", capacity + 3, env);
                    }
                    appendCharacter(" ", 4, env);
                }
//            }
            env.append("\n");
        }
        appendCharacter("=", elevators * length + 50, env);
        env.append("\n");
        int iteration=0;
        for (Command command: getAllCommands()) {
            env.append(command.name + " ");
            env.append(command.from + "->" + command.to);
            switch(command.state) {
                case WAITING:
                    env.append(" W ");
                    break;
                case REGISTERING:
                    env.append(" R ");
                    break;
                case IN_ELEVATOR:
                    env.append(" IN ");
                    break;
                case COMPLETED:
                    env.append(" C ");
                    break;
            }
            if (getAgentWithCommand(command) != null && !command.state.equals(Command.CommandStates.REGISTERING) && !command.state.equals(Command.CommandStates.COMPLETED)) {
                try {
                    env.append(((ElevatorAgent)getAgentWithCommand(command)).getId());
                }
                catch (Exception e) {
                    continue;
                }
            }
            if (iteration % 4 == 0) {
                env.append("\n");
            }
            else {
//                appendCharacter(" ", 10, env);
                env.append("          ");
            }
            iteration++;
        }
        System.out.println(env.toString());
    }

    public void appendCharacter(String c, long no, StringBuilder b) {
        for (int j = 0; j < no; j++) {
            b.append(c);
        }
    }

    public Set<Command> getAllCommands() {
        Set<Command> commands = new HashSet<>();
        for(ElevatorAgent agent: elevatorAgents) {
            commands.addAll(agent.getPeople());
        }
        for(FloorAgent agent: floorAgents) {
            commands.addAll(agent.getPeople());
        }
        return commands;
    }

    public Agent getAgentWithCommand(Command command){
        for(ElevatorAgent agent: elevatorAgents) {
            if (agent.getPeople().contains(command)){
                return agent;
            }
        }
        for(FloorAgent agent: floorAgents) {
            if (agent.getPeople().contains(command)){
                return agent;
            }
        }
        return null;
    }

    public boolean goalsCompleted() {
        return stop;
    }

    @Override
    public Set<AgentMessage> response(Set<AgentMessage> perceptions) throws InterruptedException {
        return null;
    }
}
