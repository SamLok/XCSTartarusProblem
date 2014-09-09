package xcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import xcs.XCSConstants.UpdateMethod;
import xcs.stats.Snapshot;

/**
 * @author Colin Douch
 *
 * @param <S> The type of state that the environment to this XCS will be creating
 * @param <C> The type of Condition that the state will generate
 * @param <A> The type of actions that the Environment will accept
 */
public class XCS<S extends State<C>, C extends Condition<S, C>, A> {
	/**
	 * ============
	 * Main Process
	 * ============
	 */
	/* The current match set. (All classifiers in population that match the state) */
	private Set<Classifier<A, C>> setM = new HashSet<Classifier<A, C>>();

	/* The current action set. (All classifiers in the match set that propose a given action) */
	private Set<Classifier<A, C>> setA = new HashSet<Classifier<A, C>>();

	/* The last action set from the previous time step */
	private Set<Classifier<A, C>> setAMinusOne = new HashSet<Classifier<A, C>>();

	/* The global timestamp */
	private int timestamp = 1;

	/* The set of classifier in the population */
	private Set<Classifier<A, C>> population = new HashSet<Classifier<A, C>>();

	/* The set of the discrete actions the system can take */
	private A[] actionSet;

	/* Initial ID for generating classifiers. Allows each to get a unique id*/
	private long initialClassifierID = 0;

	/* The set of parameters that are being used in this XCS */
	private final XCSConstants constants;

	private S preState = null;
	private S state = null;
	private double preRho = 0.00;
	private A preAct = null;

	private final List<Snapshot> stats = new ArrayList<Snapshot>();

	private final Random random = new Random();

	/**
	 * Creates a new XCS choosing from the specified set of actions
	 * and a default set of constants
	 * @param actions The action set to use in this XCS
	 */
	public XCS(A[] actions){
		this(new XCSConstantsBuilder().build(), actions);
	}

	/**
	 * Constructs a new XCS with the specified action set and parameters
	 * @param constants The parameters to use in this XCS
	 * @param actions The action set to use in this XCS
	 */
	public XCS(XCSConstants constants, A[] actions){
		this.constants = constants;
		this.actionSet = actions;
	}

	/**
	 * Sorts the population by experience and then prints them to stdout
	 */
	public void printPopulation(int top){
		if(population.size() == 0)System.err.println("No Population??");
		if(top < 0)top = population.size();
		Classifier<A, C>[] pop = toArray(population);
		int count = 0;
		Arrays.sort(pop, new Comparator<Classifier<A, C>>(){
			@Override
			public int compare(Classifier<A, C> c1, Classifier<A, C> c2){
				return (int)Math.signum(c1.getFitness() - c2.getFitness());
			}
		});
		for(Classifier<A, C> c : pop){
			if((population.size() - (count ++) > top))continue;
			System.out.println(c.toString());
		}
	}

	/**
	 * Runs an evaluation routine to see how optimal the system gets
	 * in a set number of iterations
	 * @param env The environment to use in this XCS
	 * @param iteration The number of iterations to run the simulation for
	 * @return A double value representing the total reward received during the running of the evaluation
	 */
	public double runXCSEvaluationSingleStep(Environment<S, A> env, int iteration){
		double performance = 0;

		setM = new HashSet<Classifier<A, C>>();
		for(int i = 0;i < iteration;i ++){
			performance += runXCSEvaluation(env, env.getState());
		}

		return performance;
	}

	public int runXCSEvaluationMultiStep(Environment<S, A> env, int finalStateBound){
		int timestamp = 1;

		int finalStateCount = 0;
		while(finalStateCount <= finalStateBound){
			S state = env.getState();
			if(env.isFinalState(state)){
				++finalStateCount;
			}
			++timestamp;
			runXCSEvaluation(env, state);
		}

		return timestamp;
	}

	private double runXCSEvaluation(Environment<S, A> env, S state){
		setM = findMatchClassifiersInPopulation(state);
		PredictionArray<A> PA = generatePredictionArray(setM);

		UpdateMethod update = constants.getUpdateMethod();

		A act = null;
		if(update.equals(UpdateMethod.NXCS) || update.equals(UpdateMethod.RXCS)){
			act = selectActionFromDistribution(PA);
		}
		else{
			act = PA.getActionDeterministic();
		}

		if(act == null)act = actionSet[(int)(Math.random() * actionSet.length)];

		return env.getReward(state, act);
	}

	public List<Snapshot> getStats(){
		return stats;
	}

