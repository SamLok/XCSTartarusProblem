package xcs.testbed;

import xcs.Condition;

public class StringCondition implements Condition<StringState, StringCondition> {

	private String condition;
	public static final char wildcard = '*';

	public StringCondition(String condition){
		this.condition = condition;
	}

	/**
	 * @return The string representing this condition
	 */
	public String getCondition(){
		return condition;
	}

	/**
	 * Sets the string that is the condition in this StringCondition
	 * @param con The new condition String
	 */
	public void setCondition(String con){
		this.condition = con;
	}

	/**
	 * Mutates this condition, swapping each bit between a wildcard
	 * and the equivalent bit in the given StringState with a probability of mu
	 *
	 * @param state the State to mutate with
	 * @param mu the probability that any given bit will be mutated
	 */
	@Override
	public void mutate(StringState state, double mu) {
		String strState = state.getState();
		char[] conditionChars = condition.toCharArray();

		for(int i = 0;i < conditionChars.length;i ++){
			if(Math.random() < mu){
				if(conditionChars[i] == wildcard){
					conditionChars[i] = strState.charAt(i);
				}
				else{
					conditionChars[i] = wildcard;
				}
			}
		}

		condition = new String(conditionChars);
	}

	/**
	 * Checks whether this condition matches the given StringState.
	 * It is a match if every bit in this condition is either a wildcard,
	 * or the same as the equivalent bit in the given StringState.
	 *
	 * @param state The state to check
	 * @return Whether this condition matches the given state
	 */
	@Override
	public boolean matchesState(StringState state) {
		int length = condition.length();

		String strState = state.getState();

		for(int i = 0;i < length;i ++){
			if(!isWildcardAtBit(i) && condition.charAt(i) != strState.charAt(i)){
				return false;
			}
		}

		return true;
	}

	/**
	 * @return Whether the char at the given position is a wildcard
	 */
	@Override
	public boolean isWildcardAtBit(int bit) {
		return condition.charAt(bit) == wildcard;
	}

	/**
	 * @return The number of wildcards in this conditions String.
	 */
	@Override
	public int wildcardCount() {
		//numWildcards = length of condition - length of condition with all wildcards removed
		int count = 0;
		for(char c : condition.toCharArray()){
			if(c == wildcard)++count;
		}
		
		return count;
	}

	/**
	 * Performs a crossover between this Condition and the given one, updating
	 * both Conditions. Swaps a random number of bits between the two conditions.
	 * @param c2 The StringCondition to crossover with
	 */
	@Override
	public void crossover(StringCondition c2) {
		int x = (int)(Math.random() * (condition.length() + 1));
		int y = (int)(Math.random() * (condition.length() + 1));
		if(x > y){
			//Swap x and y
			int temp = x;
			x = y;
			y = temp;
		}

		char[] c1Chars = getCondition().toCharArray();
		char[] c2Chars = c2.getCondition().toCharArray();
		for(int i = x;i < y;++ i){
			if(x <= i && i < y){
				//Swap the chars in each condition
				char temp = c1Chars[i];
				c1Chars[i] = c2Chars[i];
				c2Chars[i] = temp;
			}
		}

		setCondition(String.valueOf(c1Chars));
		c2.setCondition(String.valueOf(c2Chars));
	}

	/**
	 * Checks whether this StringCondition is more general that the given one,
	 * that is for every position in this condition, it is either a wildcard or equal
	 * to the given Condition
	 * @return Whether this Condition is mor general than the given one.
	 */
	@Override
	public boolean isMoreGeneral(StringCondition specific) {
		String coG = getCondition();
		String coS = specific.getCondition();

		int wildcardGeneralCount = wildcardCount();
		int wildcardSpecificCount = specific.wildcardCount();

		if(wildcardGeneralCount <= wildcardSpecificCount)return false;

		for(int i = 0;i < coG.length();i ++){
			if(!(coG.charAt(i) == wildcard || coG.charAt(i) == coS.charAt(i))){
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString(){
		return condition;
	}

	@Override
	public int hashCode() {
		return condition.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)return true;
		if (obj == null)return false;
		if (getClass() != obj.getClass())return false;

		StringCondition other = (StringCondition) obj;
		return other.getCondition().equals(getCondition());
	}

	@Override
	public StringCondition clone(){
		return new StringCondition(condition);
	}
}
