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

package com.testify.ecfeed.test.runner;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.testify.ecfeed.runner.EcFeeder;
import com.testify.ecfeed.runner.annotations.EcModel;
import com.testify.ecfeed.runner.annotations.expected;

@RunWith(EcFeeder.class)
@EcModel("test/com/testify/ecfeed/test/runner/model.ect")
public class SomeClass{
	// This is our function that we want to test. It checks whether a number is divisable by
	// potentialDivisor
	public boolean divisorCheck(int number, int potentialDivisor){
		boolean result = ((number % potentialDivisor) == 0);
		return result;
	}

	@Test
	// This function tests the one above. We will check if for some example values the
	// divisorCheck returns right answer
	public void testDivisorCheck(@expected boolean expectedResult, int number, int potentialDivisor){
		assertEquals(expectedResult, divisorCheck(number, potentialDivisor));
	}
}