package org.aksw.simba.lemming.simplexes;

import com.carrotsearch.hppc.BitSet;

public class TriColos {
	private BitSet a, b, c;	

    public TriColos(BitSet a, BitSet b, BitSet c) {
        set(a, b, c); // sorting logic for the bitset
    }

    TriColos(TriColos t) {
        this.a = t.a;
        this.b = t.b;
        this.c = t.c;
    }    
    
    
    /**
     * For the input BitSets their long values are analyzed and they are assigned to TriangleColors object as per ascending order.
     * @param a
     * @param b
     * @param c
     */
    public void set(BitSet a, BitSet b, BitSet c) {
    	
    	// Get the Long values for the three input BitSets
    	long[] aBits = a.bits;
    	double aLongValue = 0.0;
    	for (long tempValue: aBits) {
    		aLongValue = aLongValue + tempValue;
    	}
    	
    	long[] bBits = b.bits;
    	double bLongValue = 0.0;
    	for (long tempValue: bBits) {
    		bLongValue = bLongValue + tempValue;
    	}
    	
    	long[] cBits = c.bits;
    	double cLongValue = 0.0;
    	for (long tempValue: cBits) {
    		cLongValue = cLongValue + tempValue;
    	}
    	
    	//long aLongValue = a.bits[0];
    	//long bLongValue = b.bits[0];
    	//long cLongValue = c.bits[0];
    	
        if (aLongValue < bLongValue) { // isFirstSmallerthanSecond(a, b)
            if (aLongValue < cLongValue) { // isFirstSmallerthanSecond(a, c)
                this.a = a;
                if (bLongValue < cLongValue) { // isFirstSmallerthanSecond(b, c)
                    this.b = b;
                    this.c = c;
                } else {
                    this.b = c;
                    this.c = b;
                }
            } else {
                this.a = c;
                this.b = a;
                this.c = b;
            }
        } else {
            if (aLongValue < cLongValue) { // isFirstSmallerthanSecond(a, c)
                this.a = b;
                this.b = a;
                this.c = c;
            } else {
                this.c = a;
                if (bLongValue < cLongValue) { // isFirstSmallerthanSecond(b, c)
                    this.a = b;
                    this.b = c;
                } else {
                    this.a = c;
                    this.b = b;
                }
            }
        }
    }
    
    /*
     * Old Logic utilizing XOR operation for sorting the BitSet values.
    public void set(BitSet a, BitSet b, BitSet c) {
        if (isFirstSmallerthanSecond(a, b)) { // isFirstSmallerthanSecond(a, b)
            if (isFirstSmallerthanSecond(a, c)) { // isFirstSmallerthanSecond(a, c)
                this.a = a;
                if (isFirstSmallerthanSecond(b, c)) { // isFirstSmallerthanSecond(b, c)
                    this.b = b;
                    this.c = c;
                } else {
                    this.b = c;
                    this.c = b;
                }
            } else {
                this.a = c;
                this.b = a;
                this.c = b;
            }
        } else {
            if (isFirstSmallerthanSecond(a, c)) { // isFirstSmallerthanSecond(a, c)
                this.a = b;
                this.b = a;
                this.c = c;
            } else {
                this.c = a;
                if (isFirstSmallerthanSecond(b, c)) { // isFirstSmallerthanSecond(b, c)
                    this.a = b;
                    this.b = c;
                } else {
                    this.a = c;
                    this.b = b;
                }
            }
        }
    }
    */
    
    public BitSet getA() {
		return a;
	}

	public BitSet getB() {
		return b;
	}

	public BitSet getC() {
		return c;
	}


	/**
     * The function utilizes XOR operation to determine if first bitset is smaller then the second one
     * @param temp1
     * @param temp2
     * @return
     */
    public boolean isFirstSmallerthanSecond(BitSet temp1, BitSet temp2) {
    	
    	BitSet clone = (BitSet)temp1.clone();
    	// Performing XOR operation that returns the position of first different bit among temp 1 and temp 2
    	clone.xor(temp2); 
    	
    	// if both the BitSet are equal the XOR operation returns empty BitSet
    	if (clone.length() == 0)
    		return false;
    	
    	int firstDifferentBit = (int) (clone.length() - 1); // Initializing index of first different bit 
    	
    	// temp2 is true at firstDifferentBit index if it is greater than temp1
    	return !temp1.get(firstDifferentBit);
    	
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + a.hashCode();
        result = prime * result + b.hashCode();
        result = prime * result + c.hashCode();
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TriColos other = (TriColos) obj;
        if (a.hashCode() != other.a.hashCode())
            return false;
        if (b.hashCode() != other.b.hashCode())
            return false;
        return c.hashCode() == other.c.hashCode();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new TriColos(this);
    }

}
