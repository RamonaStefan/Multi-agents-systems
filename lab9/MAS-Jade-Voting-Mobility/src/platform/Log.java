package platform;

import jade.core.Agent;

/**
 * Class helping with logging to standard output.
 * 
 * @author Andrei Olaru
 */
public class Log
{
	/**
	 * Prints to standard output a message emmited by the agent, formed of the objects in the output.
	 * 
	 * @param agent
	 *            - the agent logging the message.
	 * @param output
	 *            - components of the output.
	 */
	public static void log(Agent agent, Object... output)
	{
		String out = agent.getLocalName() + ": ";
		for(Object o : output)
			out += (o != null ? o.toString() : "<null>") + " ";
		System.out.println(out);
	}
}
