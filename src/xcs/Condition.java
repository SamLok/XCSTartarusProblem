package xcs;

/**
 * A condition for use in an XCS system. Has a series of specific values
 * and wildcards which can potentially match states. Defines various methods for
 * mutating and crossing over conditions.
 *
 * @param <S> The type of state this condition could match
 * @param <C> The type of Condition (Should be the class implementing this interface) that can be crossed over and compared
 * 				with this Condition
 */
public interface Condition<S extends State<?>, C extends Condition<S, C>> extends Cloneable {

	/**
	 * Mutates this Condition using the given state and the mutation probability
	 * @param state The state to mutate with
	 * @param mu The probability to mutate
	 */
	public void mutate(S state, double mu);

	/**
	 * Checks whether this Condition matches the given state.
	 * @param state The state to check
	 * @return If this condition is a match to the given state
	 */
	public boolean matchesState(S state);

	/**
	 * Performs a crossover with the given classifier swapping random alleles.
	 * @param c2 The Condition to crossover with
	 */
	public void crossover(C c2);

	/**
	 * Checks whether the bit'th term in this Condition is a wildcard (Don't care symbol)
	 * @param bit The bit to check (0 <= bit <= stateDim).
	 * @return Whether a wildcard is at the specified position
	 */
	public boolean isWildcardAtBit(int bit);

	/**
	 * @return The number of wildcard (Don't care symbols) in this Condition
	 */
	public int wildcardCount();

	/**
	 * Returns whether this condition is 'Nore General' that the given one.
	 * More general means that this has more wildcards, and that every bit in
	 * this Condition is either a wildcard or the same as the given Condition
	 * @param specific The condition to check if this is more general than
	 * @return Whether or not this Condition is more general than the given one
	 */
	public boolean isMoreGeneral(C specific);

	/**
	 * {@inheritDoc}
	 */
	public C clone();
}
