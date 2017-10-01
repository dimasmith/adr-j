package org.doble.adr;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

public class CommandInitTest {
    private static FileSystem fileSystem;
    final static private String rootPath = "/project/adr";
    
    private Environment env;
 


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {	}

	@Before
	public void setUp() throws Exception {

		// Set up the mock file system
		try {
			fileSystem = Jimfs.newFileSystem(Configuration.unix());

			Path rootPath = fileSystem.getPath("/project");

			Files.createDirectory(rootPath);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Set up the environment
		env = new Environment.Builder(fileSystem)
				.out(System.out)
				.err(System.err)
				.in(System.in)
				.userDir(rootPath)
				.editorCommand("dummyEditor")
				.build();

	}

	@After
	public void tearDown() throws Exception {
		fileSystem.close();
	}

	@Test
	public void testInit() {
		ADR adr = new ADR(env);

		String[] args = {"init"};

		try {
			adr.run(args);
		} catch (ADRException e) {
			fail("ADR Exception raised");
		}


		// Check to see if the .adr directory has been created. 
		String pathName = rootPath + "/.adr";
		try {
			Path p = fileSystem.getPath(pathName);
			boolean exists = Files.exists(p);
			assertTrue(exists);
		} catch (InvalidPathException e) {
			
			fail("InvalidPathException raised on "+ pathName);
		}
		
		//Now see if the  standard docs directory has been created
		pathName = rootPath + "/doc/adr";
		try {
			Path p = fileSystem.getPath(pathName);
			boolean exists = Files.exists(p);
			assertTrue(exists);
		} catch (InvalidPathException e) {
			fail("InvalidPathException raised on "+ pathName);
		}
		
				
		// Check if the ADR has been created
		pathName = rootPath + "/doc/adr/0001-record-architecture-decisions.md";
		try {
			Path p = fileSystem.getPath(pathName);
			boolean exists = Files.exists(p);
			assertTrue(exists);
		} catch (InvalidPathException e) {
			fail("InvalidPathException raised on "+ pathName);
		}
		
		// Do a sample check on the content
		pathName = rootPath + "/doc/adr/0001-record-architecture-decisions.md";
		try {
			Path p = fileSystem.getPath(pathName);
			List<String> contents = Files.readAllLines(p);
			
			// Sample the contents
			int matches = 0; 
			for (String line: contents) {
				if (line.contains("Record architecture decisions")) matches++;
				if (line.contains("## Decision")) matches++;
				if (line.contains("Nygard")) matches++;
			}
			
		
			assertTrue(matches == 4);
		} catch (InvalidPathException e) {
			fail("InvalidPathException raised on "+ pathName);
		} catch (IOException e) {
			fail(e.getMessage() + " reading " +  pathName);
		}
	}
	
	
	@Test
	public void testInitCustomDirectory() {
		String customDir = "myStuff/myDocs/myADRs";
		
		ADR adr = new ADR(env);

		String[] args = {"init", customDir };

		try {
			adr.run(args);
		} catch (ADRException e) {
			fail("ADR Exception raised");
		}
		
		// Check to see if the custom directory has been created. 
		String pathName = rootPath + "/" + customDir;
		try {
			Path p = fileSystem.getPath(pathName);
			boolean exists = Files.exists(p);
			assertTrue(exists);
		} catch (InvalidPathException e) {
			fail("InvalidPathException raised on "+ pathName);
		}
	}
	
	/** Test to see if a re initialization of the directory causes an exception to be raised 
	 * 
	 */
	@Test
	public void testReInit() {
		boolean exceptionRaised = false; 
		ADR adr = new ADR(env);

		// Initialize the ADR directories
		String[] args = {"init"};

		try {
			adr.run(args);
		} catch (ADRException e) {
			fail("ADR Exception raised");
		}
		
		// Re-initialize to see if an exception is raised
		try {
			adr.run(args);
		} catch (ADRException e) {
			exceptionRaised = true;
		}
		
		assertTrue(exceptionRaised);
	
	}
	
	/** Test to see if the init command still goes through even 
	 * if the EDITOR or VISUAL environment variable has not been set. 
	 */
	@Test
	public void testEditorNotSet() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream testErr =  new PrintStream(baos);		
		// Set up the environment with a null editor command and catch the output
		Environment envWithoutEditor = new Environment.Builder(fileSystem)
				.out(env.out)
				.err(testErr)
				.in(env.in)
				.userDir(env.dir)
				.editorCommand(null)
				.build();
		
		
		ADR adr = new ADR(envWithoutEditor);

		String[] args = {"init"};

		try {
			adr.run(args);
			String commandOutput= new String(baos.toByteArray());
			assertTrue("No warning given from init command that edit has not been set.", commandOutput.contains("WARNING"));
		} catch (ADRException e) {
			fail("ADR Exception raised");
		}


		// Check to see if the .adr directory has been created even though an error is there . 
		String pathName = rootPath + "/.adr";
		try {
			Path p = fileSystem.getPath(pathName);
			boolean exists = Files.exists(p);
			assertTrue(exists);
		} catch (InvalidPathException e) {
			
			fail("InvalidPathException raised on "+ pathName);
		}
	}
	

}
