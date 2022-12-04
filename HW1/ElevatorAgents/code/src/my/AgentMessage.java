package my;

import java.util.HashSet;
import java.util.Set;

import base.Agent;

public class AgentMessage
{
	protected Agent	sender;
	protected Agent	destination;
	protected Object content;
	protected String info = null;
	
	public AgentMessage(Agent sender, Agent destination, Object content)
	{
		this.sender = sender;
		this.destination = destination;
		this.content = content;
	}

	public AgentMessage(Agent sender, Agent destination, Object content, String info)
	{
		this.sender = sender;
		this.destination = destination;
		this.content = content;
		this.info = info;
	}
	
	public Agent getSender()
	{
		return sender;
	}
	
	public Agent getDestination()
	{
		return destination;
	}
	
	public Object getContent()
	{
		return content;
	}
	
	@Override
	public String toString()
	{
		return "[" + sender + "@" + destination + ":" + content + "]";
	}
	
	public static Set<AgentMessage> filterMessagesFor(Set<AgentMessage> allMessages, Agent destinationID)
	{
		Set<AgentMessage> result = new HashSet<>();
		for(AgentMessage m : allMessages)
			if(m.destination.equals(destinationID))
				result.add(m);
		return result;
	}
}
