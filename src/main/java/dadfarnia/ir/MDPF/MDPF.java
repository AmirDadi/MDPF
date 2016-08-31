package dadfarnia.ir.MDPF;
import com.sun.org.apache.regexp.internal.RE;
import net.sf.javabdd.BDD;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Markov Decision Process Family(MDPF) defined over a feature model F, is a tuple (States, initial state, actions, Atomic Propositions, labels)
 */
public class MDPF {
    /**
     * ArrayList of States,
     * @see State
     */
    private ArrayList<State> states;

    /**
     * A Service that provides function related to bdds and proposition logic
     * @see BDDService
     */
    private BDDService bddService;

    /**
     * Constructor that creates an MDPF from a file,
     * <b> Text file format: </b> <br>
     *      first line contains name of propositions(features) over MDPF,
     *      second line contains name of states, seperated by space. First state is the initial state. <br>
     *      After states, each line represent one action, which in this format, <br>
     *              "startState destenitionState label probability propositionFormula" <br>
     * <b> json file format </b>: <br>
     *  {
     *      "Propositions" : <propistions seperated by space>,
     *      "States" : <state names seperated by space>,
     *      "Transitions" : [
     *          {
     *              "source": <source>,
     *              "Destination: <destination>,
     *              "Label": <Label>,
     *              "Probability": <probability>,
     *              "ApplicationCondition": <Application condition>
     *          },
     *          ...
     *      ]
     * @param fileName
     */
    public MDPF(String fileName){
        if(fileName.endsWith("json"))
            readJsonFile(fileName);
        else
            readFile(fileName);
    }

    /**
     * @return arraylist of states
     */

    public ArrayList<State> getStates(){
       return states;
    }

    /**
     * Print MDPF by states, Every State Represent a row of matrix representation
     */
    public void print(){
        for(State state : states){
            state.print();
        }
    }

    /**
     * Given an string of pctl formula, calculate result based on mdpf
     * @param input
     * @return ResultSet that shows action/probability per State
     */
    public ResultSet sat(String input){
        ArrayList<String> variables = new ArrayList<String>(Arrays.asList(input.split("\\&|\\~|\\$|\\@|U|[0-9]+|\\|")));
        while(variables.remove(""));
        StringBuilder formula = new StringBuilder(input);
        return recParseFormula(formula, variables);
    }

