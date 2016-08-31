package dadfarnia.ir.MDPF;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The result of applying a condition to an MDPF represent in a ResultSet
 * Contains Hashmap of state to valid transitions
 */
public class ResultSet {
    HashMap<String, ArrayList<Transition>> results;
    ArrayList<State> states;
    BDDService bddService;

    public ResultSet(ArrayList<State> states, BDDService bddService){
        results = new HashMap<String, ArrayList<Transition>>();
        this.states = states;
        this.bddService = bddService;
        for(int i=0; i<states.size(); i++){
            ArrayList<Transition> transition = new ArrayList<Transition>();
            results.put(states.get(i).getName(), transition);
        }
    }
    public void setOne(String key, int value){
        if(value == 1) {
            results.get(key).add(new Transition(bddService.getOne(), 1, bddService));
        }
    }
    public void set(State key, ArrayList<Transition> transition){
        results.put(key.getName(), transition);
    }
    public int size(){
        return results.size();
    }

    /**
     * Check equality of Two ResultSet
     * @param that
     * @return
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
        for(int i=0; i<states.size(); i++){
            String currentStateName = states.get(i).getName();
            if(results.get(currentStateName).size() == 0)
                results.get(currentStateName).add(new Transition(bddService.getOne(), 1, bddService));
            else
                results.get(currentStateName).clear();

        }
        return this;
    }
    /**
     * Sat(T1 and T2) = {(s, w) E Sat(T1)| ∃(s, Y ) E Sat(T2) . w <= y}
     *                  U  {(s, w) E Sat(T2)| ∃(s, Y ) E Sat(T1) . w <= y}
     * @param b
     * @return
     */
    public ResultSet and(ResultSet b){
        for(State state: states){
            if(! (results.get(state.getName()).size() == 1 && b.results.get(state.getName()).size() == 1) )
                results.get(state.getName()).clear();
        }
        return this;
    }
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
     * print states and transitions that the input condition satisfies
     *
     * @param condition any string with ">" or "=" or "<"
     * @param p double
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

}
