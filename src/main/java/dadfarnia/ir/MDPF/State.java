
package dadfarnia.ir.MDPF;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * represent state in an MDPF
 */
public class State {
    /**
     * Name of State.
     */
    private String name;

    /**
     * Every Transition is labeled by an action. Transitions are grouped by actions.
     * <Action, <destination, Transition[]>>
     */
    HashMap <String, HashMap<String,ArrayList<Transition>>> transitions; //<Action, <destination, Transition[]>>
    String[] states_names;

    /**
     * Constructor
      * @param name, states_names
     */
    State(String name, String[] states_names){
        this.name = name;
        this.states_names = states_names;
        transitions = new HashMap<String, HashMap<String,ArrayList<Transition>>>();
    }

    /**
     * Given an action and a transition, add it to State.
     * @param action
     * @param t transition
     * @param destination
     */
    public void addTransition(String action, Transition t, String destination){
        if(transitions.containsKey(action)){
            transitions.get(action).get(destination).add(t);
        }
        else {
            HashMap<String, ArrayList<Transition>> outGoingTrans = new HashMap<String, ArrayList<Transition>>();
            for(int i=0; i<states_names.length; i++) {
                ArrayList<Transition> transitionArray = new ArrayList<Transition>();
                if(states_names[i].equals(destination))
                    transitionArray.add(t);
                outGoingTrans.put(states_names[i], transitionArray);
            }
            transitions.put(action, outGoingTrans);
        }
    }

    /**
     * Check State has label or not
     * @param label String
     * @return boolean
     */
    public boolean hasAction(String label){
        return transitions.containsKey(label);
    }
    public String getName(){
        return name;
    }


    public void print(){
        for(String action : transitions.keySet()){
            System.out.println(action);
            print(transitions.get(action));
            System.out.println("-----------");
        }
    }

    private void print(HashMap<String, ArrayList<Transition>> outGoingTrans){
        for(String destination: states_names) {
            System.out.println("  " + destination);
            for(int i=0; i<outGoingTrans.get(destination).size(); i++)
                System.out.println("\t" + outGoingTrans.get(destination).get(i));

        }
    }

    //HashMap <String, HashMap<String,ArrayList<Transition>>> transitions; //<Action, <destination, Transition[]>>
    public ArrayList<Transition> multiply(ResultSet input){
        ArrayList<ArrayList<Transition>> results = new ArrayList<ArrayList<Transition>>();
        for(String action : transitions.keySet()){
            ArrayList<Transition> partialResult = new ArrayList<Transition>();
            for(String state : states_names){
                ArrayList<Transition> arg1  = transitions.get(action).get(state);
                ArrayList<Transition> arg2  = input.get(state);
                partialResult = Transition.sum(partialResult, Transition.product(arg1, arg2, input.bddService));
            }
            results.add(partialResult);
            if(results.size() > 1)
                minimize(results);
        }
        return results.get(0);
    }

    public static void minimize(ArrayList<ArrayList<Transition>> input){
        if(input.size() != 2)
            return;

        ArrayList<Transition> transitions1 = input.get(0);
        ArrayList<Transition> transitions2 = input.get(1);
        for(int i=0; i<transitions1.size(); i++){
            for(int j=0; j<transitions2.size(); j++){
                Transition t1 = transitions1.get(i);
                Transition t2 = transitions2.get(j);
                if(t2.isWeaker(t1)) {
                    transitions2.remove(t2);
                    j--;
                }
                else if(t1.isWeaker(t2)) {
                    transitions1.remove(t1);
                    i--;
                }
            }
        }

        transitions1.addAll(transitions2);
        input.remove(transitions2);
    }
}
