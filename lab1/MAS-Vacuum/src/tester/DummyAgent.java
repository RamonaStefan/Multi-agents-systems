package tester;

import base.Action;
import base.Agent;
import base.Perceptions;
import gridworld.GridRelativeOrientation;
import my.MyEnvironment.MyAction;
import my.MyEnvironment.MyAgentPerceptions;

/**
 * A simple agent that goes around in a circle and picks trash if it finds any.
 * 
 * @author andreiolaru
 */
public class DummyAgent implements Agent
{
	@Override
	public Action response(Perceptions perceptions)
	{
		MyAgentPerceptions percept = (MyAgentPerceptions) perceptions;
		System.out.println("Dummy sees current tile is " + (percept.isOverJtile() ? "dirty" : "clean")
				+ "; current orientation is " + percept.getAbsoluteOrientation() + "; obstacles at: "
				+ percept.getObstacles());
		// // clean
		if(percept.isOverJtile())
			return MyAction.PICK;
		// // turn
		if(percept.getObstacles().contains(GridRelativeOrientation.FRONT))
			return MyAction.TURN_RIGHT;
		// forward
		return MyAction.FORWARD;
	}
	
	@Override
	public String toString()
	{
		return "D";
	}
}
