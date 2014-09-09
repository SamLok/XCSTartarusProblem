package xcs;

/**
 * An interface that outlines the contract of an Environment in an XCS,
 * operating with actions of the type A.
 *
 * @param <A> The type of actions in the action set that the XCS will be providing
 * @param <S> The type of State that this Environment will provide
 */
public interface Environment<S extends State<?>, A> {
	/**
	 * Calculates and returns the current state of the environment
	 * @return The current state of the environment
	 */
	public S getState();

	/**
	 * Executes the given action, given the state the action is to be performed in
	 * and returns the observed reward.
	 * @param state The state the action is to be implemented in
	 * @param action The action that is to be performed
	 * @return The observed reward as a result of executing that action
	 */
	public double getReward(S state, A action);

	/**
	 * Checks whether the given state marks the end of the problem
	 * @param state The state to check
	 * @return Whether the given state marks the end of the problem
	 */
	public boolean isFinalState(S state);
}
