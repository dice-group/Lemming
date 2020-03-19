package org.aksw.simba.borrowed.ldcbench.api;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *  TODO add dependency when available in mvn repo, borrowed from
 * {@link  <a href="https://github.com/dice-group/orca/blob/develop/ldcbench.api/src/main/java/org/dice_research/ldcbench/generate/ParallelBarabasiRDF.java">org.dice_research.ldcbench.generate.ParallelBarabasiRDF</a>}
 *
 */
public class ParallelBarabasiRDF implements GraphGenerator{
	protected Random generator;

    public String name;

	public ParallelBarabasiRDF(String gname) {
		name = gname;
	}

	protected int[] weightedSampleWithoutReplacement(int n, int m, int[] wt,Random generator) {
		/* without accumulated weights*/

		int[] Res = new int[m];

		int rand;
		int i;
	    int Sum = 0;
	    int n1 = n;

	    for(i = 1; i <= n; i++) Sum += wt[i];

		int[] wt1=Arrays.copyOf(wt,n+1);

		for (int j = 0; j < m; j++) {
			rand =  generator.nextInt(Sum);

			// find interval
			for (i = 1; i <= n1; i++) {
				rand-=wt1[i];
				if (rand < 0) {//to avoid getting nodes set to zero degree, must be less than
					Res[j] = i;
					Sum -= wt1[i];
					wt1[i] = 0;// to avoid replacement

					break;
				}
			}

		}

		return (Res);
	}

	protected int getInitGraph(int N, double degree, long seed,int[] subj,int[]obj,int[] inDeg,int m,Random generator) {
		int indexToEdgeList = 0;

		if (degree < 1) {
			throw new IllegalArgumentException("Degree must be more than 1.");
		}

		if (degree > (N-1)) {// max links created at any step is N-1
			throw new IllegalArgumentException("Degree can NOT be more than (N-1).");
		}

//		generator = new Random(seed);// seed

		if(m >1 ) {
			subj[indexToEdgeList] = 1;
			obj[indexToEdgeList] = 2;// first edge
			indexToEdgeList++;
			inDeg[2] = 2;
		}

		// initial part
		if (m > 2) {
			for (int i = 3; i <= m; i++) {
				int[] tmp = weightedSampleWithoutReplacement(i - 1, 2, inDeg,generator);// new links
				boolean randIndex = generator.nextBoolean();
				int vto;
				if (randIndex) {
					vto = 0;
				} else {
					vto = 1;
				}
				subj[indexToEdgeList] = i;
				obj[indexToEdgeList] = tmp[1 - vto];
				indexToEdgeList++;
				subj[indexToEdgeList] = tmp[vto];
				obj[indexToEdgeList] = i;
				indexToEdgeList++;// inverted link randomly
			}
		}

		return(indexToEdgeList);
	}

	//-------------------------------------------------------------------------------------------

	protected void getBarabasiRDFParallel(int N, double degree, long seed, GraphBuilder builder,int nCores) {
		/* nodes are numbered from 1 to N */
        /* uniform distribution for in/out typing*/
		class Connections{
			 int node;
			 int[] conns;
			 public Connections(int node,int[] conns) {
				 this.node = node;
				 this.conns = conns;
			 }
		}
		int indexToEdgeList ;// index to edge list
		long t0 = System.currentTimeMillis();
		long ti0 = System.currentTimeMillis();

		int nE=(int) Math.ceil(N*degree);
		int[] subj = new int[nE];
		int[] obj = new int[nE];
		int[] inDeg = new int[N + 1];
		Arrays.fill(inDeg, 1);
		inDeg[0] = 0;// not used
		int m = (int) Math.floor(degree);// average degree of graph

		generator=new Random(seed);
		indexToEdgeList=getInitGraph(N,degree,seed,subj,obj,inDeg,m,generator);

		int P1 = (m + 1)*(N-m) - (nE - indexToEdgeList) + m ;
		if (P1<=m ) P1 = m + 1;

		double biasedCoin=((m/2.0-1)/(m-1));
		long ts10k = 0;
		int cntE=indexToEdgeList;
		List<Connections> clist;
		for (int i = m + 1; i <= N; i+=nCores) {//index to node
			long ts1=System.currentTimeMillis();

			int m1= 1+ generator.nextInt(2*m);// to use uniform distribution from 1 to 2m
			if(m1 >= i) m1=i-1;
			if(m1 > 2*m) m1=2*m;
			int  m2=m1;

			clist= IntStream.range(i, i + nCores).parallel()
	        .mapToObj(a -> new Connections(a,weightedSampleWithoutReplacement(a - 1, m2, inDeg,generator)))
	        		.collect(Collectors.toList());

			ts10k += (System.currentTimeMillis()-ts1);
			/*
			 * 8/2/2019: choose randomly the number of in-links [1,m],
			 * 	 1. select randomly one link to be in-link then
			 *   2. toss a biased coin ((m/2)-1 inlink,(m/2) outlink)
			 */
			//in- link
			for(Connections cs:clist) {
				int[] tmp=cs.conns;
				int node=cs.node;

				cntE += m1;
				if(cntE > nE) {m1-=(cntE-nE);
				cntE=nE;
				}

				int vin_ix = generator.nextInt(m1) ;//uniform distribution of(m)
				if (vin_ix == m1)
					vin_ix--;

				biasedCoin=((m1/2.0-1)/(m1-1));
				for (int k = 0; k < m1; k++) {
					if (k != vin_ix && (generator.nextDouble() > biasedCoin) ) {
						inDeg[tmp[k]] = inDeg[tmp[k]] + 1;
						subj[indexToEdgeList] = node;
						obj[indexToEdgeList] = tmp[k];
					} else {// inverted link
						inDeg[node] = inDeg[node] + 1;
						subj[indexToEdgeList] = tmp[k];
						obj[indexToEdgeList] = node;
					}
					indexToEdgeList++;
				}
				if(node==P1) { // consider nCores
					m = m + 1;//second part
				}

				if (node % 10000 == 0) {
					long ti1;
					ti1= System.currentTimeMillis();
					ts10k=0;
					ti0= System.currentTimeMillis();
				}
				if(indexToEdgeList >= nE) break;
			}
			if(indexToEdgeList >= nE) break;
		}

		long t_barabasi = System.currentTimeMillis();

		//Grph structure ----------------------------------------------
		int nEdges = indexToEdgeList;
		int[] idRange=builder.addNodes(N);//Range
		for(int i=0; i < nEdges; i++) {
			if(!builder.addEdge(subj[i]-1 + idRange[0], obj[i]-1+idRange[0], 0)) {
				System.out.println("Failed to add edge : i="+i+" subj="+subj[i]+" idRange[0]=" + idRange[0]+" obj[i]="+ obj[i]);
			};
		}
		int[] entranceNodes= {0};
		builder.setEntranceNodes(entranceNodes);

		long t_gbuilder = System.currentTimeMillis();
	}

	//-------------------------------------------------------------------------------------------

	@Override
public void generateGraph(int numberOfNodes, double avgDegree, long seed, GraphBuilder builder) {
	this.getBarabasiRDFParallel(numberOfNodes, avgDegree, seed,builder,3);
}

@Override
public void generateGraph(double avgDegree, int numberOfEdges, long seed, GraphBuilder builder) {
	this.getBarabasiRDFParallel((int)Math.ceil(numberOfEdges/avgDegree), avgDegree, seed,builder,3);
}
}
