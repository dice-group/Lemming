package org.aksw.simba.lemming.mimicgraph.literals;

import com.carrotsearch.hppc.BitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OrdinatesLiteralGenerator extends AbstractLiteralGenerator implements ILiteralGenerator{

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdinatesLiteralGenerator.class);

    /**
     * 1.key: date typed edge colour  2.key: literal tail vertex colour  value: an double array of size 2,
     * first value is min value of first ordinate, second value is min value of second ordinate.
     */
    private Map<BitSet, Map<BitSet, double[]>> mapOfMinValues;

    /**
     * 1.key: date typed edge colour  2.key: literal tail vertex colour  value: an double array of size 2,
     * first value is max value of first ordinate, second value is max value of second ordinate.
     */
    private Map<BitSet, Map<BitSet, double[]>> mapOfMaxValues;

    public OrdinatesLiteralGenerator(Map<BitSet, Map<BitSet, Set<String>>> sampleData) {
        super(sampleData);
        mapOfMinValues = new HashMap<>();
        mapOfMaxValues = new HashMap<>();
        computeDataRange();
    }

    private void computeDataRange(){
        LOGGER.info("Start - computation of range for literals with type ordinates");
        Set<BitSet> dteColours = mBaseData.keySet();
        for(BitSet dteColour : dteColours){
            Map<BitSet, Set<String>> mapTColour2Literals = mBaseData.get(dteColour);
            if(mapTColour2Literals != null && !mapTColour2Literals.isEmpty()){
                Map<BitSet, double[]> mapTColour2Mins = new HashMap<>();
                Map<BitSet, double[]> mapTColour2Maxes = new HashMap<>();

                Set<BitSet> tColours = mapTColour2Literals.keySet();
                for(BitSet tColour : tColours){
                    Set<String> literals = mapTColour2Literals.get(tColour);
                    if(literals != null && !literals.isEmpty()){
                        double[] mins = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
                        double[] maxes = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
                        for(String literal : literals){
                            double[] ordinates = getOrdinatesValues(literal);
                            mins[0] = Double.min(mins[0], ordinates[0]);
                            mins[1] = Double.min(mins[1], ordinates[1]);
                            maxes[0] = Double.max(maxes[0], ordinates[0]);
                            maxes[1] = Double.max(maxes[1], ordinates[1]);
                        }
                        mapTColour2Mins.put(tColour, mins);
                        mapTColour2Maxes.put(tColour, maxes);
                    }
                }
                mapOfMinValues.put(dteColour, mapTColour2Mins);
                mapOfMaxValues.put(dteColour, mapTColour2Maxes);
            }
        }
        LOGGER.info("End - computation of range for literals with type ordinates");
    }

    /**
     * The method is used to change a literal with type ordinates into a double array.
     */
    private double[] getOrdinatesValues(String literal){
        double[] values = new double[2];
        String[] words = literal.split(" ");
        int i= 0;
        for(String word : words ){
            if(!word.isEmpty()){
                try{
                    double value = Double.parseDouble(word);
                    values[i] = value;
                    i++;
                }catch (Exception e){
                    LOGGER.error("The given ordinates cannot be parsed!");
                    return new double[]{0.0, 0.0};
                }
            }
        }
        return values;
    }


    /**
     * Get an ordinates for the given data typed edge colour and tail colour.
     * Note: the numberOfValues plays no role in this method, because we always generate 2 doubles to form a literal
     */
    @Override
    public String getValue(BitSet tColo, BitSet dteColo, int numberOfValues){
        String literal = "";
        if(tColo != null && dteColo != null){
            Map<BitSet, double[]> mapTColour2Mins = mapOfMinValues.get(dteColo);
            Map<BitSet, double[]> mapTColour2Maxes = mapOfMaxValues.get(dteColo);
            if(mapTColour2Mins != null && !mapTColour2Mins.isEmpty() &&
                mapTColour2Maxes != null && !mapTColour2Maxes.isEmpty()){
                double[] mins = mapTColour2Mins.get(tColo);
                double[] maxes = mapTColour2Maxes.get(tColo);
                if(mins != null && maxes !=null){
                    double ordinate1 = mins[0] + mRand.nextDouble()*(maxes[0] - mins[0]);
                    double ordinate2 = mins[1] + mRand.nextDouble()*(maxes[1] - mins[1]);
                    String string1 = String.format("%.4f", ordinate1);
                    String string2 = String.format("%.4f", ordinate2);
                    literal = string1 + " " + string2;
                }
            }else{
                LOGGER.error("Cannot generate valid ordinates literal!");
                return "0.0 0.0";
            }
        }else {
            LOGGER.error("Cannot generate valid ordinates literal!");
            return "0.0 0.0";
        }
        return literal;
    }
}

