package com.testify.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.testify.ecfeed.api.GeneratorException;
import com.testify.ecfeed.api.IConstraint;
import com.testify.ecfeed.api.IGenerator;
import com.testify.ecfeed.api.IGeneratorParameter;
import com.testify.generators.algorithms.IAlgorithm;

public class AbstractGenerator<E> implements IGenerator<E> {

	List<IGeneratorParameter> EMPTY_PARAMETER_LIST = new ArrayList<IGeneratorParameter>();
	IAlgorithm<E> fAlgorithm = null;
	
	@Override
	public List<IGeneratorParameter> parameters() {
		return EMPTY_PARAMETER_LIST;
	}

	@Override
	public void initialize(List<? extends List<E>> inputDomain,
			Collection<? extends IConstraint<E>> constraints,
			Map<String, Object> parameters, IProgressMonitor progressMonitor)
			throws GeneratorException {
		fAlgorithm.initialize(inputDomain, constraints, progressMonitor);
	}

	@Override
	public List<E> next() throws GeneratorException {
		return fAlgorithm.getNext();
	}

	@Override
	public void reset(){
		fAlgorithm.reset();
	}
	
	protected void setAlgorithm(IAlgorithm<E> algorithm){
		fAlgorithm = algorithm;
	}

	protected IAlgorithm<E> getAlgorithm(){
		return fAlgorithm;
	}
}
