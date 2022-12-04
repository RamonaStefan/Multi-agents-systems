package my;

import base.Action;
import base.Agent;
import base.Perceptions;
import gridworld.GridRelativeOrientation;

import java.util.Random;

/**
 * Your implementation of a reactive cleaner agent.
 * 
 * @author Andrei Olaru
 */
public class MyAgent implements Agent
{
	@Override
	public Action response(Perceptions perceptions)
	{
		// TODO Auto-generated method stub
		MyEnvironment.MyAgentPerceptions percept = (MyEnvironment.MyAgentPerceptions) perceptions;
		System.out.println("Agent sees current tile is " + (percept.isOverJtile() ? "dirty" : "clean")
				+ "; current orientation is " + percept.getAbsoluteOrientation() + "; obstacles at: "
				+ percept.getObstacles());
		Random r = new Random();
		// clean
		if(percept.isOverJtile())
			return MyEnvironment.MyAction.PICK;
		// move
		int option = r.nextInt(10);
		if (!percept.getObstacles().contains(GridRelativeOrientation.FRONT)) {
			if( option != 0) {
				return MyEnvironment.MyAction.FORWARD;
			}
		}
		return MyEnvironment.MyAction.TURN_RIGHT;
	}
	
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		// please use a single character
		return "R";
	}
	
}
