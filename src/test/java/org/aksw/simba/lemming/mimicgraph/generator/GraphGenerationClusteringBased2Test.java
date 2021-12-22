package org.aksw.simba.lemming.mimicgraph.generator;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class GraphGenerationClusteringBased2Test{

	@Test
	public void test() {
		
		int[] mean = {7,30, 4, 8, 50, 100, 25, 1};
		for(int m : mean) {
			   double diff = 0.0, diff1 = 0.0;
			for(int l = 0; l < 1000; l++){
				int j = 10000;
				double k = 0.0;
				while(j>0){
					int i = getPoissonRandom(m);
					   //System.out.println("In 7 printing the value of i : "+i);  
					   k += i;
					   j--;
					
				}
				if((k/10000)>m){
					diff = (k/10000)-m;
				}
				else{
					diff = m-(k/10000);
				}
				//System.out.println("printing k : "+ (k/10000) +" and k - 30 : "+ diff);
				diff1 += diff;
				//break;
				
			}
			System.out.println("Printng the value of diff : "+(diff1/1000)+" and percentage of error : "+ (((diff1/1000)/m)*100));
			assertTrue((((diff1/1000)/m)*100) < 1);
		}		
		//fail("Not yet implemented");
	}	
	
	static int getPoissonRandom(double mean) {
		Random r = new Random();
		double L = Math.exp(-mean);
		int k = 0;
		double p = 1.0;
		do {
			p = p * r.nextDouble();
			k++;
		} while (p > L);
		//System.out.println("In 1085 printing the value of k-1 : "+(k-1));
		return k - 1;
	}
}
