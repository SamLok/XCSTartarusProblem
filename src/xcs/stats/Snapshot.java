package xcs.stats;

import java.util.List;
import java.util.Set;

import xcs.Classifier;
import xcs.Condition;

public class Snapshot {
	private final int populationSize;
	private final double macroClassifierProportion;
	private final double averageFitness;
	private final double averageSpecificity;
	private final int time;
	private final double performance;

	public <A, C extends Condition<?, C>> Snapshot(int timestamp, Set<Classifier<A, C>> population, double perf, int length){
		time = timestamp;
		populationSize = population.size();
		macroClassifierProportion = calculateMacroPopulationProportion(population);
		averageFitness = calculateAverageFitness(population);
		averageSpecificity = calculateAverageSpecificity(population, length);
		performance = perf;
	}

	private Snapshot(int popSize, double perf, double macroClassifierProp, double avFitness, double avSpec, int stamp){
		populationSize = popSize;
		macroClassifierProportion = macroClassifierProp;
		averageFitness = avFitness;
		averageSpecificity = avSpec;
		time = stamp;
		performance = perf;
	}

	private <A, C extends Condition<?, C>> double calculateMacroPopulationProportion(Set<Classifier<A, C>> population){
		int numSum = 0;
		for(Classifier<A, C> classifier : population){
			if(classifier.getNum() > 1){
				numSum ++;
			}
		}

		if(numSum == 0)return 0;

		return numSum / (double)population.size();
	}

	private <A, C extends Condition<?, C>> double calculateAverageFitness(Set<Classifier<A, C>> population){
		double fitnessSum = 0;
		int numSum = 0;
		for(Classifier<A, C> classifier : population){
			fitnessSum += classifier.getFitness() * classifier.getNum();
			numSum += classifier.getNum();
		}

		if(numSum == 0)return 0;

		return fitnessSum / numSum;
	}

	private <A, C extends Condition<?, C>> double calculateAverageSpecificity(Set<Classifier<A, C>> population, int length){
		int specificitySum = 0;
		for(Classifier<A, C> classifier : population){
			specificitySum += (length - classifier.getNumWildcards());
		}

		return specificitySum / (double)population.size();
	}

	public int getTimestamp(){
		return time;
	}

	public double getPopulationSize(){
		return populationSize;
	}

	public double getAverageFitness(){
		return averageFitness;
	}

	public double getAverageSpecificity(){
		return averageSpecificity;
	}

	public double getMacroClassifierProportion(){
		return macroClassifierProportion;
	}

	public double getPerformance(){
		return performance;
	}

	@Override
	public String toString(){
		StringBuilder build = new StringBuilder();
		build.append(String.format("Snapshot at %d timesteps%n", time));
		build.append(String.format("Population Size: %d%n", populationSize));
		build.append(String.format("Average Fitness across Classifiers: %3.2f%n", averageFitness));
		build.append(String.format("Average Specificity: %3.2f%n", averageSpecificity));
		build.append(String.format("Macro Classifier Proportion: %3.2f%n", macroClassifierProportion));
		build.append(String.format("Performance: %3.2f%n", performance));

		return build.toString();
	}

	public String toCSV(){
		StringBuilder build = new StringBuilder();
		build.append(time);
		build.append(", ");
		build.append(populationSize);
		build.append(", ");
		build.append(averageFitness);
		build.append(", ");
		build.append(averageSpecificity);
		build.append(", ");
		build.append(macroClassifierProportion);
		build.append(", ");
		build.append(performance);
		build.append("\n");

		return build.toString();
	}

	public static Snapshot average(List<Snapshot> snapshots){
		if(snapshots.size() == 0){
			return new Snapshot(0, 0, 0, 0, 0, 0);
		}
		double avPopSize = 0;
		double avFitness = 0;
		double avSpec = 0;
		double avMacroProp = 0;
		double perf = 0;

		for(Snapshot snapshot : snapshots){
			avPopSize += snapshot.populationSize;
			avFitness += snapshot.averageFitness;
			avSpec += snapshot.averageSpecificity;
			avMacroProp += snapshot.macroClassifierProportion;
			perf += snapshot.performance;
		}

		int size = snapshots.size();
		return new Snapshot((int)(avPopSize / size), perf / size, avMacroProp / size, avFitness / size, avSpec / size, snapshots.get(0).time);
	}
}


