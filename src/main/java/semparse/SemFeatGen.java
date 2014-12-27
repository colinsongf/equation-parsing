package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import utils.FeatGen;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class SemFeatGen extends AbstractFeatureGenerator implements
		Serializable {
	private static final long serialVersionUID = 1810851154558168679L;
	public Lexiconer lm = null;

	public SemFeatGen(Lexiconer lm) {
		this.lm = lm;
	}
	
	@Override
	public IFeatureVector getFeatureVector(IInstance arg0, IStructure arg1) {
		SemX x = (SemX) arg0;
		SemY y = (SemY) arg1;
		List<String> features = new ArrayList<>();
		features.addAll(getFeatures(x, y));
		return FeatGen.getFeatureVectorFromList(features, lm);
	}

	public IFeatureVector getFeatureVector(SemX x, SemY y) {
		List<String> feats = getFeatures(x, y);
		return FeatGen.getFeatureVectorFromList(feats, lm);
	}
	
	public List<String> getFeatures(SemX x, SemY y) {
		List<String> features = new ArrayList<>();
		for(IntPair slot : y.emptySlots) {
			features.addAll(alignmentFeatures(x, y, slot));
		}
		return features;
	}

	public List<String> alignmentFeatures(SemX x, SemY y, IntPair slot) {
		List<String> features = new ArrayList<>();
		Double d = y.terms.get(slot.getFirst()).get(slot.getSecond()).getSecond();
		List<IntPair> quantSpans = Tools.getRelevantSpans(d, x.relationQuantities);
		int tokenId = x.ta.getTokenIdFromCharacterOffset(quantSpans.get(0).getFirst());
		Sentence sent = x.ta.getSentenceFromToken(tokenId);
		List<Constituent> sentLemmas = FeatGen.partialLemmas(
				x.lemmas, sent.getStartSpan(), sent.getEndSpan());
		List<Pair<String, IntPair>> sentSkeleton = FeatGen.getPartialSkeleton(
				x.skeleton, sent.getStartSpan(), sent.getEndSpan());
		String prefix;
		if(slot.getFirst() == 4) prefix = ""+slot.getFirst();
		else if(slot.getFirst() == 1 || slot.getFirst() == 3){
			prefix = ""+y.operations.get(slot.getFirst())+"_AB2";
		} else {
			prefix = ""+y.operations.get(slot.getFirst())+"_AB1";
		}
		for(String feature : FeatGen.neighboringSkeletonTokens(sentSkeleton, tokenId, 3)) {
			features.add(prefix+"_"+feature);
		}
		for(int i=0; i<sentSkeleton.size(); ++i) {
			features.add(prefix+"_SentUnigram_"+sentSkeleton.get(i).getFirst());
		}
		for(int i=0; i<sentSkeleton.size()-1; ++i) {
			features.add(prefix+"_SentBigram_"+sentSkeleton.get(i).getFirst()
					+"_"+sentSkeleton.get(i+1).getFirst());
		}
		return features;
	}
}