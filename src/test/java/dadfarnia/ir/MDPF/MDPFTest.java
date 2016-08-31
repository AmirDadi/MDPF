package dadfarnia.ir.MDPF;

import org.junit.*;



/**
 * Unit test for simple App.
 */
public class MDPFTest {
    MDPF mdpf;

    @Before
    public void initialize(){
        System.out.println("Tests Started");
        String path = getClass().getResource("/MDPFInput.json").getPath();
        mdpf = new MDPF(path);
        System.out.println("Test initialized.");
    }

    @Test
    public void testMDPFCreate(){
        System.out.println("Test mdpf creation.");
        Assert.assertEquals(mdpf.getStates().size(), 5);
    }

    @Test
    public void testSimpleSatResult1(){
        System.out.println("Print Result of 'Send'");
        ResultSet result = mdpf.sat("$send");
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(result.get("s2").size(), 1);
        Assert.assertEquals(result.get("s2").get(0).toString(), "T/1.0");
        System.out.println("State s2 : T/1.0 satisfied");
    }

    @Test
    public void testSimpleSatResult2(){
        System.out.println("Print Result of 'Try'");
        ResultSet result = mdpf.sat("$try");
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(result.get("s1").size(), 1);
        Assert.assertEquals(result.get("s1").get(0).toString(), "T/1.0");
        System.out.println("State s1 : T/1.0 satisfied");
    }

    @Test
    public void testOrSatResult(){
        System.out.println("Print Result of 'Try or Send'");
        ResultSet result = mdpf.sat("|$send$try");
        Assert.assertEquals(result.size(), 5);

        Assert.assertEquals(result.get("s1").size(), 1);
        Assert.assertEquals(result.get("s2").size(), 1);

        Assert.assertEquals(result.get("s1").get(0).toString(), "T/1.0");
        System.out.println("State s1 : T/1.0 satisfied");
        Assert.assertEquals(result.get("s2").get(0).toString(), "T/1.0");
        System.out.println("State s2 : T/1.0 satisfied");
    }
    @Test
    public void testNotSatResult(){
        System.out.println("Print Result of 'not Try'");
        ResultSet result = mdpf.sat("~$try");
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(result.get("s1").size(), 0);
        Assert.assertEquals(result.get("s0").get(0).toString(), "T/1.0");
        Assert.assertEquals(result.get("s2").get(0).toString(), "T/1.0");
        Assert.assertEquals(result.get("s3").get(0).toString(), "T/1.0");
        System.out.println("State s1 : T/0.0 satisfied and other states are 'T/1.0'");
    }

    @Test
    public void testNextSatFunction(){
        ResultSet result = mdpf.sat("@$send");
        result.print();
        Assert.assertEquals(result.size(), 5);
    }

}
