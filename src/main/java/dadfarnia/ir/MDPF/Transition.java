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
     * @param applicationCondition
     * @param probability
     * @param bddService
     */
    public Transition(BDD applicationCondition, double probability, BDDService bddService) {
        this.applicationCondition = applicationCondition;
        this.probability = probability;
        this.bddService = bddService;
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
     * @param r1
     * @param r2
     * @return
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
     * @param that
     * @return weaker transition
     */
    public boolean isWeaker(Transition that){
        if(bddService.isWeaker(this.applicationCondition, that.applicationCondition) && that.probability <= this.probability)
            return true;
        return false;
    }
    public double getProbability(){
        return probability;
    }
}
