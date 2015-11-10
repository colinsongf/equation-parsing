package lca;

import java.util.List;

import structure.Node;
import structure.SimulProb;
import tree.TreeX;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class LcaX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<QuantSpan> quantities;
	public List<IntPair> candidateVars;
	public Node leaf1, leaf2;
	String midPhrase, prePhrase, leftToken;
	
	public LcaX(SimulProb simulProb, Node leaf1, Node leaf2, 
			String prePhrase, String midPhrase) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		this.leaf1 = leaf1;
		this.leaf2 = leaf2;
		this.prePhrase = prePhrase;
		this.midPhrase = midPhrase;
	}
	
	public LcaX(TreeX simulProb, Node leaf1, Node leaf2, 
			String prePhrase, String midPhrase) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.problemIndex;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		this.leaf1 = leaf1;
		this.leaf2 = leaf2;
		this.prePhrase = prePhrase;
		this.midPhrase = midPhrase;
	}
	
	public LcaX(struct.lca.LcaX simulProb, Node leaf1, Node leaf2,
			String prePhrase, String midPhrase) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.problemIndex;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		this.leaf1 = leaf1;
		this.leaf2 = leaf2;
		this.prePhrase = prePhrase;
		this.midPhrase = midPhrase;
	}
	
	
	
}