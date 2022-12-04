package blocksworld;

import java.util.LinkedList;

import base.Action;

/**
 * A planning action is a high-level action which is the result of agent planning.
 * 
 * @author Andrei Olaru
 */
public class PlanningAction extends LinkedList<BlocksWorldAction> implements Action {
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The type of a {@link PlanningAction}.
	 * 
	 * @author Andrei Olaru
	 */
	public enum PlanningActionType {
		/**
		 * No modification on the previously returned plan. Any actions in this plan are ignored.
		 */
		CONTINUE_PLAN,
		/**
		 * The previous plan is removed and the agent does nothing at the current step.
		 */
		CANCEL_PLAN,
		/**
		 * The existing plan (if any) must be replaced with this new plan.
		 */
		NEW_PLAN,
		/**
		 * for future use
		 */
		MODIFY_PLAN,
	}
	
	/**
	 * The type.
	 */
	PlanningActionType actionType = PlanningActionType.CONTINUE_PLAN;
	
	/**
	 * Creates a new {@link PlanningAction}.
	 * 
	 * @param type - the type of the planning action.
	 */
	public 	PlanningAction(PlanningActionType type) {
		actionType = type;
	}
	
	@Override
	public String toString() {
		return actionType + ": " + super.toString();
	}
}
