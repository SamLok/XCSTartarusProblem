package xcs;


/**
 * An implementation of a Classifier for use an an XCS.
 * Has fields for storing ideas of predicted payoff and estimated error in said payoff
 * as well as methods of determining eligibility to subsume and calculating deletion votes etc.
 *
 * @param <A> The type of action that this Classifier advocates for
 * @param <C> The type of Condiiton this classifier has
 */
public class Classifier<A, C extends Condition<?, C>> implements Cloneable{
	/* The condition for the classifier */
	private C condition;

	/* The action this classifier advocates */
	private A action;

	/* The unique ID of this classifier */
	private long id;

	/* The predicted payoff of running this classifier */
	private double p;

	/* The statistical error in the predicted payoff of this classifier */
	private double e;

	/* The fitness of this classifier */
	private double f;

	/* The experience of this classifier */
	private double exp;

	/* The time stamp since the last occurrence of a GA in an action to which a classifier belonged */
	private int ts;

	/* The average size of the action sets a classifier has belonged to */
	private double as;

	/* The number of micro-classifiers that a macro-classifier represents */
	private int num;

	/* The policy parameter for this classifier */
	private double theta;

	/* The prediction error due to uncertainties in the environment */
	private double mu;

	/* The previous theta value */
	private double w;

	private XCSConstants constants;

	private String source;

	public Classifier(XCSConstants constants, String source, long id, C condition, A action){
		this.constants = constants;
		this.id = id;
		this.condition = condition;
		this.action = action;
		this.source = source;

		//Initialise the default values
		p = constants.getPI();
		e = constants.getEI();
		f = constants.getFI();
		theta = constants.getThetaI();
		ts = 0;
		exp = 0;
		as = 0.00;
		num = 1;
		mu = 0;
	}

	/* =============
	 *    Setters
	 * =============
	 */

	public void setID(long id){
		this.id = id;
	}

	public void setCondition(C condition){
		this.condition = condition;
	}

	public void setAction(A action){
		this.action = action;
	}

	public void setPrediction(double p){
		this.p = p;
	}

	public void setPredictionError(double e){
		this.e = e;
	}

	public void setFitness(double f){
		this.f = f;
	}

	public void setExperience(double exp){
		this.exp = exp;
	}

	public void setTimeStamp(int ts){
		this.ts = ts;
	}

	public void setAverageSize(double as){
		this.as = as;
	}

	public void setNum(int nNum){
		num = nNum;
	}

	public void setTheta(double theta){
		this.theta = theta;
	}

	public void setMu(double mu){
		this.mu = mu;
	}

	public void setW(double w){
		this.w = w;
	}

	/* ===========
	 *   Getters
	 * ===========
	 */

	public long getID(){
		return id;
	}

	public C getCondition(){
		return condition;
	}

	public A getAction(){
		return action;
	}

	public double getPrediction(){
		return p;
	}

	public double getPredictionError(){
		return e;
	}

	public double getFitness(){
		return f;
	}

	public double getExperience(){
		return exp;
	}

	public int getTimeStamp(){
		return ts;
	}

	public double getAverageSize(){
		return as;
	}

	public int getNum(){
		return num;
	}

	public double getTheta(){
		return theta;
	}

	public double getW(){
		return w;
	}

	public double getMu(){
		return mu;
	}

	/**
	 * Calculates the number of wildcards in this Classifiers condition
	 * @return The number of wildcard characters in this condition
	 */
	public int getNumWildcards(){
		return condition.wildcardCount();
	}

	/**
	 * Clones this classifier and returns an exact copy
	 * with the same condition, action and prediction values etc
	 *
	 * @return The clone of this Classifier
	 */
	@Override
	public Classifier<A, C> clone(){
		Classifier<A, C> classifier = new Classifier<A, C>(constants, "Clone", id, condition.clone(), action);
		classifier.setPrediction(getPrediction());
		classifier.setPredictionError(getPredictionError());
		classifier.setFitness(getFitness());
		classifier.setExperience(getExperience());
		classifier.setTimeStamp(getTimeStamp());
		classifier.setAverageSize(getAverageSize());
		classifier.setNum(getNum());

		return classifier;
	}

	/*=======================
	 *    Utility Methods
	 *=======================
	 */

	/**
	 * Calculates the deletion vote contribution of this Classifier.
	 * This is used to determine which Classifier is deleted from the population,
	 * and the probability of this Classifier being deleted is proportional
	 * to its deletionVote.
	 * @param averageFitnessInPopulation The average level of fitness across all
	 * 			classifiers in the population
	 * @return This classifiers deletion vote
	 */
	public double deletionVote(double averageFitnessInPopulation){
		double vote = as * num;
		double averageFitness = getFitness() / getNum();
		if(getExperience() > constants.getThetaDel() && averageFitness < constants.getDelta() * averageFitnessInPopulation){
			vote *= (averageFitnessInPopulation / averageFitness);
		}

		return vote;
	}

	/**
	 * Checks whether this classifier *does* subsume the given classifier,
	 * that is, that this is more general and can subsume things and also that
	 * the two actions are the same.
	 *
	 * Reference: Page 15 'An Algorithmic Description of XCS'
	 * @param specific The Classifier to check whether this classifier can subsume
	 * @return Whether or not this Classifier does subsume the given one
	 */
	public boolean doesSubsume(Classifier<A, C> specific){
		A aG = getAction();
		A aS = specific.getAction();

		return (aG.equals(aS) && couldSubsume() && isMoreGeneral(specific));
	}

	/**
	 *Checks whether this classifier is eligible to subsume other classifiers.
	 *There are two conditions -> The experience is greater than thetaSub and the possible
	 *prediction error < e0.
	 * Reference: Page 15 'An Algorithmic Description of XCS'
	 * @return  Whether this classifier is eligible to subsume other classifiers
	 */
	public boolean couldSubsume(){
		return (getExperience() > constants.getThetaSub()) && (getPredictionError() < constants.getE0());
	}

	/**
	 * Checks whether this classifier is more general than the given one.
	 * More general = The same condition, but with more wildcards
	 * Reference: Page 16 'An Algorithmic Description of XCS'
	 * @param specific The classifier to check whether this is more general than it
	 * @return True if this classifier is more general than `specific`, false otherwise
	 */
	public boolean isMoreGeneral(Classifier<A, C> specific){
		return getCondition().isMoreGeneral(specific.getCondition());
	}

	@Override
	public String toString(){
		return String.format("ID: %d Condition: %s Action: %s Prediction: %3.2f Error: %3.2f Fitness: %3.2f Num: %d Can Subsume? %b Experience: %3.2f Theta: %3.2f Source: %s", id, condition, action.toString(), p, e, f, num, couldSubsume(), exp, theta, source);
	}

	@Override
	public int hashCode(){
		return (int) getID();
	}

	@Override
	public boolean equals(Object o){
		Classifier<?, ?> c = (Classifier<?, ?>)o;
		return getID() != c.getID();
	}
}