	public void runXCSMultistep(Environment<S, A> env, int finalStateBound, int logCount){
		boolean logged = true;
		int finalStateCount = 0;
		timestamp = 1;
		population = new HashSet<Classifier<A, C>>();
		setM = new HashSet<Classifier<A, C>>();
		setA = new HashSet<Classifier<A, C>>();
		setAMinusOne = new HashSet<Classifier<A, C>>();
		initialClassifierID = 0;
		stats.clear();

		while(finalStateCount <= finalStateBound){
			if(finalStateCount % logCount == 0 && !logged){
				double result = runXCSEvaluationMultiStep(env, 200) / 200.0;
				System.out.printf("Final State %d reached with Algorithm %s. Logged Value: %3.2f%n", finalStateCount, constants.getUpdateMethod(), result);
				stats.add(new Snapshot(finalStateCount, population, result, constants.getStateDim()));
				logged = true;
			}

			state = env.getState();
			if(env.isFinalState(state)){
				++finalStateCount;
				logged = false;
			}

			++timestamp;
			runXCS(env, state);
		}
	}

	/**
	 * Runs the XCS on the given environment for the given amount of iterations,
	 * ignoring any final states that are encountered (There should be none if this is a
	 * single step problem)
	 * @param env The environment to run this XCS on
	 * @param iteration The maximum number of iterations to run for
	 * @param finalStateBound The maximum number of final states to encounter before exitting
	 */
	public void runXCSSingleStep(Environment<S, A> env, int iteration, int logCount){
		//Initialise
		population = new HashSet<Classifier<A, C>>();
		setM = new HashSet<Classifier<A, C>>();
		setA = new HashSet<Classifier<A, C>>();
		setAMinusOne = new HashSet<Classifier<A, C>>();
		initialClassifierID = 0;
		stats.clear();

		//Main Loop
		for(timestamp = 1;timestamp <= iteration;timestamp ++){
			runXCS(env, env.getState());
			if(timestamp % logCount == 0){
				stats.add(new Snapshot(timestamp, population, runXCSEvaluationSingleStep(env, 200) / 200, constants.getStateDim()));
			}
		}
	}

	private void runXCS(Environment<S, A> env, S state){
		boolean isFinalState = env.isFinalState(state);
		A act = null;
		PredictionArray<A> PA = null;
		if(isFinalState){
			act = actionSet[random.nextInt(actionSet.length)];
		}
		else{
			setM = generateMatchSet(state);
			PA = generatePredictionArray(setM);

			UpdateMethod method = constants.getUpdateMethod();
			if(method.equals(UpdateMethod.NXCS) || method.equals(UpdateMethod.RXCS) || method.equals(UpdateMethod.NXCS2)){
				act = selectActionFromDistribution(PA);
			}
			else{
				act = selectBestAction(PA);
			}
			setA = generateActionSet(setM, act);
		}

		if(!setAMinusOne.isEmpty() && preState != null && !env.isFinalState(preState)){
			double P;
			if(isFinalState){
				P = preRho;
			}
			else{
				UpdateMethod method = constants.getUpdateMethod();
				if(method.equals(UpdateMethod.NXCS) || method.equals(UpdateMethod.RXCS) || method.equals(UpdateMethod.NXCS2)){
					P = preRho + constants.getGamma() * valueFunctionEstimation(setM);
				}
				else{
					P = preRho + constants.getGamma() * PA.getPrediction(PA.getActionDeterministic());
				}
			}

			switch(constants.getUpdateMethod()){
			case NXCS:
			case NXCS2:
				setAMinusOne = updateSetNXCS(preState, preAct, P);
				break;
			case RXCS:
				if(!state.equals(preState))setAMinusOne = updateSetRXCS(preState, preAct, P);
				break;
			case XCSMU:
				setAMinusOne = updateSetXCSMU(setAMinusOne, P);
				break;
			case NORMAL:
				setAMinusOne = updateSet(setAMinusOne, P);
				break;
			}
			runGA(setAMinusOne, preState);
		}

		preRho = env.getReward(state, act);
		preAct = act;
		setAMinusOne = setA;
		preState = state;
	}

