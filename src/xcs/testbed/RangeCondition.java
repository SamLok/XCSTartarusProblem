package xcs.testbed;

import xcs.Condition;

public class RangeCondition implements Condition<RealState, RangeCondition>{

	Range[] ranges;

	public RangeCondition(Range[] ranges){
		this.ranges = ranges;
	}

	@Override
	public void mutate(RealState state, double mu) {
		for(int i = 0;i < ranges.length;i ++){
			Range range = ranges[i];
			if(Math.random() < mu){
				//-0.1 < change < 0.1
				double change = (Math.random() < 0.5 ? 1 : -1) * (Math.random() * 0.1);
				ranges[i].setCenter(range.getCenter() + change);
			}

			if(Math.random() < mu){
				double change = (Math.random() < 0.5 ? 1 : -1) * (Math.random() * 0.1);
				ranges[i].setStretch(range.getStretch() + change);
			}
		}
	}

	/**
	 * Performs a crossover with the given RangeCondition.
	 * For each bit selected to crossover, selects a random allele from
	 * both this and the given condition and swaps them.
	 */
	@Override
	public void crossover(RangeCondition c2) {
		int x = (int)(Math.random() * (ranges.length + 1));
		int y = (int)(Math.random() * (ranges.length + 1));
		if(x > y){
			//Swap x and y
			int temp = x;
			x = y;
			y = temp;
		}

		for(int i = x;i < y;++ i){
			if(x <= i && i < y){
				Range r1 = ranges[i];
				Range r2 = c2.ranges[i];
				//Equiprobable to swap any of the combinations of center and stretch
				//0 = center 1 = stretch 2 = c2.center 3 = c2.stretch
				int allele1 = 0;
				int allele2 = 0;
				do{
					allele1 = (int)(Math.random() * 4);
					allele2 = (int)(Math.random() * 4);
				}while(allele1 == allele2);

				if(allele1 > allele2){
					int temp = allele1;
					allele1 = allele2;
					allele2 = temp;
				}

				double temp = -1;
				switch(allele1){
				case 0:
					temp = r1.getCenter();
					break;
				case 1:
					temp = r1.getStretch();
					break;
				case 2:
					temp = r2.getCenter();
					break;
				}

				if(allele1 == 0 && allele2 == 1){
					//Swap center and stretch
					r1.setCenter(r1.getStretch());
					r1.setStretch(temp);
				}
				else if(allele1 == 0 && allele2 == 2){
					//Swap center and c2.center
					r1.setStretch(r2.getCenter());
					r2.setCenter(temp);
				}
				else if(allele1 == 0 && allele2 == 3){
					//Swap center and c2.stretch
					r1.setCenter(r2.getStretch());
					r2.setStretch(temp);
				}
				else if(allele1 == 1 && allele2 == 2){
					//Swap stretch and c2.center
					r1.setStretch(r2.getCenter());
					r2.setCenter(temp);
				}
				else if(allele1 == 1 && allele2 == 3){
					//Swap stretch and c2.stretch
					r1.setStretch(r2.getStretch());
					r2.setStretch(temp);
				}
				else if(allele1 == 2 && allele2 == 3){
					//Swap c2.center and c2.stretch
					r2.setCenter(r2.getStretch());
					r2.setStretch(temp);
				}
			}
		}
	}

	@Override
	public boolean matchesState(RealState state) {
		for(int i = 0;i < ranges.length;i ++){
			if(!ranges[i].contains(state.getBitAt(i)))return false;
		}
		return true;
	}

	@Override
	public boolean isWildcardAtBit(int bit) {
		Range range = ranges[bit];
		return range.contains(new Range(0.5, 0.5));
	}

	@Override
	public int wildcardCount() {
		int count = 0;
		for(int i = 0;i < ranges.length;i ++){
			if(isWildcardAtBit(i))++count;
		}

		return count;
	}

	@Override
	public boolean isMoreGeneral(RangeCondition specific) {
		int wildcardGeneralCount = wildcardCount();
		int wildcardSpecificCount = specific.wildcardCount();

		if(wildcardGeneralCount <= wildcardSpecificCount)return false;

		for(int i = 0;i < ranges.length;i ++){
			if(!(isWildcardAtBit(i) || ranges[i].contains(specific.ranges[i]))){
				return false;
			}
		}

		return true;
	}

	@Override
	public RangeCondition clone(){
		Range[] range = new Range[ranges.length];
		for(int i = 0;i < range.length;i ++)range[i] = new Range(ranges[i].getCenter(), ranges[i].getStretch());
		return new RangeCondition(range);
	}

	@Override
	public String toString(){
		StringBuilder build = new StringBuilder();
		for(int i = 0;i < ranges.length;i ++){
			if(isWildcardAtBit(i))build.append("*");
			else build.append(ranges[i]);
			build.append(", ");
		}

		return build.toString();
	}

}
