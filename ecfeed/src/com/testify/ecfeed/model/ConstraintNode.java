/*******************************************************************************
 * Copyright (c) 2013 Testify AS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)gmail.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.model;

import java.util.List;

import com.testify.ecfeed.model.constraint.Constraint;

public class ConstraintNode extends GenericNode{

	private Constraint fConstraint;

	public ConstraintNode(String name, Constraint constraint) {
		super(name);
		fConstraint = constraint;
	}
	
	public Constraint getConstraint(){
		return fConstraint;
	}
	
	@Override
	public String toString(){
		return getName() + ": " + getConstraint().toString();
	}
	
	public MethodNode getMethod() {
		if(getParent() != null && getParent() instanceof MethodNode){
			return (MethodNode)getParent();
		}
		return null;
	}

	public boolean evaluate(List<PartitionNode> values) {
		if(fConstraint != null){
			return fConstraint.evaluate(values);
		}
		return false;
	}

	public boolean mentions(PartitionNode partition) {
		if(fConstraint.mentions(partition)){
			return true;
		}
		return false;
	}

	public boolean mentions(CategoryNode category) {
		if(fConstraint.mentions(category)){
			return true;
		}
		return false;
	}
}