	/**
	 * Generates a set of classifiers that match the given state.
	 * Looks first for already generates ones in the population, but if
	 * the number of matches is less than thetaMNA, generates new classifiers
	 * with random actions and adds them to the match set.
	 * Reference: Page 7 'An Algorithmic Description of XCS'
	 * @param state The current state
	 * @return A set of classifiers that match the given state
	 */
	private Set<Classifier<A, C>> generateMatchSet(S state){
		setM = findMatchClassifiersInPopulation(state);
		while(coveredActions(setM).size() < constants.getThetaNma()){
			Classifier<A, C> cl = generateCoveringClassifier(setM, state);
			if(cl == null)break;
			addToPopulation(cl);
			deleteFromPopulation();
			setM.add(cl);
		}

		return setM;
	}

	/**
	 * Generates a classifier with the given state as the condition
	 * and a random action not covered by the given set of classifiers
	 * Reference: Page 8 'An Algorithmic Description of XCS'
	 * @param setM The current covering classifiers
	 * @param state The state to use as the condition for the new classifier
	 * @return The generated classifier
	 */
	private Classifier<A, C> generateCoveringClassifier(Set<Classifier<A, C>> setM, S state){
		Set<A> covered = coveredActions(setM);
		Set<A> uncovered = uncoveredActions(covered);

		if(uncovered.size() == 0)return null;
		A ra = findRandomElementInSet(uncovered);

		Classifier<A, C> cl = createNewClassifier(state, ra, "Covering");
		cl.setExperience(0.00);
		cl.setTimeStamp(timestamp);
		cl.setAverageSize(1.0);
		cl.setNum(1);

		return cl;
	}

	/**
	 * Insert the given classifier into the population, checking first to see if any
	 * classifier already in the population is more general. If a more general classifier
	 * is found with the same action, that classifiers num is incremented. Else the given classifer
	 * is added to the population.
	 * Reference: Page 13 'An Algorithmic Description of XCS'
	 * @param classifier The classifier to add
	 */
	private void insertInPopulation(Classifier<A, C> classifier){
		for(Classifier<A, C> cl : population){
			if(conditionConditionMatch(classifier, cl) && classifier.getAction().equals(cl.getAction())){
				cl.setNum(cl.getNum() + 1);
				return;
			}
		}

		addToPopulation(classifier);
	}

	/**
	 * Deletes a random classifier in the population, with probability of being deleted
	 * proportional to the fitness of that classifier.
	 * Reference: Page 14 'An Algorithmic Description of XCS'
	 */
	private void deleteFromPopulation(){
		int numSum = 0;
		double fitnessSum = 0.00;

		for(Classifier<A, C> classifier : population){
			numSum += classifier.getNum();
			fitnessSum += classifier.getFitness();
		}
		//If we have fewer than the max, no need to delete classifiers
		if(numSum < constants.getSP())return;

		double averageFitnessInPopulation = fitnessSum / numSum;
		double voteSum = 0;

		for(Classifier<A, C> classifier : population){
			voteSum += classifier.deletionVote(averageFitnessInPopulation);
		}

		double choicePoint = random.nextDouble() * voteSum;

		voteSum = 0.00;

		Classifier<A, C> toDel = null;

		for(Classifier<A, C> classifier : population){
			voteSum += classifier.deletionVote(averageFitnessInPopulation);
			if(voteSum > choicePoint){
				if(classifier.getNum() > 1){
					classifier.setNum(classifier.getNum() - 1);
				}
				else{
					toDel = classifier;
				}
				break;
			}
		}

		if(toDel != null)population.remove(toDel);
	}

	/**
	 * Generates an array of 'best guesses' as to the payoff of each action
	 * advocated for by a Classifier<A, C> in the given match set.
	 * Reference: Page 8 'An Algorithmic Description of XCS'
	 * @param setM The current match set
	 * @return The generated prediction array
	 */
	private PredictionArray<A> generatePredictionArray(Set<Classifier<A, C>> setM){
		UpdateMethod method = constants.getUpdateMethod();
		if(method.equals(UpdateMethod.NXCS) || method.equals(UpdateMethod.RXCS) || method.equals(UpdateMethod.NXCS2)){
			return PredictionArray.fromMatchSetTheta(actionSet, setM);
		}
		else{
			return PredictionArray.fromMatchSetPrediction(actionSet, setM);
		}
	}

	/**
	 * Selects an action to perform. Biased on pexp, choses an
	 * action using either pure exploitation (Best predicted payoff)
	 * or pure exploration (Random Action)
	 * Reference: Page 9 'An Algorithmic Description of XCS'
	 * @param predictionArray The array of predicted payoff values for each action
	 * @return An action chosen to execute
	 */
	private A selectActionFromDistribution(PredictionArray<A> PA){
		//Normalise
		PA.normalize();

		double choicePoint = Math.random();
		double sum = 0;

		for(A action : actionSet){
			sum += PA.getPrediction(action);
			if(sum > choicePoint)return action;
		}

		return actionSet[(int)(Math.random() * actionSet.length)];
	}

