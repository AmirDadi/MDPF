package dadfarnia.ir.MDPF;

import org.junit.*;



/**
 * Unit test for MDPF
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
    public void testTrueSatResult(){
        System.out.println("Print Result of 'True'");
        ResultSet result = mdpf.sat("$True");
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(result.get("s0").size(), 1);
        Assert.assertEquals(result.get("s1").size(), 1);
        Assert.assertEquals(result.get("s2").size(), 1);
        Assert.assertEquals(result.get("s3").size(), 1);
        Assert.assertEquals(result.get("s4").size(), 1);
        Assert.assertEquals(result.get("s0").get(0).toString(), "T/1.0");
        Assert.assertEquals(result.get("s1").get(0).toString(), "T/1.0");
        Assert.assertEquals(result.get("s2").get(0).toString(), "T/1.0");
        Assert.assertEquals(result.get("s3").get(0).toString(), "T/1.0");
        Assert.assertEquals(result.get("s4").get(0).toString(), "T/1.0");
    }
    @Test
    public void testFalseSatResult(){
        System.out.println("Print Result of 'False'");
        ResultSet result = mdpf.sat("$False");
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(result.get("s0").size(), 0);
        Assert.assertEquals(result.get("s1").size(), 0);
        Assert.assertEquals(result.get("s2").size(), 0);
        Assert.assertEquals(result.get("s3").size(), 0);
        Assert.assertEquals(result.get("s4").size(), 0);
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
    public void testSimpleNextSatFunction1(){
        System.out.println("Print Result of 'Next send'");
        ResultSet result = mdpf.sat("@$send");
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(result.get("s0").size(), 0);
        Assert.assertEquals(result.get("s2").size(), 0);
        Assert.assertEquals(result.get("s3").size(), 0);
        Assert.assertEquals(result.get("s4").size(), 0);
        Assert.assertEquals(result.get("s1").size(), 2);
        Assert.assertTrue(result.get("s1").toString().contains("A/0.0"));
        Assert.assertTrue(result.get("s1").toString().contains("B/0.8"));
        System.out.println("All states except s1 have zero size");
    }

    @Test
    public void testSimpleNextSatFunction2(){
        System.out.println("Print Result of 'Next wait'");
        System.out.println("Result should be \n s0\n" +
                "\tM/1.0\n" +
                "s1\n" +
                "\tB/0.2\n" +
                "\tA/0.0\n" +
                "s4\n" +
                "\t(not B) and U/1.0");
        ResultSet result = mdpf.sat("@$wait");
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(result.get("s2").size(), 0);
        Assert.assertEquals(result.get("s3").size(), 0);
        Assert.assertEquals(result.get("s0").size(), 1);
        Assert.assertEquals(result.get("s1").size(), 2);
        Assert.assertEquals(result.get("s4").size(), 1);
        Assert.assertTrue(result.get("s0").toString().contains("M/1.0"));
        Assert.assertTrue(result.get("s1").toString().contains("A/0.0"));
        Assert.assertTrue(result.get("s1").toString().contains("B/0.2"));
        Assert.assertTrue(result.get("s4").toString().contains("1.0"));
        System.out.println("Result asserted");
    }

    @Test
    public void testNextSatFunction(){
        System.out.println("Print Result of 'Next (wait and try)'");
        System.out.println("Result should be \n s0\n" +
                "\tM/1.0\n" +
                "s1\n" +
                "\tB/0.2\n" +
                "\tA/0.0\n" +
                "s4\n" +
                "\t(not B) and U/1.0");
        ResultSet result = mdpf.sat("@&$try$wait");
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(result.get("s2").size(), 0);
        Assert.assertEquals(result.get("s3").size(), 0);
        Assert.assertEquals(result.get("s0").size(), 1);
        Assert.assertEquals(result.get("s1").size(), 2);
        Assert.assertEquals(result.get("s4").size(), 1);
        Assert.assertTrue(result.get("s0").toString().contains("M/1.0"));
        Assert.assertTrue(result.get("s1").toString().contains("A/0.0"));
        Assert.assertTrue(result.get("s1").toString().contains("B/0.2"));
        Assert.assertTrue(result.get("s4").toString().contains("1.0"));
        System.out.println("Result asserted");
    }

    @Test
    public void testNextSatFunctionWithEmptyResult(){
        System.out.println("Print Result of 'Next (restart and retry)'");
        System.out.println("Result should be none");
        ResultSet result = mdpf.sat("@&$restart$retry");
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(result.get("s0").size(), 0);
        Assert.assertEquals(result.get("s1").size(), 0);
        Assert.assertEquals(result.get("s2").size(), 0);
        Assert.assertEquals(result.get("s3").size(), 0);
        Assert.assertEquals(result.get("s4").size(), 0);
    }


}
