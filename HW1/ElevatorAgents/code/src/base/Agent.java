package base;

import my.AgentMessage;

import java.util.Set;

public interface Agent
{
    public Set<AgentMessage> response(Set<AgentMessage> perceptions) throws InterruptedException;

    @Override
    public String toString();
}