	private A selectBestAction(PredictionArray<A> PA){
		if(Math.random() > constants.getPexp()){
			return PA.getActionDeterministic();
		}
		else{
			return actionSet[(int)(Math.random() * actionSet.length)];
		}
	}

	/**
	 * Generates a set of all the classifier in setM that advocate for the given action.
	 * Reference: Page 9 'An Algorithmic Description of XCS'
	 * @param setM The current match set
	 * @param action The action to check for
	 * @return A Set containing all the classifiers from setM that have an action equal to the given one
	 */
	private Set<Classifier<A, C>> generateActionSet(Set<Classifier<A, C>> setM, A action){
		Set<Classifier<A, C>> matches = new HashSet<Classifier<A, C>>();
		for(Classifier<A, C> classifier : setM){
			if(classifier.getAction().equals(action)){
				matches.add(classifier);
			}
		}

		return matches;
	}

	/**
	 * Generates a prediction array based on the given match set and then normalises
	 * it so that the sum of the values in the array is 1
	 * @param setM The match set to generate a prediction array from
	 * @return The normalised prediction array
	 */
	private PredictionArray<A> generateNormalizedPredictionArray(Set<Classifier<A, C>> setM){
		return generatePredictionArray(setM).normalize();
	}

	/**
	 * Updates the given classifiers experience, prediction, prediction error and average size based on
	 * the given P and numSum
	 * @param classifier The classifier to update
	 * @param P The adjusted reward value
	 * @param numSum The number of micro-classifiers in the population
	 */
	private void updateClassifierParameters(Classifier<A, C> classifier, double P, double numSum){
		classifier.setExperience(classifier.getExperience() + 1);
		if(classifier.getExperience() < 1 / constants.getBeta()){
			//Update parameters by experience
			classifier.setPrediction(classifier.getPrediction() + (P - classifier.getPrediction()) / classifier.getExperience());
			classifier.setPredictionError(classifier.getPredictionError() + (Math.abs(P - classifier.getPrediction()) - classifier.getPredictionError()) / classifier.getExperience());
			classifier.setAverageSize(classifier.getAverageSize() + (numSum - classifier.getAverageSize()) / classifier.getExperience());
		}
		else{
			//Update parameters by beta
			classifier.setPrediction(classifier.getPrediction() + (P - classifier.getPrediction()) * constants.getBeta());
			classifier.setPredictionError(classifier.getPredictionError() + (Math.abs(P - classifier.getPrediction()) - classifier.getPredictionError()) * constants.getBeta());
			classifier.setAverageSize(classifier.getAverageSize() + (numSum - classifier.getAverageSize()) * constants.getBeta());
		}
	}

	/**
	 * Generates an array of improved predictions for each action, based
	 * on both classifier prediction and fitness.
	 * @param setM The match set to generate the array from
	 * @return A
	 */
	private double valueFunctionEstimation(Set<Classifier<A, C>> setM){
		PredictionArray<A> PA = generateNormalizedPredictionArray(setM);
		double ret = 0;
		for(int i = 0;i < actionSet.length;i ++){
			Set<Classifier<A, C>> setAA = generateActionSet(setM, actionSet[i]);
			if(setAA.size() > 0){
				double fitnessSum = 0;
				double predictionSum = 0;
				for(Classifier<A, C> classifier : setAA){
					fitnessSum += classifier.getFitness();
					predictionSum += classifier.getPrediction() * classifier.getFitness();
				}

				ret += PA.getPrediction(actionSet[i]) * predictionSum / fitnessSum;
			}
		}

		return ret;
	}

