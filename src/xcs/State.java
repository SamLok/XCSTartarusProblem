package xcs;

/**
 * A state for use in an XCS system. Only guarantees one method - constructing a condition
 *
 * @param <C> The type of condition that this state constructs
 */
public interface State<C extends Condition<?, C>> {

	/**
	 * Constructs a new condition of type C based on this State
	 * @param length The number of 'bits' in the condition
	 * @param specificityProbability (1 - chance of being a wildcard in any given bit)
	 * @return The new Condition
	 */
	public C makeCondition(int length, double specificityProbability);
}
