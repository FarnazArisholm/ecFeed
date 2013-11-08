package com.testify.ecfeed.runner;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.testify.ecfeed.api.GeneratorException;
import com.testify.ecfeed.api.IConstraint;
import com.testify.ecfeed.api.IGenerator;
import com.testify.ecfeed.api.IGeneratorParameter;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.runner.annotations.Constraints;
import com.testify.ecfeed.runner.annotations.Generator;
import com.testify.ecfeed.runner.annotations.GeneratorParameter;

public class OnlineRunner extends StaticRunner {

	public OnlineRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}
	
	@Override
	protected List<FrameworkMethod> generateTestMethods() throws RunnerException {
		List<FrameworkMethod> methods = new ArrayList<FrameworkMethod>();
		for(FrameworkMethod method : getTestClass().getAnnotatedMethods(Test.class)){
			if(method.getMethod().getParameterTypes().length == 0){
				//standard jUnit test
				methods.add(method);
			} else{
				MethodNode methodModel = getMethodModel(getModel(), method);
				if(methodModel == null){
					continue;
				}
				IGenerator<PartitionNode> generator = getGenerator(method);
				List<List<PartitionNode>> input = getInput(methodModel);
				Collection<IConstraint<PartitionNode>> constraints = getConstraints(method, methodModel);
				Map<String, Object> parameters = getGeneratorParameters(generator, method);
				try {
					generator.initialize(input, constraints, parameters, null);
				} catch (GeneratorException e) {
					throw new RunnerException("Generator initializetion problem: " + e.getMessage());
				}
				methods.add(new RuntimeMethod(method.getMethod(), generator));
			}
		}
		return methods;
	}

	private Map<String, Object> getGeneratorParameters(
			IGenerator<PartitionNode> generator, FrameworkMethod method) throws RunnerException {
		List<IGeneratorParameter> parameters = generator.parameters();
		Map<String, Object> result = new HashMap<String, Object>();
		for(IGeneratorParameter parameter : parameters){
			Object value = getParameterValue(parameter, method);
			if(value == null && parameter.isRequired()){
				throw new RunnerException(Messages.MISSING_REQUIRED_PARAMETER(parameter.getName()));
			}
			else if(value != null){
				result.put(parameter.getName(), value);
			}
		}
		return result;
	}

	private Object getParameterValue(IGeneratorParameter parameter,
			FrameworkMethod method) throws RunnerException {
		String rawValue = getParameterValue(parameter.getName(), method.getAnnotations());
		if(rawValue == null){
			rawValue = getParameterValue(parameter.getName(), getTestClass().getAnnotations());
		}
		if(rawValue != null){
			try{
				switch (parameter.getType()) {
				case BOOLEAN:
					return Boolean.parseBoolean(rawValue);
				case FLOAT:
					return Double.parseDouble(rawValue);
				case INTEGER:
					return Integer.parseInt(rawValue);
				case STRING:
					return (String)rawValue;
				}
			}
			catch(Throwable e){
				throw new RunnerException(Messages.WRONG_PARAMETER_TYPE(parameter.getName(), e.getMessage()));
			}
		}
		return null;
	}

	private String getParameterValue(String name, Annotation[] annotations) {
		for(Annotation annotation : annotations){
			if(annotation instanceof GeneratorParameter){
				GeneratorParameter parameterAnnotation = (GeneratorParameter)annotation;
				if(parameterAnnotation.name().equals(name)){
					return parameterAnnotation.value();
				}
			}
		}
		return null;
	}

	protected Collection<IConstraint<PartitionNode>> getConstraints(
			FrameworkMethod method, MethodNode methodModel) {
		Collection<String> constraintsNames = constraintsNames(method);
		if(constraintsNames == null){
			return null;
		}
		if(constraintsNames.contains(Constraints.ALL)){
			constraintsNames = methodModel.getConstraintsNames();
		}
		else if(constraintsNames.contains(Constraints.NONE)){
			constraintsNames.clear();
		}
		
		Collection<IConstraint<PartitionNode>> constraints = new HashSet<IConstraint<PartitionNode>>();
		for(String name : constraintsNames){
			constraints.addAll(methodModel.getConstraints(name));
		}
		return constraints;
	}

	protected List<List<PartitionNode>> getInput(MethodNode methodModel) {
		// TODO Auto-generated method stub
		return null;
	}

	protected IGenerator<PartitionNode> getGenerator(FrameworkMethod method) throws RunnerException {
		IGenerator<PartitionNode> generator = getGenerator(method.getAnnotations());
		if(generator == null){
			generator = getGenerator(getTestClass().getAnnotations());
		}
		if(generator == null){
			throw new RunnerException(Messages.NO_VALID_GENERATOR(method.getName()));
		}
		return generator;
	}

	private IGenerator<PartitionNode> getGenerator(Annotation[] annotations) throws RunnerException{
		IGenerator<PartitionNode> generator = null;
		for(Annotation annotation : annotations){
			if(annotation instanceof Generator){
				try {
					Class<IGenerator<PartitionNode>> generatorClass = ((Generator)annotation).value();  
					generator = generatorClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RunnerException("Cannot instantiate generator: " + e.getMessage());
				}
			}
		}
		return generator;
	}

	protected Set<String> constraintsNames(FrameworkMethod method) {
		Set<String> names = constraintsNames(method.getAnnotations());
		if(names == null){
			names = constraintsNames(getTestClass().getAnnotations());
		}
		return names;
	}

	private Set<String> constraintsNames(Annotation[] annotations) {
		for(Annotation annotation : annotations){
			if(annotation instanceof Constraints){
				String[] constraints = ((Constraints)annotation).value();
				return new HashSet<String>(Arrays.asList(constraints));
			}
		}
		return null;
	}
}