	private Set<Classifier<A, C>> updateSetXCSMU(Set<Classifier<A, C>> setA, double P){
		double mubar = Double.MAX_VALUE;
		double numSum = 0;
		for(Classifier<A, C> classifier : setA){
			if(classifier.getPredictionError() < mubar){
				mubar = classifier.getPredictionError();
			}

			numSum += classifier.getNum();
		}

		for(Classifier<A, C> classifier : setA){
			classifier.setExperience(classifier.getExperience() + 1);

			classifier.setMu(classifier.getMu() + 0.05 * (mubar - classifier.getMu()));

			if(classifier.getExperience() < 1 / constants.getBeta()){
				//Update parameters by experience
				classifier.setPrediction(classifier.getPrediction() + (P - classifier.getPrediction()) / classifier.getExperience());
				double error = Math.abs(P - classifier.getPrediction()) - classifier.getMu();
				if(error < 0){
					//error = constants.getE0();
				}
				classifier.setPredictionError(classifier.getPredictionError() + (error - classifier.getPredictionError()) / classifier.getExperience());
				classifier.setAverageSize(classifier.getAverageSize() + (numSum - classifier.getAverageSize()) / classifier.getExperience());
			}
			else{
				//Update parameters by beta
				classifier.setPrediction(classifier.getPrediction() + (P - classifier.getPrediction()) * constants.getBeta());
				double error = Math.abs(P - classifier.getPrediction()) - classifier.getMu();
				if(error < 0){
					//error = constants.getE0();
				}
				classifier.setPredictionError(classifier.getPredictionError() + (error - classifier.getPredictionError()) * constants.getBeta());
				classifier.setAverageSize(classifier.getAverageSize() + (numSum - classifier.getAverageSize()) * constants.getBeta());
			}

		}

		updateFitness(setA);

		if(constants.doActionSetSubsumption()){
			return actionSetSubsumption(setA);
		}
		else{
			return setA;
		}
	}

	private Set<Classifier<A, C>> updateSetRXCS(S preState, A act, double P){
		Set<Classifier<A, C>> setMPrev = generateMatchSet(preState);
		double deltaT = P - valueFunctionEstimation(setMPrev);
		double numSum = 0;
		Set<Classifier<A, C>> setAPrev = generateActionSet(setMPrev, act);
		for(Classifier<A, C> classifier : setAPrev){
			numSum += classifier.getNum();
		}

		for(Classifier<A, C> classifier : setAPrev){
			updateClassifierParameters(classifier, P, numSum);
		}

		updateFitness(setAPrev);

		double actProb = generateNormalizedPredictionArray(setMPrev).getPrediction(act);
		for(Classifier<A, C> classifier : setAPrev){
			classifier.setTheta(classifier.getTheta() + constants.getOmega() * deltaT * (actProb - actProb * actProb));
		}

		if(constants.doActionSetSubsumption()){
			return actionSetSubsumption(setAPrev);
		}
		else{
			return setAPrev;
		}
	}

	/**
	 * Updates the set according to the Natural XCS algorithm
	 * @param state The current state with which to update the set
	 * @param act The action to be performed in the current state
	 * @param P The adjusted reward for executing the given action
	 * @return The action set generated from this updating
	 */
	private Set<Classifier<A, C>> updateSetNXCS(S state, A act, double P){
		Set<Classifier<A, C>> setM = generateMatchSet(state);
		double deltaT = (P - valueFunctionEstimation(setM));

		Set<Classifier<A, C>> setA = generateActionSet(setM, preAct);

		//Update parameters
		double numSum = 0;
		for(Classifier<A, C> classifier : setA){
			numSum += classifier.getNum();
		}

		for(Classifier<A, C> classifier : setA){
			updateClassifierParameters(classifier, P, numSum);
		}

		updateFitness(setA);

		//Update Thetas of the match set
		PredictionArray<A> reward = generateNormalizedPredictionArray(setM);
		Classifier<A, C>[] setMArray = toArray(setM);


		double dot = 0;
		double[] stateFeatures = new double[setM.size()];
		for(int i = 0;i < stateFeatures.length;i ++){
			Classifier<A, C> classifier = setMArray[i];
			if(classifier.getAction().equals(act)){
				stateFeatures[i] = 1 - reward.getPrediction(classifier.getAction());
			}
			else{
				stateFeatures[i] = -reward.getPrediction(classifier.getAction());
			}
			dot += (classifier.getTheta() - classifier.getW()) * Math.pow(stateFeatures[i], 2);
		}


		for(int i = 0;i < stateFeatures.length;i ++){
			Classifier<A, C> classifier = setMArray[i];
			classifier.setW(classifier.getTheta());
			if(constants.getUpdateMethod().equals(XCSConstants.UpdateMethod.NXCS)){
				classifier.setTheta(classifier.getTheta() + constants.getOmega() * deltaT * stateFeatures[i]);
			}
			else{
				double mod = 0.1 * (classifier.getTheta() - classifier.getW()) + constants.getOmega() * deltaT * stateFeatures[i] - constants.getOmega() * dot;
				classifier.setTheta(classifier.getTheta() + mod);
			}
		}


		//Return either the subsumped action set or the standard action set
		if(constants.doActionSetSubsumption()){
			return actionSetSubsumption(generateActionSet(setM, act));
		}
		else{
			return generateActionSet(setM, act);
		}
	}

