package equationmatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import structure.Equation;
import structure.Operation;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class EquationInfSolver extends AbstractInferenceSolver 
implements Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private EquationFeatureExtractor featGen;
	
	public EquationInfSolver(EquationFeatureExtractor featGen) {
		this.featGen=featGen;
	}
	
	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}

	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		Lattice l1 = (Lattice) arg1;
		Lattice l2 = (Lattice) arg2;
		float loss = 0.0f;
		for(int i=0; i<2; ++i) {
			Equation eq1 = l1.equations.get(i);
			Equation eq2 = l2.equations.get(i);
			for(Pair<Operation, Double> pair : eq1.A1) {
				if(!eq2.A1.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq1.A2) {
				if(!eq2.A2.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq1.B1) {
				if(!eq2.B1.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq1.B2) {
				if(!eq2.B2.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq1.C) {
				if(!eq2.C.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.A1) {
				if(!eq1.A1.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.A2) {
				if(!eq1.A2.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.B1) {
				if(!eq1.B1.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.B2) {
				if(!eq1.B2.contains(pair)) loss = loss + 1;
			}
			for(Pair<Operation, Double> pair : eq2.C) {
				if(!eq1.C.contains(pair)) loss = loss + 1;
			}
			for(int j=0; j<5; j++) {
				if(eq1.operations.get(j) != eq2.operations.get(j)) {
					loss = loss + 1;
				}
			}
		}
		return loss;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector wv, IInstance arg1, IStructure arg2) throws Exception {
		Blob blob = (Blob) arg1;
		Lattice gold = (Lattice) arg2;
		List<Operation> operationList = Arrays.asList(
				Operation.ADD, Operation.SUB, Operation.MUL, Operation.DIV, 
				Operation.NONE);
		Map<String, List<QuantSpan>> clusterMap = blob.simulProb.clusterMap;
		List<Pair<Lattice, Double>> tmpLatticeList = 
				new ArrayList<Pair<Lattice, Double>>();
		BoundedPriorityQueue<Pair<Lattice, Double>> beam = 
				new BoundedPriorityQueue<Pair<Lattice, Double>>(50);
		beam.add(new Pair<Lattice, Double>(new Lattice(), 0.0));
		// Enumerate all equations
		for(int i = 0; i < 2; i++) {
			// Transfer states from beam to tmpLatticeList
			Iterator<Pair<Lattice, Double>> it = beam.iterator();
			tmpLatticeList.clear();
			for(;it.hasNext();) {
				tmpLatticeList.add(it.next());
			}
			beam.clear();
			for(QuantSpan qs : clusterMap.get("E1")) {
				for(Pair<Lattice, Double> pair : tmpLatticeList) {
					Lattice tmpLattice = new Lattice(pair.getFirst());
					beam.add(new Pair<>(tmpLattice, pair.getSecond()));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A1.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A1"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A1.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A1"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A2.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A2"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).A2.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "A2"))));
				}
				it = beam.iterator();
				tmpLatticeList.clear();
				for(;it.hasNext();) {
					tmpLatticeList.add(it.next());
				}
				beam.clear();
			}

			for(QuantSpan qs : clusterMap.get("E2")) {
				for(Pair<Lattice, Double> pair : tmpLatticeList) {
					Lattice tmpLattice = new Lattice(pair.getFirst());
					beam.add(new Pair<>(tmpLattice, pair.getSecond()));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B1.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B1"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B1.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B1"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B2.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B2"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).B2.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "B2"))));
				}
				it = beam.iterator();
				tmpLatticeList.clear();
				for(;it.hasNext();) {
					tmpLatticeList.add(it.next());
				}
				beam.clear();
			}
			
			for(QuantSpan qs : clusterMap.get("E3")) {
				for(Pair<Lattice, Double> pair : tmpLatticeList) {
					Lattice tmpLattice = new Lattice(pair.getFirst());
					beam.add(new Pair<>(tmpLattice, pair.getSecond()));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).C.add(new Pair<Operation, Double>(
							Operation.MUL, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "C"))));
					
					tmpLattice = new Lattice(pair.getFirst());
					tmpLattice.equations.get(i).C.add(new Pair<Operation, Double>(
							Operation.DIV, Tools.getValue(qs)));
					beam.add(new Pair<>(tmpLattice, pair.getSecond()+wv.dotProduct(
							featGen.getFeaturesVector(blob, tmpLattice, i, "C"))));
				}
				it = beam.iterator();
				tmpLatticeList.clear();
				for(;it.hasNext();) {
					tmpLatticeList.add(it.next());
				}
				beam.clear();
			}

			for(Pair<Lattice, Double> pair : tmpLatticeList) {
				for(Operation op1 : operationList) {
					for(Operation op2 : operationList) {
						for(Operation op3 : operationList) {
							for(Operation op4 : operationList) {
								for(Operation op5 : operationList) {
									Lattice tmpLattice = new Lattice(pair.getFirst());
									Equation tmpEq = tmpLattice.equations.get(i);
									if(tmpEq.operations.get(1) != Operation.NONE &&
											tmpEq.A2.size() == 0) continue;
									if(tmpEq.operations.get(3) != Operation.NONE &&
											tmpEq.B2.size() == 0) continue;
									if(tmpEq.operations.get(4) != Operation.NONE &&
											tmpEq.C.size() == 0) continue;
									if(op1 == Operation.NONE && op2 == Operation.NONE) {
										continue;
									}
									tmpEq.operations.set(0, op1);
									tmpEq.operations.set(1, op2);
									tmpEq.operations.set(2, op3);
									tmpEq.operations.set(3, op4);
									tmpEq.operations.set(4, op5);
									beam.add(new Pair<Lattice, Double>(
											tmpLattice, 
											pair.getSecond()+wv.dotProduct(
													featGen.getFeaturesVector(
															blob, 
															tmpLattice, 
															i, 
															"Op"))));
								}
							}
						}
					}
				}
			}
			
		}
		return beam.element().getFirst();
	}

}
