package xcs.testbed;

public class RealStateBuilder {

	private Range[] extremes;

	public RealStateBuilder(int numBits){
		extremes = new Range[numBits];
		for(int i = 0;i < extremes.length;i ++){
			extremes[i] = Range.fromMinMax(0, 1);
		}
	}

	private void updateExtremes(double[] bits){
		for(int i = 0;i < bits.length;i ++){
			if(extremes[i] == null)Range.fromMinMax(0, 1);
			else{
				double min = Math.min(bits[i], extremes[i].getMin());
				double max = Math.max(bits[i], extremes[i].getMax());
				extremes[i].setCenter((min + max) / 2);
				extremes[i].setStretch((max - min) / 2);
			}
		}
	}

	public RealState makeState(double[] values){
		updateExtremes(values);
		return new RealState(values, extremes);
	}

	public void reset(){
		extremes = new Range[extremes.length];
		for(int i = 0;i < extremes.length;i ++){
			extremes[i] = Range.fromMinMax(0, 1);
		}
	}
}
