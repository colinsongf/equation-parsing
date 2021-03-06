package numoccur;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import joint.JointX;
import joint.JointY;
import structure.Node;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class NumoccurY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<Integer> numOccurList;
	
	public NumoccurY(List<Integer> list) {
		numOccurList = new ArrayList<>();
		numOccurList.addAll(list);
	}
	
	public NumoccurY(NumoccurY other) {
		numOccurList = new ArrayList<>();
		numOccurList.addAll(other.numOccurList);
	}
	
	public NumoccurY(SimulProb prob) {
		numOccurList = new ArrayList<>();
		for(int i=0; i<prob.quantities.size(); ++i) {
			int numOccur = 0;
			for(Node leaf : prob.equation.root.getLeaves()) {
				if(leaf.label.equals("NUM") && Tools.safeEquals(
						leaf.value, Tools.getValue(prob.quantities.get(i)))) {
					numOccur++;
				}
			}
			if(numOccur>2) {
				System.err.println("Issue here NumOccur > 2 : "+prob.index+" : "+prob.text);
			}
			numOccurList.add(numOccur);
		}
	}
	
	public NumoccurY(JointX x, JointY y) {
		numOccurList = new ArrayList<Integer>();
		for(int i=0; i<x.quantities.size(); ++i) {
			int numOccur = 0;
			for(Node leaf : y.equation.root.getLeaves()) {
				if(leaf.label.equals("NUM") && Tools.safeEquals(
						leaf.value, Tools.getValue(x.quantities.get(i)))) {
					numOccur++;
				}
			}
			numOccurList.add(numOccur);
		}
	}
	
	public NumoccurY(JointX x, List<Node> leaves) {
		numOccurList = new ArrayList<Integer>();
		for(int i=0; i<x.quantities.size(); ++i) {
			int numOccur = 0;
			for(Node leaf : leaves) {
				if(leaf.label.equals("NUM") && Tools.safeEquals(
						leaf.value, Tools.getValue(x.quantities.get(i)))) {
					numOccur++;
				}
			}
			numOccurList.add(numOccur);
		}
	}
	
	public static float getLoss(NumoccurY gold, NumoccurY pred) {
		float loss = 0.0f;
		if(gold.numOccurList.size() != pred.numOccurList.size()) return 10.0f;
		for(int i=0; i<gold.numOccurList.size(); ++i) {
			if(gold.numOccurList.get(i) != pred.numOccurList.get(i)) {
				loss += 1.0f;
			}
		}
		return loss;
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(numOccurList);
	}
}