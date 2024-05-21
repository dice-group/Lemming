package org.aksw.simba.lemming.mimicgraph.vertexselection;

public class TripleProposal {
	int headId;
	int edgeId;
	int tailId;

	public TripleProposal(int headId, int edgeId, int tailId) {
		this.headId = headId;
		this.edgeId = edgeId;
		this.tailId = tailId;
	}

	public int getHeadId() {
		return headId;
	}

	public int getEdgeId() {
		return edgeId;
	}

	public int getTailId() {
		return tailId;
	}

}
