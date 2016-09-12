package dadfarnia.ir.MDPF;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Binary Decision Diagrams (BDDs) are used for efficient computation of many common problems. <br>
 * This is done by giving a compact representation and a set of efficient operations on boolean functions f: {0,1}^n --> {0,1}.
 * BDDService is a service use for assigning name to bdd propositions, and manipulating with names in code instead of bdd itself.
 * http://javabdd.sourceforge.net/apidocs/index.html
 */
public class BDDService {
    /**
     * BDDFactory is an interface for the creation and manipulation of BDDs.
     */
    private BDDFactory bddFactory;
    /**
     * Arraylist used for naming bdds.
     */
    public ArrayList<Pair<String, BDD>> propositions;
    /**
     * Characters should not be used in variable and state names.
     */
    public static char[] specialChars = {'$', '&','|', '~', '@'};

    /**
     * Given an array of variable names, BDDFactory initialized
     * @param variables String[]
     */
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

    /**
     * Convert an string of expression in preorder <i> e.g &$b$a means (a and b)</i> to a bdd
     * @param exp StringBuilder
     * @return BDD
     */
    public BDD expressionToBDD(StringBuilder exp){
        for(int i=0; i<exp.length(); i++){
            char op = exp.charAt(0);
            String tmp = exp.substring(1);
            exp.delete(0, exp.length());
            exp.append(tmp);
            if(op == '$')
                return propositions.get(Utils.findInArrayOfPairs(propositions,getName(exp))).getElement1();
            else if(op == '&'){
                BDD firstArg = expressionToBDD(exp);
                BDD secondArg = expressionToBDD(exp);
                return firstArg.apply(secondArg, BDDFactory.and);
            }
            else if(op == '~'){
                return expressionToBDD(exp).not();
            }
            else if (op == '|'){
                BDD firstArg = expressionToBDD(exp);
                BDD secondArg = expressionToBDD(exp);
                return firstArg.apply(secondArg, BDDFactory.or);
            }
        }
        return null;
    }

    /**
     * String format of a BDD
     * @param b BDD
     * @return String
     */
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

    /**
     * Check is a character special one.
     * @param in char
     * @return boolean, true if is special character
     */
    public static boolean isSpecialChar(char in){
        for(char c : specialChars){
            if(c == in)
                return true;
        }
        return false;
    }

    /**
     * Check a name is valid or not
     * @param in String
     * @return boolean, true if valid.
     */
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

    /**
     *
     * @return true BDD
     */
    public BDD getOne(){
        return bddFactory.one();
    }

    /**
     * Return Assignment that satisfies a bdd. <i>e.g. for a & b returns ((a,1) , (b,1))</i>
     * if has more variables, all valid combinations are listed.
     * @param b BDD
     * @return ArrayList of Assignment
     */
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

    /**
     * Check if the min weaker than max. (Weaker definition is  in article II.A.): <br>
     *      <b>"We say w is weaker than w` if the set of products satisfying w is a superset of the same set for w`."</b>
     * @param min BDD
     * @param max BDD
     * @return boolean, true if min is weaker than max.
     */
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

/**
 * Used for Assignment of variable names to values.
 */
class Assignment {
    HashMap<String, Integer> values;
    ArrayList<Pair<String, BDD>> propositions;

    /**
     * Constructor
     * @param propositions ArrayList of (name, bdd) pairs.
     */
    public Assignment(ArrayList<Pair <String, BDD>> propositions){
        this.propositions = propositions;
        this.values = new HashMap<String, Integer>();
        for(Pair<String, BDD> unit : propositions){
            values.put(unit.getElement0(), -1);
        }
    }

    /**
     * Copy Constructor
     * @param a Assignment
     */
    public Assignment(Assignment a){
        this.propositions = a.propositions;
        this.values = new HashMap<String, Integer>();
        for(String name : a.values.keySet()){
            this.values.put(name, a.values.get(name));
        }
    }

    /**
     * Given a BDD_id and its value, it will be add to assignments
     * @param pair PairOfInts (bdd_var, value)
     */
    public void add(PairOfInts pair){
        for(Pair<String, BDD> proposition : propositions){
            BDD element1 = proposition.getElement1();
            if(element1.var() == pair.first)
                values.put(proposition.getElement0(), pair.second);
        }
    }

    /**
     * Check if this is weaker than the input
     *      <b>"We say w is weaker than w` if the set of products satisfying w is a superset of the same set for w`."</b>
     * @param big Assignment
     * @return boolean, return true if this is weaker.
     */
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
