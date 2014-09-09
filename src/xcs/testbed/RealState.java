package xcs.testbed;

import xcs.State;

public class RealState implements State<RangeCondition>{

	private double[] state;
	private Range[] extremes;

	public RealState(double[] state){
		this.state = state;
		extremes = new Range[state.length];
		for(int i = 0;i < extremes.length;i ++){
			extremes[i] = new Range(0.5, 0.5);
		}
	}

	public RealState(double[] state, Range[] extremes){
		this.state = state;
		this.extremes = extremes;
	}

	/**
	 * @param i The bit to get
	 * @return The double value at the ith position in this state
	 */
	public double getBitAt(int i){
		if(i < 0 || i >= state.length)return Double.NaN;
		return state[i];
	}

	/**
	 * Creates a new RangeCondition based on this state, of the given length
	 */
	@Override
	public RangeCondition makeCondition(int length, double specificityProbability) {
		Range[] ranges = new Range[length];
		for(int i = 0;i < length;i ++){
			if(Math.random() < specificityProbability)ranges[i] = new Range(state[i], Math.random() * 0.75 * extremes[i].getStretch());
			else ranges[i] = extremes[i];
		}

		return new RangeCondition(ranges);
	}

}
