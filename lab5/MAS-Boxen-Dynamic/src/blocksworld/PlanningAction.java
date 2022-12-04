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
		 * In this case, the {@link PlanningAction} instance specifies:
		 * <ul>
		 * <li>a number of actions to remove from the beginning of the existing plan.
		 * <li>the actions to insert at the beginning of the existing plan, as the list of actions in this plan.
		 * </ul>
		 * For example, if the existing plan is PICKUP(B), STACK(B,C), a {@link #MODIFY_PLAN} action could contain a
		 * list of actions GOTO_STATION(#1), PICKUP(B), GOTO_STATION(#3), and could specify that 1 action from the
		 * original plan should be removed. The result (computed by the environment) would be the following plan:
		 * GOTO_STATION(#1), PICKUP(B), GOTO_STATION(#3), MARKER, STACK(B,C), where MARKER is a special action added by
		 * the environment to mark the plan modification.
		 */
		MODIFY_PLAN,
	}
	
	/**
	 * The type.
	 */
	PlanningActionType actionType = PlanningActionType.CONTINUE_PLAN;
	
	/**
	 * If the action is a plan modification, the number of actions to remove from the beginning of <b>the original
	 * plan</b>.
	 */
	int actionsToRemoveInModification = -1;
	
	/**
	 * Creates a new {@link PlanningAction}.
	 * 
	 * @param type
	 *            - the type of the planning action.
	 */
	public PlanningAction(PlanningActionType type) {
		actionType = type;
	}
	
	/**
	 * Adds one action and returns the instance.
	 * 
	 * @param act
	 *            - the action to add.
	 * @return this instance itself.
	 */
	public PlanningAction addAction(BlocksWorldAction act) {
		add(act);
		return this;
	}
	
	/**
	 * For a {@link PlanningActionType#MODIFY_PLAN} action, the number of actions to remove from the beginning of <b>the
	 * original plan</b>.
	 * 
	 * @param nActions
	 *            - the number of actions to remove.
	 * @return this instance itself.
	 */
	public PlanningAction removeFromOriginalPlan(int nActions) {
		if(actionType != PlanningActionType.MODIFY_PLAN)
			throw new IllegalStateException("Can only call this method on a modification action.");
		actionsToRemoveInModification = nActions;
		return this;
	}
	
	/**
	 * @return For a {@link PlanningActionType#MODIFY_PLAN} action, the number of actions to remove from the beginning
	 *         of <b>the original plan</b>.
	 */
	public int getNumberOfActionsToRemove() {
		return actionsToRemoveInModification;
	}
	
	@Override
	public String toString() {
		return actionType + ": " + super.toString();
	}
}
