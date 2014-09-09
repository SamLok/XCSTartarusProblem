package xcs.testbed;

import xcs.State;

public class StringState implements State<StringCondition>{

	private final String state;

	public StringState(String state){
		this.state = state;
	}

	public String getState(){
		return state;
	}

	/**
	 * Generates a StringCondition from this StringState.
	 * @param length The number of bits (chars) to be in the created Condition. Should be the length of this state (stateDim)
	 * @param specificityProbability The probability to choose a bit form this state, rather than a wildcard
	 * @return The new condition
	 */
	@Override
	public StringCondition makeCondition(int length, double specificityProbability) {
		StringBuilder condition = new StringBuilder();
		for(int i = 0;i < length;i ++){
			if(Math.random() < specificityProbability){
				condition.append(state.charAt(i));
			}
			else{
				condition.append(StringCondition.wildcard);
			}
		}

		return new StringCondition(condition.toString());
	}

	@Override
	public int hashCode() {
		return state.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)return true;
		if (obj == null)return false;
		if (getClass() != obj.getClass())return false;

		StringState other = (StringState) obj;
		return other.getState().equals(getState());
	}

	@Override
	public String toString(){
		return state;
	}
}
