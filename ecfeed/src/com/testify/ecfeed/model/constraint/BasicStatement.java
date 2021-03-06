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

package com.testify.ecfeed.model.constraint;

import java.util.List;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.PartitionNode;

public class BasicStatement implements IStatement {
	BasicStatement fParent = null;
	private static int fLastId = 0;
	private final int fId;
	
	public BasicStatement(){
		fId = fLastId++;
	}
	
	@Override
	public boolean evaluate(List<PartitionNode> values) {
		return false;
	}

	public int getId(){
		return fId;
	}
	
	public BasicStatement getParent() {
		return fParent;
	}

	void setParent(BasicStatement parent) {
		fParent = parent;
	}

	public List<BasicStatement> getChildren(){
		return null;
	}

	public void replaceChild(BasicStatement oldStatement, 
			BasicStatement newStatement) {
		List<BasicStatement> children = getChildren();
		if(children != null){
			int index = children.indexOf(oldStatement);
			if(index != -1){
				newStatement.setParent(this);
				children.set(index, newStatement);
			}
		}
	}

	public void removeChild(BasicStatement child) {
		List<BasicStatement> children = getChildren();
		if(children != null){
			children.remove(child);
		}
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof BasicStatement == false){
			return false;
		}
		return fId == ((BasicStatement)obj).getId();
	}

	public boolean mentions(PartitionNode partition) {
		return false;
	}
	
	public void addStatement(BasicStatement statement){
		if(getParent() != null){
			getParent().addStatement(statement);
		}
	}

	public boolean mentions(CategoryNode category) {
		return false;
	}
}
