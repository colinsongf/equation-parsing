package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class SemInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private SemFeatGen featGen;
	private List<SemY> templates;

	public SemInfSolver(SemFeatGen featGen, List<SemY> templates) {
		this.featGen = featGen;
		this.templates = templates;
	}

	public static List<SemY> extractTemplates(SLProblem slProb) {
		List<SemY> templates = new ArrayList<>();
		for(IStructure struct : slProb.goldStructureList) {
			SemY gold = (SemY) struct;
			SemY eq1 = new SemY(gold);
			for(int j=0; j<5; ++j) {
				for(int k=0; k<eq1.terms.get(j).size(); ++k) {
					eq1.terms.get(j).get(k).setSecond(null);
					eq1.emptySlots.add(new IntPair(j, k));
				}
			}
			boolean alreadyPresent = false;
			for(SemY eq2 : templates) {
				boolean diff = false;
				for(int j=0; j<5; ++j) {
					if(eq1.terms.get(j).size() != eq2.terms.get(j).size()) {
						diff = true; break;
					}
					if(diff) break;
					for(int k=0; k<eq1.terms.get(j).size(); k++) {
						if(eq1.terms.get(j).get(k).getFirst() != 
								eq2.terms.get(j).get(k).getFirst()) {
							diff = true; break;
						}
					}
					if(diff) break;
					for(int k=0; k<4; k++) {
						if(eq1.operations.get(k) != eq2.operations.get(k)) {
							diff = true; break;
						}
					}
					if(diff) break;
				}
				if(!diff) {
					alreadyPresent = true;
					break;
				}
			}
			if(!alreadyPresent) templates.add(eq1);
		}
		System.out.println("Number of templates : "+templates.size());
		return templates;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		SemY y1 = (SemY) arg1;
		SemY y2 = (SemY) arg2;
		return SemY.getLoss(y1, y2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		SemX blob = (SemX) x;
		SemY gold = (SemY) goldStructure;
		SemY pred = new SemY();

		PairComparator<SemY> semPairComparator = 
				new PairComparator<SemY>() {};
		BoundedPriorityQueue<Pair<SemY, Double>> beam1 = 
				new BoundedPriorityQueue<Pair<SemY, Double>>(50, semPairComparator);
		BoundedPriorityQueue<Pair<SemY, Double>> beam2 = 
				new BoundedPriorityQueue<Pair<SemY, Double>>(50, semPairComparator);
		
		Set<Double> availableNumbers = new HashSet<Double>();
		for(Double d : Tools.uniqueNumbers(blob.relationQuantities)) {
			availableNumbers.add(d);
		}
		
		for(SemY template : templates) {
			if(availableNumbers.size() >= template.emptySlots.size()) {
				beam1.add(new Pair<SemY, Double>(template, 0.0));
			}
		}
		
		for(Pair<SemY, Double> pair : beam1) {
			for(SemY y : enumerateSemYs(availableNumbers, pair.getFirst())) {
				beam2.add(new Pair<SemY, Double>(y, pair.getSecond() + 
						wv.dotProduct(featGen.getFeatureVector(blob, y))));		
			}
		}
		return beam2.element().getFirst();
	}
	
	public List<SemY> enumerateSemYs(Set<Double> availableNumbers, SemY seed) {
		List<SemY> list1 = new ArrayList<>();
		list1.add(seed);
		List<SemY> list2 = new ArrayList<>();
		for(IntPair slot : seed.emptySlots) {
			for(Double d : availableNumbers) {
				SemY y = new SemY(seed);
				y.terms.get(slot.getFirst()).get(slot.getSecond()).setSecond(d);
				list2.add(y);
			}
			list1.clear();
			list1.addAll(list2);
			list2.clear();
		}
		for(SemY y : list1) {
			boolean allow = true;
			for(int i=0; i<seed.emptySlots.size(); ++i) {
				IntPair slot1 = seed.emptySlots.get(i);
				for(int j=i+1; j<seed.emptySlots.size(); ++j) {
					IntPair slot2 = seed.emptySlots.get(j);
					if(Tools.safeEquals(y.terms.get(slot1.getFirst())
							.get(slot1.getSecond()).getSecond(), 
							y.terms.get(slot2.getFirst())
							.get(slot2.getSecond()).getSecond())) {
						allow = false;
						break;
					}
				}
				if(!allow) break;
			}
			if(allow) continue;
			y.emptySlots.clear();
			list2.add(y);
		}
		return list2;
	}
		
	
}