	/**
	 * Updates the classifier parameters for all the classifiers in the supplied action set and performs
	 * subsumption if required.
	 * Reference: Page 10 'An Algorithmic Description of XCS'
	 * @param setA The current action set
	 * @param P The predicted payoff for this timestep
	 * @return The updated version of setA
	 */
	private Set<Classifier<A, C>> updateSet(Set<Classifier<A, C>> setA, double P){
		int numSum = 0;
		for(Classifier<A, C> classifier : setA){
			numSum += classifier.getNum();
		}

		for(Classifier<A, C> classifier : setA){
			updateClassifierParameters(classifier, P, numSum);
		}

		updateFitness(setA);

		if(constants.doActionSetSubsumption()){
			setA = actionSetSubsumption(setA);
		}

		return setA;
	}

	/**
	 * Updates the fitness of all the classifiers in setA according to the sum of
	 * their errors.
	 * @param setA The action set to update fitness on
	 */
	private void updateFitness(Set<Classifier<A, C>> setA){
		double accuracySum = 0;
		double[] kappa = new double[setA.size()];

		Classifier<A, C>[] classifiers = toArray(setA);
		for(int i = 0;i < classifiers.length;i ++){
			Classifier<A, C> cl = classifiers[i];
			if(cl.getPredictionError() <= constants.getE0()){
				kappa[i] = 1;
			}
			else{
				kappa[i] = constants.getAlpha() * Math.pow(cl.getPredictionError() / constants.getE0(), -constants.getVi());
			}


			accuracySum += kappa[i] * cl.getNum();
		}

		for(int i = 0;i < classifiers.length;i ++){
			Classifier<A, C> cl = classifiers[i];
			cl.setFitness(cl.getFitness() + constants.getBeta() * (kappa[i] * cl.getNum() / accuracySum - cl.getFitness()));
		}
	}

	/**
	 * Runs the genetic algorithm (assuming enough time has passed) in order to make new
	 * classifiers based on the ones currently in the action set
	 * Reference: Page 11 'An Algorithmic Description of XCS'
	 * @param currentActionSet The current action set in this timestep
	 * @param state The current state from the environment
	 */
	@SuppressWarnings("unchecked")
	private void runGA(Set<Classifier<A, C>> currentActionSet, S state){
		double averageTimeStamp = 0.00;
		int numSum = 0;
		for(Classifier<A, C> classifier : currentActionSet){
			averageTimeStamp += classifier.getNum() * classifier.getTimeStamp();
			numSum += classifier.getNum();
		}

		averageTimeStamp /= numSum;

		//If not enough time has passed, don't run
		if(timestamp - averageTimeStamp <= constants.getThetaGA()){
			return;
		}

		for(Classifier<A, C> classifier : currentActionSet){
			classifier.setTimeStamp(timestamp);
		}

		Classifier<A, C> parent1 = selectOffspring(currentActionSet);
		Classifier<A, C> parent2 = selectOffspring(currentActionSet);

		if(parent1 == null || parent2 == null || parent1.equals(parent2)){
			//There are not enough parents in the set.
			return;
		}

		Classifier<A, C> child1 = parent1.clone();
		Classifier<A, C> child2 = parent2.clone();

		child1.setID(newClassifierID());
		child2.setID(newClassifierID());

		child1.setNum(1);
		child2.setNum(1);

		child1.setExperience(0.00);
		child2.setExperience(0.00);

		if(random.nextDouble() < constants.getChi()){
			child1.getCondition().crossover(child2.getCondition());

			//Reset childrens data
			child1.setPrediction((parent1.getPrediction() + parent2.getPrediction()) / 2);
			child1.setPredictionError(0.25 * (parent1.getPredictionError() + parent2.getPredictionError()) / 2);
			child1.setFitness(0.1 * (parent1.getFitness() + parent2.getFitness()) / 2);

			child2.setPrediction(child1.getPrediction());
			child2.setPredictionError(child1.getPredictionError());
			child2.setFitness(child1.getFitness());
		}

		Classifier<A, C>[] children = new Classifier[]{child1, child2};

		//Mutate and add both children
		for(Classifier<A, C> child : children){
			applyMutation(child, state);

			if(constants.doGASubsumption()){
				if(parent1.doesSubsume(child)){
					parent1.setNum(parent1.getNum() + 1);
				}
				else if(parent2.doesSubsume(child)){
					parent2.setNum(parent2.getNum() + 1);
				}
				else{
					insertInPopulation(child);
				}
			}
			else{
				insertInPopulation(child);
			}
			deleteFromPopulation();
		}

	}

