package my;

import base.Agent;
import java.util.*;

public class ElevatorAgent implements Agent {
    public int id;
    public ElevatorStates state = ElevatorStates.CLOSED;
    public int position = 1;
    public int direction = 0;
    public long capacity = 0;
    public long maxCapacity;
    public long Tdoors;
    public long TdoorsIterrator1 = 0;
    public long TdoorsIterrator2 = 0;
    public long TdoorsIterrator3 = 0;
    public long Tclose;
    public long TcloseIterrator = 0;
    public long Ttransit;
    public long TtransitIterrator = 0;
    public long Taccel;
    public long TaccelIterrator1 = 0;
    public long TaccelIterrator2 = 0;
    public long TaccelIterrator3 = 0;
    public long Tslow;
    public long TslowIterrator = 0;
    public long TslowIterrator2 = 0;
    public long Tfast;
    public long TfastIterrator = 0;
    public long TfastIterrator2 = 0;
    public long destination = -1;
    public long start = -1;


    public ArrayList<Command> people = new ArrayList<>();

    public ElevatorAgent(int id, long Tdoors, long Tclose, long Ttransit, long Taccel, long Tslow, long Tfast, long maxCapacity){
        this.id = id;
        this.Taccel = Taccel;
        this.Ttransit = Ttransit;
        this.Tslow = Tslow;
        this.Tclose = Tclose;
        this.Tdoors = Tdoors;
        this.Tfast = Tfast;
        this.maxCapacity = maxCapacity;
    }

    public Set<AgentMessage> response(Set<AgentMessage> perceptions) {
        Set<AgentMessage> messages = new HashSet<>();
        long noMaxAfirm = maxCapacity - capacity;

        // delete complete commands
        Set<Command> toDelete = new HashSet<>();
        for (Command c: people) {
            if(c.state.equals(Command.CommandStates.COMPLETED)) {
                toDelete.add(c);
            }
        }
        people.removeAll(toDelete);

        // get messages
        for (AgentMessage msg : perceptions) {
            if (people.size() == 0 && msg.info.equals("ASK") && noMaxAfirm > 0 ){
                messages.add(new AgentMessage(msg.destination, msg.sender, msg.content, "AFIRMATIVE " + position));
                noMaxAfirm--;
            }
            else if(!state.equals(ElevatorStates.IN_TRANSIT) && people.size() > 0 && msg.info.equals("ASK") && noMaxAfirm > 0 && (isSameDirection((Command) msg.getContent()) || direction == 0)) {
                messages.add(new AgentMessage(msg.destination, msg.sender, msg.content, "AFIRMATIVE " + position));
                noMaxAfirm--;
            }
            else if(state.equals(ElevatorStates.IN_TRANSIT) && msg.info.equals("ASK") && noMaxAfirm > 0 && Math.abs(position - start) > 4) {
                messages.add(new AgentMessage(msg.destination, msg.sender, msg.content, "AFIRMATIVE " + position));
                noMaxAfirm--;
            }
            else if (msg.info.equals("DO")) {
                people.add((Command) msg.getContent());
            }
        }

        //moving
        if (people.size() > 0) {
            if (state.equals(ElevatorStates.CLOSED)) {
                destination = getFirstDestination();
                start = getFirstStart();
                setDirection(start, destination);
                long position_copy = position;
                if (start != position) {
                    setDirection(position_copy, start);
                    if (Math.abs(position_copy - start) == 1) {
                        if (TslowIterrator2 < Tslow) {
                            TslowIterrator2++;
                            return messages;
                        }
                        if (TslowIterrator2 == Tslow) {
                            TslowIterrator2++;
                            if (direction == 1) {
                                position++;
                            } else {
                                position--;
                            }
                        }

                    } else {
                        float d = Math.signum(position_copy - start);
                        if (TaccelIterrator3 < Taccel) {
                            if (TaccelIterrator3 % (Taccel / d) == 0) {
                                if (direction == 1) {
                                    position++;
                                } else {
                                    position--;
                                }
                            }
                            TaccelIterrator3++;
                            return messages;
                        }

                        long TfastDuration = (Tfast * (Math.abs(position_copy - start) - 2));
                        if (TfastIterrator2 < TfastDuration) {
                            if (TfastIterrator2 % (TfastDuration / Math.abs(position_copy - start - 2 * d)) == 0) {
                                if (direction == 1) {
                                    position++;
                                } else {
                                    position--;
                                }
                            }
                            TfastIterrator2++;
                            return messages;
                        }

                        if (TaccelIterrator2 < Taccel) {
                            if (TaccelIterrator2 % (Taccel / d) == 0) {
                                if (direction == 1) {
                                    position++;
                                } else {
                                    position--;
                                }
                            }
                            TaccelIterrator2++;
                            return messages;
                        }
                    }
                    position = (int) start;
                    state = ElevatorStates.DOORS_OPENING;
                }
                else {
                    for (Command c: people) {
                        if(c.from == position && c.state.equals(Command.CommandStates.WAITING)) {
                            c.state = Command.CommandStates.IN_ELEVATOR;
                            capacity++;
                        }
                    }
                    state = ElevatorStates.IN_TRANSIT;
                }
            }
            if (state.equals(ElevatorStates.IN_TRANSIT)) {
                if (Math.abs(destination - start) == 1) {
                    if (TslowIterrator < Tslow) {
                        TslowIterrator++;
                        return messages;
                    }
                    if (TslowIterrator == Tslow) {
                        TslowIterrator++;
                        if (direction == 1) {
                            position++;
                        } else {
                            position--;
                        }
                    }

                } else {
                    float d = Math.signum(destination - start);
                    if (TaccelIterrator1 < Taccel) {
                        if (TaccelIterrator1 % (Taccel / d) == 0) {
                            if (direction == 1) {
                                position++;
                            } else {
                                position--;
                            }
                        }
                        TaccelIterrator1++;
                        return messages;
                    }

                    long TfastDuration = (Tfast * (Math.abs(destination - start) - 2));
                    if (TfastIterrator < TfastDuration) {
                        if (TfastIterrator % (TfastDuration / Math.abs(destination - start - 2 * d)) == 0) {
                            if (direction == 1) {
                                position++;
                            } else {
                                position--;
                            }
                        }
                        TfastIterrator++;
                        return messages;
                    }

                    if (TaccelIterrator2 < Taccel) {
                        if (TaccelIterrator2 % (Taccel / d) == 0) {
                            if (direction == 1) {
                                position++;
                            } else {
                                position--;
                            }
                        }
                        TaccelIterrator2++;
                        return messages;
                    }
                }
                position = (int) destination;
                state = ElevatorStates.DOORS_OPENING;
                return messages;
            }

            if (state.equals(ElevatorStates.DOORS_OPENING)) {
                // open doors
                if (TdoorsIterrator1 < Tdoors) {
                    TdoorsIterrator1++;
                    return messages;
                }
                state = ElevatorStates.OPEN;
            }

            if (state.equals(ElevatorStates.OPEN)) {
                long time = Math.max(Tclose, Ttransit * (getPeopleIn() + getPeopleOut()));
                // waits
                if (TcloseIterrator < time) {
                    if (TcloseIterrator == Ttransit * getPeopleOut()) {
                        // people get out
                        for (Command c: people) {
                            if(c.to == position) {
                                c.state = Command.CommandStates.COMPLETED;
                                capacity--;
                            }
                        }
                    }
                    // people get in
                    if (TcloseIterrator == Ttransit * (getPeopleIn() + getPeopleOut())) {
                        // people get out
                        for (Command c: people) {
                            if(c.from == position && c.state.equals(Command.CommandStates.WAITING)) {
                                c.state = Command.CommandStates.IN_ELEVATOR;
                                capacity++;
                            }
                        }
                    }
                    TcloseIterrator++;
                    return messages;
                }
                //close doors
                state = ElevatorStates.DOORS_CLOSING;
            }

            if (state.equals(ElevatorStates.DOORS_CLOSING)) {
                if (TdoorsIterrator2 < Tdoors) {
                    TdoorsIterrator2++;
                    return messages;
                }
                state = ElevatorStates.CLOSED;
            }

            resetCommand();
        }
        return messages;
    }

