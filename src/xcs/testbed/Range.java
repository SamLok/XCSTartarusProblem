package xcs.testbed;

/**
 * A simple range implementation that has a center and stretches out
 * in either direction from that center.
 *
 */
public class Range {
	private double center;
	private double stretch;

	public Range(double center, double stretch){
		this.center = center;
		this.stretch = stretch;
	}

	/**
	 * Calculates the minimum value in this range
	 * @return The minimum value in this range
	 */
	public double getMin(){
		return center - stretch;
	}

	/**
	 * Calculates the maximum number in this range
	 * @return The maximum number in this range
	 */
	public double getMax(){
		return center + stretch;
	}


	/**
	 * @return The center of this range
	 */
	public double getCenter(){
		return center;
	}

	/**
	 * @return How much this range stretches either side of the center
	 */
	public double getStretch(){
		return stretch;
	}

	/**
	 * Checks whether this range completely contains the given one  that is
	 * this minimum is less than the given minimum and this maximum is greater than the given maxium
	 * @param r The range to check is inside this one
	 * @return Whether this range completly contains the given one
	 */
	public boolean contains(Range r){
		return r.getMin() >= getMin() && r.getMax() <= getMax();
	}

	/**
	 * Checks whether the given value is inside this range that is >= min and <= max
	 * @param value The value to check
	 * @return Whether the value falls into this range
	 */
	public boolean contains(double value){
		return value > getMin() && value < getMax();
	}


	public void setCenter(double center){
		this.center = center;
	}

	public void setStretch(double stretch){
		this.stretch = stretch;
	}

	@Override
	public String toString(){
		return String.format("[%3.2f -> %3.2f]", getMin(), getMax());
	}

	public static Range fromMinMax(double min, double max){
		return new Range((max + min) / 2, (max - min) / 2);
	}
}
