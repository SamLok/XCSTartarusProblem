package xcs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PredictionArray<A> {
	private Map<A, Double> predictions;

	public PredictionArray(){
		predictions = new HashMap<A, Double>();
	}

	public void addPrediction(A action, double d){
		predictions.put(action, d);
	}

	public double getPrediction(A action){
		return predictions.get(action);
	}

	public PredictionArray<A> normalize(){
		double sum = 0;
		for(Double d : predictions.values()){
			sum += d;
		}

		for(Map.Entry<A, Double> d : predictions.entrySet()){
			predictions.put(d.getKey(), d.getValue() / sum);
		}
		return this;
	}

	public A getActionDeterministic(){
		A max = null;
		for(A action : predictions.keySet()){
			if(max == null || predictions.get(action) > predictions.get(max)){
				max = action;
			}
		}
		return max;
	}

	public static <A, C extends Condition<?, C>> PredictionArray<A> fromMatchSetTheta(A[] actionSet, Set<Classifier<A, C>> setM){
		List<A> actionList = Arrays.asList(actionSet);
		PredictionArray<A> pred = new PredictionArray<A>();
		double[] PA = new double[actionSet.length];
		for(Classifier<A, C> classifier : setM){
			int index = actionList.indexOf(classifier.getAction());
			PA[index] += classifier.getTheta();
		}

		for(int i = 0;i < PA.length;i ++){
			if(PA[i] > 20)PA[i] = 20;
			else if(PA[i] < -20)PA[i] = -20;
			pred.addPrediction(actionSet[i], Math.exp(PA[i]));
		}

		return pred;
	}

	public static <A, C extends Condition<?, C>> PredictionArray<A> fromMatchSetPrediction(A[] actionSet, Set<Classifier<A, C>> setM){
		List<A> actionList = Arrays.asList(actionSet);
		PredictionArray<A> pred = new PredictionArray<A>();
		double[] PA = new double[actionSet.length];
		double[] FSA = new double[actionSet.length];
		for(Classifier<A, C> classifier : setM){
			int pos = actionList.indexOf(classifier.getAction());
			PA[pos] += classifier.getPrediction() * classifier.getFitness();
			FSA[pos] += classifier.getFitness();
		}

		for(int i = 0;i < actionSet.length;i ++){
			if(FSA[i] > 0){
				pred.addPrediction(actionSet[i], PA[i] / FSA[i]);
			}
		}

		return pred;
	}
}