    private void readJsonFile(String fileName){
        try {
            FileReader reader = new FileReader(fileName);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            String variables = (String) jsonObject.get("Propositions");
            String state_names = (String) jsonObject.get("States");
            states = readStates(state_names);
            String[] variables_array = variables.split("\\s+");
            bddService = new BDDService(variables_array);
            readTransitionJson(jsonObject);
        }catch(FileNotFoundException e){
            System.out.println("File " + fileName + " Not Found");
        }catch(ParseException p){
            System.out.println("File Format Problem");
            p.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void readTransitionJson(JSONObject jsonObject){
        JSONArray transitions = (JSONArray) jsonObject.get("Transitions");
        Iterator i = transitions.iterator();
        while(i.hasNext()){
            JSONObject currentTransition = (JSONObject) i.next();
            String source = (String) currentTransition.get("Source");
            String dest = (String) currentTransition.get("Destination");
            String action = (String) currentTransition.get("Label");
            Double probability = (Double) currentTransition.get("Probability");
            String applicationCondition = (String) currentTransition.get("ApplicationCondition");
            BDD appCond = bddService.expToBDD(new StringBuilder(applicationCondition));
            Transition transition = new Transition(appCond, probability, bddService);
            for(State state : states){
                if(state.getName().equals(source))
                    state.addTransition(action, transition, dest);
            }
        }
    }
    private void readFile(String fileName){
        File file = new File(fileName);
        try{
            Scanner scanner = new Scanner(file);
            String variables_line = scanner.nextLine();
            String states_name = scanner.nextLine();
            states = readStates(states_name);

            String[] variables = variables_line.split("\\s+");
            bddService = new BDDService(variables);
            readTransition(scanner);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }

    private ArrayList<State> readStates(String line){
        String[] states_names = line.split("\\s+");
        ArrayList <State> result = new ArrayList<State>();
        for(int i=0; i<states_names.length; i++){
            State state = new State(states_names[i], states_names);
            result.add(state);
        }
        return result;
    }
    private ArrayList<String> getStatesContainingAction(String label){
        ArrayList<String> result = new ArrayList<String>();
        for(State state: states){
            if(state.hasAction(label)){
                result.add(state.getName());
            }
        }
        return result;
    }

    private ResultSet recParseFormula(StringBuilder formula, ArrayList<String> variables) {
        if (formula.charAt(0) == '$') {
            formula.delete(0, variables.get(0).length()+1);
            ArrayList<String> statesSatisfied = getStatesContainingAction(variables.remove(0));
            ResultSet result = new ResultSet(states, bddService);
            for(String state: statesSatisfied)
                result.setOne(state, 1);
            return result;
        }
        else if(formula.charAt(0) == '~'){
            formula.deleteCharAt(0);
            ResultSet arg1 = recParseFormula(formula, variables);
            return arg1.not();
        }
        else if(formula.charAt(0) == '&') {
            formula.deleteCharAt(0);
            ResultSet arg1 = recParseFormula(formula, variables);
            ResultSet arg2 = recParseFormula(formula, variables);
            return arg1.and(arg2);
        }
        else if(formula.charAt(0) == '|') {
            formula.deleteCharAt(0);
            ResultSet arg1 = recParseFormula(formula, variables);
            ResultSet arg2 = recParseFormula(formula, variables);
            ResultSet first = arg1.not();
            ResultSet second = arg2.not();
            return first.and(second).not();
        }
        else if(formula.charAt(0) == '@'){ // Next
            formula.deleteCharAt(0);
            ResultSet arg1 = recParseFormula(formula, variables);
            arg1 = multiply(arg1);
            return arg1.removeZeros();
        }
        else if(formula.charAt(0) == 'U'){ // Bounded Until
            int count = 0 ;
            while(++count<formula.length())
                if(formula.charAt(count) < '0' || formula.charAt(count) > '9') {
                    System.out.println("#" + formula.charAt(count) + " " + count);
                    break;
                }
            int bound = Integer.parseInt(formula.substring(1,count));
            formula.delete(0, count);
            ResultSet arg1 = recParseFormula(formula, variables);
            ResultSet arg2 = recParseFormula(formula, variables);

            System.out.println("Argument 1 Satisfication Set");
            arg1.print();
            System.out.println("Argument 2 Satisfication Set");
            arg2.print();
            return arg1;
        }
        else
            return null;
    }

    private void readTransition(Scanner scanner){
        while(scanner.hasNext()){
            String transitionExp = scanner.nextLine();
            Scanner lineScan = new Scanner(transitionExp);
            String source = lineScan.next();
            String dest = lineScan.next();
            String action = lineScan.next();
            double probability = lineScan.nextDouble();
            String rest = lineScan.nextLine();
            StringBuilder sb = new StringBuilder(rest);
            BDD appCond = bddService.expToBDD(sb);
            Transition transition = new Transition(appCond, probability, bddService);
            for(State state : states){
                if(state.getName().equals(source))
                    state.addTransition(action, transition, dest);
            }
        }
    }

    /**
     * Given a ResultSet, multiply this MDPF to ResultSet (According to multiplication defined in Article)
     * @param sat
     * @return
     */
    private ResultSet multiply(ResultSet sat){
        ResultSet result = new ResultSet(states, bddService);
        for(State state : states){
            ArrayList<Transition> partial_result = state.multiply(sat);
            result.set(state, partial_result);
        }
        return result;
    }
}
