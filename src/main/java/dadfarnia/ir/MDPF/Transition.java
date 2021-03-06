package dadfarnia.ir.MDPF;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import java.util.ArrayList;
/**
 * Represent a transition with probability, application condition.
 */
public class Transition {

    private double probability;
    private BDD applicationCondition;
    private BDDService bddService;

    /**
     * Transition Constructor
     * @param applicationCondition BDD
     * @param probability double
     * @param bddService BDDService
     */
    public Transition(BDD applicationCondition, double probability, BDDService bddService) {
        this.applicationCondition = applicationCondition;
        this.probability = probability;
        this.bddService = bddService;
    }

    /**
     * Minimize a columnar matrix, as min function defined in page 7 of article
     * @param input Arraylist of Arraylists of Transitions
     */
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


    public String toString(){
        String result = "";
        result += (bddService.toString(applicationCondition) + "/" + probability);
        return result;
    }

    /**
     * Calculate product of two array of transitions (two columnar matrices of transitions),
     * @param t1
     * @param t2
     * @param bddService
     * @return result = t1 x t2 = Sigma ( y1 ^ y2 ) / (p1 x p2), returns arraylist of transitions
     */
    public static ArrayList<Transition> product(ArrayList<Transition> t1, ArrayList<Transition>t2, BDDService bddService){
        ArrayList<Transition> result = new ArrayList<Transition>();
        if(t1.size() == 0 && t2.size() == 0)
            return result;
        else if(t1.size() == 0){
            for(Transition t: t2) {
                if (!t.applicationCondition.equals(bddService.getOne()))
                    result.add(new Transition(t.applicationCondition, 0, bddService));
            }
        }
        else if(t2.size() == 0){
            for(Transition t: t1){
                bddService.satDFS(t.applicationCondition);
                if(!t.applicationCondition.equals(bddService.getOne()))
                    result.add(new Transition(t.applicationCondition, 0, bddService));
            }
        }
        else{
            for(Transition ti : t1){
                for(Transition tj : t2){
                    BDD bddProduct = ti.applicationCondition.apply(tj.applicationCondition, BDDFactory.and);
                    result.add(new Transition(bddProduct, ti.probability*tj.probability, bddService));
                }
            }
        }
        return result;

    }

    /**
     * Calculate sum of a transition and an arraylist of transitions
     * The formula as in article:
     * w/p + r <br>
     *      if (w in domain of r): (r-w/r(w)) U w/(p+r(w))
     *      o.w:                   r U w/p
     * @param r
     * @param transition
     * @return arraylist of transitions
     */
    public static ArrayList<Transition> sum(ArrayList<Transition> r, Transition transition){
        ArrayList<Transition> result = new ArrayList<Transition>();
        result.addAll(r);
        boolean flag = false;
        for(Transition t : result){
            if(t.applicationCondition.equals(transition.applicationCondition)){
                t.probability += transition.probability;
                flag = true;
                break;
            }
        }
        if(!flag){
            result.add(transition);
        }
        return result;
    }

    /**
     * Calculate result of sum of two arraylist of transitions
     * @param r1 ArrayList of Transitions
     * @param r2 ArrayList of Transitions
     * @return ArrayList of Transitions
     */
    public static ArrayList<Transition> sum(ArrayList<Transition> r1, ArrayList<Transition> r2){
        ArrayList<Transition> result = new ArrayList<Transition>();
        result.addAll(r1);
        for(Transition t : r2){
            result = Transition.sum(result, t);
        }
        return result;
    }

    /**
     * We say w is weaker than w' if the set of products satisfying w is a superset of the same set for w'.
     * @param that Transition
     * @return boolean, true if this is weaker transition
     */
    public boolean isWeaker(Transition that){
        if(bddService.isWeaker(this.applicationCondition, that.applicationCondition) && that.probability <= this.probability)
            return true;
        return false;
    }

    /**
     * get probability
     * @return double
     */
    public double getProbability(){
        return probability;
    }

    /**
     * Check if all probabilities are zero
     * @param array ArrayList<Transitions>
     * @return boolean, return true if all Transitions have zero probability
     */
    public static boolean areAllZero(ArrayList<Transition> array){
        for(Transition t: array){
            if (t.probability != 0)
                return false;
        }
        return true;

    }
}
