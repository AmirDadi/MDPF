package dadfarnia.ir.MDPF;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import java.util.ArrayList;
import java.util.HashMap;

public class BDDService {

    private BDDFactory bddFactory;
    public ArrayList<Pair<String, BDD>> propositions;
    public static char[] specialChars = {'$', '&','|', '~', '@'};

    public BDDService(String[] variables ){
        int numberOfProp = variables.length;
        bddFactory= BDDFactory.init(numberOfProp, 100);
        bddFactory.setVarNum(numberOfProp);
        BDD[] bdds  = new BDD[numberOfProp];
        for(int i=0; i<numberOfProp; i++)
            bdds[i] = bddFactory.ithVar(i);
        propositions = new ArrayList<Pair<String, BDD>>();

        for (int i = 0; i < variables.length; i++) {
            if (!variables[i].equals(""))
                if (!isValidName(variables[i]))
                    return; //TODO throw exception
            propositions.add(new Pair<String, BDD>(variables[i], bdds[i]));
        }
    }

    public BDD expToBDD(StringBuilder exp){
        for(int i=0; i<exp.length(); i++){
            char op = exp.charAt(0);
            String tmp = exp.substring(1);
            exp.delete(0, exp.length());
            exp.append(tmp);
            if(op == '$')
                return propositions.get(Utils.findInArrayOfPairs(propositions,getName(exp))).getElement1();
            else if(op == '&'){
                BDD firstArg = expToBDD(exp);
                BDD secondArg = expToBDD(exp);
                return firstArg.apply(secondArg, BDDFactory.and);
            }
            else if(op == '~'){
                return expToBDD(exp).not();
            }
            else if (op == '|'){
                BDD firstArg = expToBDD(exp);
                BDD secondArg = expToBDD(exp);
                return firstArg.apply(secondArg, BDDFactory.or);
            }
        }
        return null;
    }

    public String toString(BDD b){
        if(b.isOne())
            return "T";
        if(b.isZero())
            return "F";
        BDD low = b.low();
        BDD high = b.high();
        String label = propositions.get(b.var()).getElement0();

        if(high.isOne() && low.isZero())
            return  label;
        else if (high.isZero() && low.isOne())
            return "(not " + label + ")";

        if (high.isOne())
            return label + " or " + toString(low) ;
        else if (low.isOne())
            return "(not " + label + ") or " + toString(high)  ;
        else if(low.isZero())
            return label + " and " +  toString(high) ;
        else if(high.isZero())
            return  "(not " + label + ") and " + toString(low) ;
        else
            return "(" + label + " and " + toString(high) +") or (" + "(not " + label +") and "+ toString(low) + ")";
    }

    private static String getName(StringBuilder number){
        int i;
        for(i=1; i<number.length(); i++){
            if(isSpecialChar(number.charAt(i)))
                break;
        }

        String name = number.substring(0,i);
        String tmp = number.substring(i);
        number.delete(0, number.length());
        number.append(tmp);
        return name;
    }


    public static boolean isSpecialChar(char in){
        for(char c : specialChars){
            if(c == in)
                return true;
        }
        return false;
    }
    public static boolean isValidName(String in){
        for(char c : specialChars){
            for(int i=0; i<in.length(); i++){
                if(in.charAt(i) == c) {
                    return false;
                }
            }
        }
        return true;
    }
    public BDD getOne(){

        return bddFactory.one();
    }

    public ArrayList<Assignment> satDFS(BDD b){
        ArrayList<Assignment> result = new ArrayList<Assignment>();
        Assignment tmp = new Assignment(propositions);
        recSatDFS(result, b, tmp);
        return result;
    }

    private void recSatDFS(ArrayList<Assignment> allPaths, BDD b, Assignment path){
        BDD high = b.high();
        BDD low = b.low();

        Assignment lastPath = new Assignment(path);
        if(high.isOne() && low.isOne())
            return;
        if(high.isOne()){
            lastPath.add(new PairOfInts(b.var(), 1));
            allPaths.add(lastPath);
        }
        else if(!high.isZero()) {
            lastPath.add(new PairOfInts(b.var(), 1));
            recSatDFS(allPaths, high, lastPath);
        }
        lastPath = new Assignment(path);
        if(low.isOne()){
            lastPath.add(new PairOfInts(b.var(), 0));
            allPaths.add(lastPath);
        }
        else if(!low.isZero()) {
            lastPath.add(new PairOfInts(b.var(), 0));
            recSatDFS(allPaths, low, lastPath);
        }
    }

    public boolean isWeaker(BDD min, BDD max){
        ArrayList<Assignment> minResult = satDFS(min);
        ArrayList<Assignment> maxResult = satDFS(max);
        for(Assignment minAssignment : minResult){
            boolean satisfied = false;
            for(Assignment maxAssignment : maxResult){
                if(minAssignment.isWeakerThan(maxAssignment)) {
                    satisfied = true;
                    break;
                }
            }
            if(!satisfied)
                return false;
        }
        return true;
    }
}

class Assignment {
    HashMap<String, Integer> values;
    ArrayList<Pair<String, BDD>> propositions;


    public Assignment(ArrayList<Pair <String, BDD>> propositions){
        this.propositions = propositions;
        this.values = new HashMap<String, Integer>();
        for(Pair<String, BDD> unit : propositions){
            values.put(unit.getElement0(), -1);
        }
    }

    public Assignment(Assignment a){
        this.propositions = a.propositions;
        this.values = new HashMap<String, Integer>();
        for(String name : a.values.keySet()){
            this.values.put(name, a.values.get(name));
        }
    }
    public void add(PairOfInts pair){
        for(Pair<String, BDD> proposition : propositions){
            BDD element1 = proposition.getElement1();
            if(element1.var() == pair.first)
                values.put(proposition.getElement0(), pair.second);
        }
    }

    public boolean isWeakerThan(Assignment big){
        for(String key : values.keySet()){
            if(big.values.get(key) == null)
                return false;

            if(values.get(key) < big.values.get(key))
                return false;
            else if(!values.get(key).equals(big.values.get(key)) && ! big.values.get(key).equals(-1))
                return false;
        }
        return true;
    }
}
