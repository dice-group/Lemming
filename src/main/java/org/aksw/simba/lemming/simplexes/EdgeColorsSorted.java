package org.aksw.simba.lemming.simplexes;

import com.carrotsearch.hppc.BitSet;

public class EdgeColorsSorted {

	private BitSet headColo, tailColo;	

    public EdgeColorsSorted(BitSet a, BitSet b) {
        set(a, b); // sorting logic for the bitset
    	
    }
    
    private void set(BitSet a, BitSet b) {
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
    	
    	if (aLongValue < bLongValue) {
    		this.headColo = a;
    		this.tailColo = b;
    	} else {
    		this.headColo = b;
    		this.tailColo = a;
    	}
    }

    EdgeColorsSorted(EdgeColorsSorted t) {
        this.headColo = t.headColo;
        this.tailColo = t.tailColo;
    }    
    
    public BitSet getA() {
		return headColo;
	}

	public BitSet getB() {
		return tailColo;
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
        result = prime * result + headColo.hashCode();
        result = prime * result + tailColo.hashCode();
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
        EdgeColorsSorted other = (EdgeColorsSorted) obj;
        if (headColo.hashCode() != other.headColo.hashCode())
            return false;
        return tailColo.hashCode() == other.tailColo.hashCode();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new EdgeColorsSorted(this);
    }

}
