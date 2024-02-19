package org.aksw.simba.lemming.simplexes;

import com.carrotsearch.hppc.BitSet;

public class EdgeColos {
	private BitSet headColo, tailColo;	

    public EdgeColos(BitSet a, BitSet b) {
        //set(a, b); // sorting logic for the bitset
    	this.headColo = a;
    	this.tailColo = b;
    }

    EdgeColos(EdgeColos t) {
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
        EdgeColos other = (EdgeColos) obj;
        if (headColo.hashCode() != other.headColo.hashCode())
            return false;
        return tailColo.hashCode() == other.tailColo.hashCode();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new EdgeColos(this);
    }

}
