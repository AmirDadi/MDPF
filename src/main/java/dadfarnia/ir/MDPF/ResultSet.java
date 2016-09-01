package dadfarnia.ir.MDPF;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The result of applying a condition (in the form of PCTL formula) to an MDPF represent in a ResultSet
 * Contains Hashmap of state to valid transitions, as the columnar matrix represent in article.
 */
public class ResultSet {
    HashMap<String, ArrayList<Transition>> results;
    ArrayList<State> states;
    BDDService bddService;

    /**
     * Constructor of Resultset
     * @param states states of the mdpf that the result set is defined on.
     * @param bddService injected bddservice from mdpf
     */
    public ResultSet(ArrayList<State> states, BDDService bddService){
        results = new HashMap<String, ArrayList<Transition>>();
        this.states = states;
        this.bddService = bddService;
        for(State state: states){
            ArrayList<Transition> transition = new ArrayList<Transition>();
            results.put(state.getName(), transition);
        }
    }

    /**
     * Add True/value element (transition) to state named key, value is the probability
     * @param key the state name that True/value pair element will add
     * @param value the probability that should add
     */
    public void setOne(String key, int value){
        if(value == 1) {
            results.get(key).add(new Transition(bddService.getOne(), 1, bddService));
        }
    }

    /**
     * Set a row of columnar matrix result.
     * @param key state name
     * @param transition arraylist of transitions (arrylist proposition/probability pairs)
     */
    public void set(State key, ArrayList<Transition> transition){
        results.put(key.getName(), transition);
    }

    /**
     *
     * @return size of the results, should be the same as number of states
     */
    public int size(){
        return results.size();
    }

    /**
     * Check equality of Two ResultSet
     * @param that Resultset
     * @return boolean true in equality and false otherwise.
     */
    public boolean equals(ResultSet that){
        if(that.size() != this.size())
            return false;
        List keys = new ArrayList(results.keySet());
        for(int i=0; i<keys.size(); i++){
            if(! this.results.get(keys.get(i)).equals(that.results.get(keys.get(i)))){
               return false;
            }
        }
        return true;
    }

    /**
     * Sat(~T) = {(s, ~w)|(s, w) E Sat(T)}
     *           U { (s,true) | s E Dom ( Sat (T) ) }
     * @return Satisficaiton Result (ResultSet)
     */
    public ResultSet not(){
        for(State state: states){
            String currentStateName = state.getName();
            if(results.get(currentStateName).size() == 0)
                results.get(currentStateName).add(new Transition(bddService.getOne(), 1, bddService));
            else
                results.get(currentStateName).clear();

        }
        return this;
    }
    /**
     * Sat(T1 and T2) = {(s, w) E Sat(T1)| exists one (s, Y ) E Sat(T2) . w <= y}
     *                  U  {(s, w) E Sat(T2)| exists one (s, Y ) E Sat(T1) . w <= y}
     * @param b Resultset
     * @return Resultset
     */
    public ResultSet and(ResultSet b){
        for(State state: states){
            if(! (results.get(state.getName()).size() == 1 && b.results.get(state.getName()).size() == 1) )
                results.get(state.getName()).clear();
        }
        return this;
    }

    /**
     * print the resultset
     */
    public void print(){
        for (State state: states) {
            System.out.println(state.getName());
            for(Transition transition : results.get(state.getName())){
                System.out.println("\t" + transition);
            }
        }
    }
    public boolean contains(String state){
        ArrayList<String> keys = new ArrayList(results.keySet());
        for (String key : keys) {
            if(key.equals(state))
                return true;
        }
        return false;
    }

    public ArrayList<Transition> get(String state){
        return results.get(state);
    }

    /**
     * return states and transitions that the input condition satisfies in string format.
     * <i> e.g. { (u/0.0), (u and (not b)/0.8) } </i>
     * @param condition any string with ">" or "=" or "<"
     * @param p double
     * @return string of result
     */
    public String getProbability(String condition, double p){

        if(p>1 || p<0) {
            System.out.println("Probability Should be between 0 and 1!");
            return "";
        }
        String result = "{ ";
        for(String name : results.keySet()){
            ArrayList<Transition> currentTransition = results.get(name);
            if(currentTransition.size() > 0){
                for(Transition t : currentTransition){
                    if(condition.contains("<") && t.getProbability() < p)
                        result += "(" + name + "," + t  + ") ";
                    if(condition.contains(">") && t.getProbability() > p)
                        result += "(" + name + "," + t  + ") ";
                    if(condition.contains("=") && t.getProbability() == p)
                        result += "(" + name + "," + t  + ") ";
                }
            }
        }
        result += "}";
        return result;
    }

    /**
     * Removes all transitions (prop/probability pairs) in the states that probability in all pairs are zero.
     * e.g if state contains state with (a/0.0 , b&c/0.0), these two elements would remove because all have zero probability
     * @return this
     */
    public ResultSet removeZeros(){
        for(String name: results.keySet()){
            ArrayList<Transition> currentTransitions = results.get(name);
            if(Transition.areAllZero(currentTransitions))
                results.get(name).clear();
        }
        return this;
    }
}
