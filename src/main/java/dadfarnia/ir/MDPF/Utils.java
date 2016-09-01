package dadfarnia.ir.MDPF;
import java.util.ArrayList;

import net.sf.javabdd.BDD;
public class Utils {
    /**
     * Find in array of pairs
     * @param array ArrayList<Pair<String, BDD>>
     * @param key String
     * @return int, position of key
     */
    public static int findInArrayOfPairs(ArrayList<Pair<String, BDD>> array, String key){
        for(int i=0; i<array.size(); i++){
            if(array.get(i).getElement0().equals(key))
                return i;
        }
        return -1;

    }
    public static ArrayList<String> getFirstArguments(ArrayList<Pair<String, BDD>> input){
        ArrayList<String> result = new ArrayList<String>();
        for(Pair<String, BDD> pair: input){
            result.add(pair.getElement0());
        }
        return result;
    }
}
