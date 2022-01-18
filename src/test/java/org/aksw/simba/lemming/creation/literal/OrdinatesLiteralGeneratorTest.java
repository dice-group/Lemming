package org.aksw.simba.lemming.creation.literal;

import org.aksw.simba.lemming.mimicgraph.literals.OrdinatesLiteralGenerator;
import com.carrotsearch.hppc.BitSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class OrdinatesLiteralGeneratorTest {

    @Test
    public void test(){
        OrdinatesLiteralGenerator generator = new OrdinatesLiteralGenerator(createSampleData());

        BitSet b0 = new BitSet();
        b0.flip(0);
        BitSet b1 = new BitSet();
        b1.flip(1);
        BitSet b2 = new BitSet();
        b2.flip(2);
        BitSet b3 = new BitSet();
        b3.flip(3);
        BitSet b4 = new BitSet();
        b4.flip(4);

        double[] expectedRange02 = {-1.0, 1.0, -1.0, 1.0};
        double[] expectedRange03 = {-10.0, 1.0, 1.0, 10.0};
        double[] expectedRange14 = {0.0, 100.0, -100.0, 50.0};
        double[] expectedRange04 = {0.0, 0.0, 0.0, 0.0};
        double[] acturalRange02 = generator.getOrdinatesRange(b0, b2);
        double[] acturalRange03 = generator.getOrdinatesRange(b0, b3);
        double[] acturalRange14 = generator.getOrdinatesRange(b1, b4);
        double[] acturalRange04 = generator.getOrdinatesRange(b0, b4);

        //test the range calculation
        for(int i = 0 ; i < 4 ; i++){
            Assert.assertEquals(expectedRange02[i], acturalRange02[i], 0.001);
            Assert.assertEquals(expectedRange03[i], acturalRange03[i], 0.001);
            Assert.assertEquals(expectedRange14[i], acturalRange14[i], 0.001);
            Assert.assertEquals(expectedRange04[i], acturalRange04[i], 0.001);
        }

        //test the generated ordinates
        for(int i = 0; i < 100 ; i++){
            String[] ordinate02 = generator.getValue(b2, b0, 0).split(" ");
            Assert.assertTrue(Double.parseDouble(ordinate02[0]) >= -1.0 && Double.parseDouble(ordinate02[0]) <= 1.0
                              && Double.parseDouble(ordinate02[1]) >= -1.0 && Double.parseDouble(ordinate02[1]) <= 1.0  );

            String[] ordinate03 = generator.getValue(b3, b0, 0).split(" ");
            Assert.assertTrue(Double.parseDouble(ordinate03[0]) >= -10.0 && Double.parseDouble(ordinate03[0]) <= 1.0
                && Double.parseDouble(ordinate03[1]) >= 1.0 && Double.parseDouble(ordinate03[1]) <= 10.0  );

            String[] ordinate14 = generator.getValue(b4, b1, 0).split(" ");
            Assert.assertTrue(Double.parseDouble(ordinate14[0]) >= 0.0 && Double.parseDouble(ordinate14[0]) <= 100.0
                && Double.parseDouble(ordinate14[1]) >= -100.0 && Double.parseDouble(ordinate14[1]) <= 50.0  );

            String[] ordinate04 = generator.getValue(b4, b0, 0).split(" ");
            Assert.assertTrue(Double.parseDouble(ordinate04[0]) == 0.0 && Double.parseDouble(ordinate04[0]) == 0.0
                && Double.parseDouble(ordinate04[1]) == 0.0 && Double.parseDouble(ordinate04[1]) == 0.0  );
        }
    }

    /**
     * create sample data for LiteralGenerator
     */
    private Map<BitSet, Map<BitSet, Set<String>>> createSampleData(){
        Map<BitSet, Map<BitSet, Set<String>>> data = new HashMap<>();
        //create data typed edge color
        BitSet dteColo0 = new BitSet();
        dteColo0.flip(0);

        //create the corresponding map
        Map<BitSet, Set<String>> mTColo2Literal0 = new HashMap<>();
        BitSet tColo2 = new BitSet();
        tColo2.flip(2);
        Set<String> lits0 = new HashSet<>();
        lits0.add("-1 1");
        lits0.add("0 0");
        lits0.add("1 -1");
        mTColo2Literal0.put(tColo2, lits0);

        BitSet tColo3 = new BitSet();
        tColo3.flip(3);
        Set<String> lits1 = new HashSet<>();
        lits1.add("-10 1");
        lits1.add("-5 5");
        lits1.add("1 10");
        mTColo2Literal0.put(tColo3, lits1);
        data.put(dteColo0, mTColo2Literal0);

        //create data typed edge color
        BitSet dteColo1 = new BitSet();
        dteColo1.flip(1);

        //create the corresponding map
        Map<BitSet, Set<String>> mTColo2Literal1 = new HashMap<>();
        BitSet tColo4 = new BitSet();
        tColo4.flip(4);
        Set<String> lits2 = new HashSet<>();
        lits2.add("100 50");
        lits2.add("0 -100");
        lits2.add("1 -50");
        mTColo2Literal1.put(tColo4, lits2);
        data.put(dteColo1, mTColo2Literal1);

        return data;
    }
}