	/**
	 * Chooses a random Classifier from setA with the intention
	 * that the Classifier will be used to generate a child Classifier.
	 *
	 * The probability of any one classifier being chosen is proportional to
	 * its fitness.
	 *
	 * Reference: Page 12 'An Algorithmic Description of XCS'
	 *
	 * @param setA The set of classifiers to choose from
	 * @return A randomly chosen classifier from the given set
	 */
	private Classifier<A, C> selectOffspring(Set<Classifier<A, C>> setA){
		double fitnessSum = 0.00;
		for(Classifier<A, C> classifier : setA){
			fitnessSum += classifier.getFitness();
		}

		//Roulette wheel selection to find a random classifier based on fitness
		double choicePoint = random.nextDouble() * fitnessSum;
		fitnessSum = 0.00;
		for(Classifier<A, C> classifier : setA){
			fitnessSum += classifier.getFitness();
			if(fitnessSum > choicePoint){
				return classifier;
			}
		}

		//Should never happen, as fitnessSum must be > choicePoint at this time
		return findRandomElementInSet(setA);
	}

	/**
	 * Mutates the given classifier according to the given state by flipping
	 * condition chars and potentially choosing a new action to perform
	 * Reference: Page 13 'An Algorithmic Description of XCS'
	 * @param cl The classifier to mutate
	 * @param state The current state by which to mutate the given classifier
	 */
	private void applyMutation(Classifier<A, C> cl, S state){
		cl.getCondition().mutate(state, constants.getMu());

		if(random.nextDouble() < constants.getMu()){
			Set<A> covered = new HashSet<A>();
			covered.add(cl.getAction());
			cl.setAction(findRandomElementInSet(uncoveredActions(covered)));
		}
	}

	/**
	 * Performs an action set subsumption, subsuming the action set into the most general
	 * of the classifiers.
	 * Reference: Page 15 'An Algorithmic Description of XCS'
	 * @param setAA The action set to subsume
	 * @return The updated action set
	 */
	private Set<Classifier<A, C>> actionSetSubsumption(Set<Classifier<A, C>> setAA){
		if(setAA.size() <= 1)return setAA;

		Classifier<A, C>[] data = toArray(setA);

		if(constants.useModifiedActionSetSubsumption()){
			//Sort the set by the number of wildcards.
			Arrays.sort(data, new Comparator<Classifier<A, C>>(){
				@Override
				public int compare(Classifier<A, C> c1, Classifier<A, C> c2){
					int c1WildcardCount = c1.getNumWildcards();
					int c2WildcardCount = c2.getNumWildcards();

					if(c1WildcardCount == c2WildcardCount)return 0;
					else if(c1WildcardCount > c2WildcardCount)return -1;
					else return 1;
				}
			});
		}

		Classifier<A, C> cl = null;

		//Find the most general classifier that is eligible to subsume
		for(Classifier<A, C> c : data){
			if(c.couldSubsume()){
				if(cl == null || c.isMoreGeneral(cl)){
					cl = c;
				}
			}
		}

		//Subsume classifiers into cl
		if(cl != null){
			for(Classifier<A, C> c : data){
				if(cl.isMoreGeneral(c)){
					cl.setNum(cl.getNum() + c.getNum());
					deleteFromPopulation(c.getID());
					setAA.remove(c);
				}
			}
		}

		return setAA;
	}

	/**
	 * ===================
	 * Facilitator Methods
	 * ===================
	 */

	/**
	 * Adds the given classifier to the population set,
	 * provided no classifier with the same ID already exists.
	 * @param classifier The classifier to add
	 */
	private void addToPopulation(Classifier<A, C> classifier){
		long id = classifier.getID();

		//Check that no other classifier in the population shares the ID
		//of the new one.
		for(Classifier<A, C> c : population){
			if(c.getID() == id)return;
		}

		population.add(classifier);
	}

