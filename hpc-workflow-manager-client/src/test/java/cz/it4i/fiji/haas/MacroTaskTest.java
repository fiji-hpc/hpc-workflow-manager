
package cz.it4i.fiji.haas;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cz.it4i.fiji.hpc_workflow.core.MacroTask;

public class MacroTaskTest {

	public MacroTaskTest() {
		// Nothing to do here.
	}
	
	@Test
	public void setAndGetDescriptionTest() {
		MacroTask macroTask = new MacroTask("Sample task");

		// Get description task
		String description = macroTask.getDescription();
		assertEquals("Sample task", description);
	}

	@Test
	public void setAndGetProgressTest() {
		MacroTask macroTask = new MacroTask("Sample task");

		// This should set the progress for task of node id 0 to 50%:
		macroTask.setProgress(0, 50);
		assertEquals(50, macroTask.getProgress(0));

		// This should return -2 as the node 1 does not exist:
		assertEquals(-2, macroTask.getProgress(1));

		// Set progress for node 1 to 60%, this should work and getting the progress
		// should return 60:
		macroTask.setProgress(1, 60);
		assertEquals(60, macroTask.getProgress(1));

		// Set the progress for a non-consecutive node (for example 55) that does
		// not exist to 70%, it should work and return 70.
		macroTask.setProgress(55, 70);
		assertEquals(70, macroTask.getProgress(55));

		// Node 45 which is in between nodes 1 and 55 that exist should exist and
		// it's progress should be -2.
		assertEquals(-2, macroTask.getProgress(45));
	}
	
	@Test
	public void progressCorrectness() {
		MacroTask macroTask = new MacroTask("Sample Task");
		
		// Progress should never decrease: 
		macroTask.setProgress(0, 95);
		macroTask.setProgress(0, 80);
		
		assertEquals(95, macroTask.getProgress(0));
		
		// Progress should never be less than -1:
		macroTask.setProgress(0, -200);
		assertEquals(true, macroTask.getProgress(0) >= -1);
		
		// Progress should increase:
		macroTask.setProgress(0, 100);
		assertEquals(100, macroTask.getProgress(0));
		
		// Progress should not exceed 100%:
		macroTask.setProgress(0, 150);
		assertEquals(100, macroTask.getProgress(0));
	}
}