    public boolean isSameDirection (Command command) {
        int dir = (command.to - command.from) > 0 ? 1: -1;
        return dir == direction;
    }

    public int getPeopleOut() {
        int nr = 0;
        for (Command c : people) {
            if (c.to == position) {
                nr++;
            }
        }
        return nr;
    }

    public int getPeopleIn() {
        int nr = 0;
        for (Command c : people) {
            if (c.from == position) {
                nr++;
            }
        }
        return nr;
    }

    public void setDirection(long start, long destination) {
        direction = (start > destination) ? -1:1;
    }

    public long getFirstDestination() {
        long destination = 0;
        int distance = 99999999;
        for (Command c : people) {
            if (Math.abs(c.to - position) < distance) {
                distance = Math.abs(c.to - position);
                destination = c.to;
            }
        }
        return destination;
    }

    public long getFirstStart() {
        long destination = 0;
        int distance = 99999999;
        for (Command c : people) {
            if (Math.abs(position - c.from) < distance) {
                distance = Math.abs(position - c.from);
                destination = c.from;
            }
        }
        return destination;
    }

    public void resetCommand() {
        TdoorsIterrator1 = 0;
        TdoorsIterrator2 = 0;
        TdoorsIterrator3 = 0;
        TcloseIterrator = 0;
        TtransitIterrator = 0;
        TaccelIterrator1 = 0;
        TaccelIterrator2 = 0;
        TaccelIterrator3 = 0;
        TslowIterrator = 0;
        TfastIterrator = 0;
        TfastIterrator2 = 0;
        TslowIterrator2 = 0;
    }

    public int getId() {
        return id;
    }

    public ElevatorStates getState() {
        return state;
    }

    public int getPosition() {
        return position;
    }

    public int getDirection() {
        return direction;
    }

    public ArrayList<Command> getPeople() {
        return people;
    }

    public String getCommands() {
        StringBuilder ppl = new StringBuilder();
        for (Command i : people) {
            if (i.state == Command.CommandStates.IN_ELEVATOR) {
                ppl.append(i.name);
            }
        }
        return ppl.toString();
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public enum ElevatorStates {
        IN_TRANSIT(0),
        DOORS_OPENING(1),
        DOORS_CLOSING(2),
        OPEN(3),
        CLOSED(4);

        public Integer value;

        ElevatorStates(Integer position) {
            this.value = position;
        }

    }

}
