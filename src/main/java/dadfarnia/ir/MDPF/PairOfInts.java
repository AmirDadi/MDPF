package dadfarnia.ir.MDPF;
public class PairOfInts{
    Integer first;
    Integer second;
    PairOfInts(int first, int second){
        this.first = first;
        this.second = second;
    }
    public String toString(){
        return "<"+first+", " + second + ">";
    }
    public boolean equals(PairOfInts that){
        if(this.first == that.first && this.second == that.second)
            return true;
        return false;
    }
}