	/**
	 * Deletes all classifiers with the given id from
	 * the population set.
	 * @param id The ID that represents classifiers to delete
	 */
	private void deleteFromPopulation(long id){
		Iterator<Classifier<A, C>> popIter = population.iterator();
		for(Classifier<A, C> classifier = popIter.next();popIter.hasNext();classifier = popIter.next()){
			if(classifier.getID() == id)popIter.remove();
		}
	}
	/**
	 * Increments the stored classifier counter
	 * and returns it
	 * @return The new classifier id (Should be unique);
	 */
	private long newClassifierID(){
		return ++initialClassifierID;
	}


	/**
	 * Checks whether the given condition is a match for the given state,
	 * i.e each position in the condition is either equal to the same position
	 * in the state, or is the wildcard char.
	 * @param state The state to match against
	 * @param condition The condition to match against
	 * @return Whether the given condition is a match to the given state
	 */
	private boolean stateConditionMatch(S state, C condition){
		return condition.matchesState(state);
	}

	/**
	 * Checks whether the condition of the given classifier is a match to the
	 * given state. i.e each position in the condition is either equal to the same position
	 * in the state, or is the wildcard char.
	 *
	 * @param state The state to check against
	 * @param classifier The classifier whose condition to match against
	 * @return Whether the given classifiers condition is a match for the given state
	 */
	private boolean stateClassifierMatch(S state, Classifier<A, C> classifier){
		return stateConditionMatch(state, classifier.getCondition());
	}

	/**
	 * Returns whether the two given conditions equal each other
	 * @param classifier1 The first classifier to check against
	 * @param classifier2 The second classifier to check against
	 * @return Whether the two given classifiers equal each other
	 */
	private boolean conditionConditionMatch(Classifier<A, C> classifier1, Classifier<A, C> classifier2){
		return classifier1.getCondition().equals(classifier2.getCondition());
	}

	/**
	 * Returns a Set of all the classifiers in the population that has a condition
	 * that matches the given state
	 * @param state The state to check against
	 * @return
	 */
	private Set<Classifier<A, C>> findMatchClassifiersInPopulation(S state){
		Set<Classifier<A, C>> matches = new HashSet<Classifier<A, C>>();
		for(Classifier<A, C> classifier : population){
			//If the classifier matches, add it to the set
			if(stateClassifierMatch(state, classifier)){
				matches.add(classifier);
			}
		}

		return matches;
	}

	/**
	 * Creates a new classifier with the given condition and action,
	 * and a unique ID.
	 * @param condition The condition for this new classifier
	 * @param action The action this classifier advocates
	 * @return A new classifier with the specified details
	 */
	private Classifier<A, C> createNewClassifier(S state, A action, String source){
		return new Classifier<A, C>(constants, source, newClassifierID(), state.makeCondition(constants.getStateDim(), constants.getSpecificityProbability()), action);
	}

	/**
	 * Generates a Set of Actions that are covered by the classifiers in the set given.
	 * @param classifiers The classifiers to examine
	 * @return A Set containing all the actions advocated for by the given classifiers
	 */
	private Set<A> coveredActions(Set<Classifier<A, C>> classifiers){
		Set<A> actions = new HashSet<A>();
		for(Classifier<A, C> classifier : classifiers){
			actions.add(classifier.getAction());
		}

		return actions;
	}

	/**
	 * Returns a Set of Actions in the action set that are not
	 * in actions. Essentially the symmetric difference of the action set
	 * and the given one.
	 * @param actions The set of covered actions
	 * @return A set of actions not included in the given set
	 */
	private Set<A> uncoveredActions(Set<A> actions){
		Set<A> uncovered = new HashSet<A>();
		for(A action : actionSet){
			if(!actions.contains(action)){
				uncovered.add(action);
			}
		}

		return uncovered;
	}

	/**
	 * Finds a random element in the given set and returns it
	 * @param data The Set to look through
	 * @return A random element in data or null if there are no elements in the set
	 */
	private <E> E findRandomElementInSet(Set<E> data){
		if(data.size() == 0)return null;
		int randomItem = (int)(random.nextDouble() * data.size());
		int count = 0;
		for(E i : data){
			if(count ++ == randomItem){
				return i;
			}
		}

		return data.iterator().next();
	}

	/**
	 * Converts the given set of classifiers into an array so that it can be indexed in a constant way
	 * @param data The set to convert into an array
	 * @return An array of the given set of classifiers
	 */
	@SuppressWarnings("unchecked")
	public Classifier<A, C>[] toArray(Set<Classifier<A, C>> data){
		Classifier<A, C>[] target = new Classifier[data.size()];
		target = data.toArray(target);
		return target;
	}
}
