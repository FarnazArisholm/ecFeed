package com.testify.ecfeed.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class RootNodeTest extends RootNode {

	public RootNodeTest() {
		super("RootNodeTest");
	}

	@Test
	public void test() {
		RootNode root = new RootNode("root");
		ClassNode klass = new ClassNode("com.test.ClassName");
		assertEquals(false, root.hasChildren());
		assertEquals(0, root.getChildren().size());
		assertEquals(0, root.getClasses().size());
		assertEquals(null, klass.getParent());

		root.addClass(klass);
		assertEquals(true, root.hasChildren());
		assertEquals(1, root.getChildren().size());
		assertEquals(1, root.getClasses().size());
		assertEquals(root, klass.getParent());
		assertEquals(klass, root.getChildren().elementAt(0));

	}
}
