package dadfarnia.ir.MDPF;

import net.sf.jbddi.BDD;

public class Main {

    public static void main(String[] args) {
//        BDDService.infixToPrefix("(x1& x2)| (z&(~t1))");
        System.out.println("Start");
        MDPFExampleTest();
//        MDPFUntilTest();
    }


    /**
     * A Simple Test on reading MDPF From File and Calculate PCTL Formula result
     */
    public static void MDPFExampleTest(){
        MDPF model = new MDPF("MDPFInput.json");
        model.print();
        System.out.println("###########");
        ResultSet result = model.sat("$True");
        result.print();
        result.getProbability(">=", 0.8);
    }
    public static void MDPFUntilTest(){
        MDPF model = new MDPF("MDPFInput.json");
        model.print();
        System.out.println("###########");
        ResultSet result = model.sat("U3$send$wait");
    }

}
