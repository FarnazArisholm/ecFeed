package com.testify.generators.algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.testify.ecfeed.api.GeneratorException;
import com.testify.generators.CartesianProductGenerator;

public class OptimalNWiseAlgorithm<E> extends AbstractNWiseAlgorithm<E>{

	CartesianProductGenerator<E> fCartesianGenerator;
	private int K;
	private Set<List<E>> fGeneratedTuples;
	
	@Override
	public List<E> getNext() throws GeneratorException{
		while(K != 0 && fGeneratedTuples.size() < tuplesToGenerate()){
			List<E> next = cartesianNext();
			if(next == null){
				--K;
				cartesianReset();
				continue;
			}
			Set<List<E>> originalTuples = originalTuples(next); 
			if(originalTuples.size() == K){
				fGeneratedTuples.addAll(originalTuples);
				progressMonitor().worked(originalTuples.size());
				return next;
			}
		}
		progressMonitor().done();
		return null;
	}

	@Override
	public void reset(){
		super.reset();
		K = maxTuples(getInput(), N);
		fGeneratedTuples = new HashSet<List<E>>();
	}
	

	private Set<List<E>> originalTuples(List<E> next) {
		Set<List<E>> originalTuples = getTuples(next);
		originalTuples.removeAll(fGeneratedTuples);
		return originalTuples;
	}

}