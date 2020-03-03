package org.doble.adr;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RecordTest {
	private FileSystem fileSystem;
	private Path docPath = null;
	private DateTimeFormatter dateFormatter;

	@BeforeEach
	public void setUp() throws Exception {
		//Path rootPath = null;

		fileSystem = Jimfs.newFileSystem(Configuration.unix());

		docPath = fileSystem.getPath("/test");

		Files.createDirectory(docPath);

		dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
	}

	@AfterEach
	public void tearDown() throws Exception {
		fileSystem.close();
	}

	@Test
	@Order(1)
	public void test1BasicRecordConstruction() throws Exception {
		String expectedContents = "# 7. This is a new record\n" + 
				"\n" + 
				"Date: {{date}}\n" + 
				"\n" + 
				"## Status\n" + 
				"\n" + 
				"Proposed\n" + 
				"\n\n" + 
				"## Context\n" + 
				"\n" + 
				"Record the architectural decisions made on this project.\n" + 
				"\n" + 
				"## Decision\n" + 
				"\n" + 
				"We will use Architecture Decision Records, as described by Michael Nygard in this article: http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions\n" + 
				"\n" + 
				"## Consequences\n" + 
				"\n" + 
				"See Michael Nygard's article, linked above.";
		
		expectedContents = expectedContents.replace("{{date}}", DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()));

		// Build the record
		Record record = new Record.Builder(docPath, dateFormatter).id(7).name("This is a new record").build();

		record.store();

		// Check if the ADR file has been created
		assertTrue(Files.exists(fileSystem.getPath("/test/0007-this-is-a-new-record.md")));

		// Read in the file
		Path adrFile = fileSystem.getPath("/test/0007-this-is-a-new-record.md");
		Stream<String> lines = Files.lines(adrFile);
		String actualContents = lines.collect(Collectors.joining("\n"));
		lines.close();

        assertEquals(expectedContents, actualContents);

	}



	@Test
	@Order(2)
	public void test2ComplexRecordConstruction() throws Exception {
		LocalDate date = LocalDate.now();
		
		String expectedContents = "# 42. This is a complex record\n" + 
				"\n" + 
				"Date: {{date}}\n" + 
				"\n" + 
				"## Status\n" + 
				"\n" + 
				"Accepted\n" + 
				"\n" + 
				"\n" + 
				"## Context\n" + 
				"\n" + 
				"Record the architectural decisions made on this project.\n" + 
				"\n" + 
				"## Decision\n" + 
				"\n" + 
				"We will use Architecture Decision Records, as described by Michael Nygard in this article: http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions\n" + 
				"\n" + 
				"## Consequences\n" + 
				"\n" + 
				"See Michael Nygard's article, linked above.";
		expectedContents = expectedContents.replace("{{date}}", DateTimeFormatter.ISO_LOCAL_DATE.format(date));
		
		Record record = new Record.Builder(docPath, dateFormatter).id(42)
				.name("This is a complex record")
				.date(date)
				.status("Accepted")
				.build();
		record.store();

		// Check if the ADR file has been created
		assertTrue(Files.exists(fileSystem.getPath("/test/0042-this-is-a-complex-record.md")));

		// Read in the file
		Path adrFile = fileSystem.getPath("/test/0042-this-is-a-complex-record.md");
		Stream<String> lines = Files.lines(adrFile);
		String actualContents = lines.collect(Collectors.joining("\n"));
		lines.close();
	
		assertEquals(expectedContents, actualContents);

	}

	@Test
	@Order(3)
	public void nameIsLowerCased() throws Exception {

		Record record = new Record.Builder(docPath, dateFormatter).id(8).name("CDR is stored in a relational database").build();

		record.store();

		// Check if the ADR file has been created
		assertTrue(Files.exists(fileSystem.getPath("/test/0008-cdr-is-stored-in-a-relational-database.md")));
	}
	
	@Test
	@Order(4)
	public void testLinkConstruction() throws Exception {
		
		// Create some ADR files that are going to be linked to.
		Path adrTestFilePath = docPath.resolve("0004-linked-to.md");
		Files.createFile(adrTestFilePath);
		adrTestFilePath = docPath.resolve("0005-also-linked-to.md");
		Files.createFile(adrTestFilePath);
		
	
		
		Record record = new Record.Builder(docPath, dateFormatter).id(102).name("Contains some links").build();
        record.store();
        
		// <target_adr>:<link_description>
		record.addLink("4:Links to");
		record.addLink("5:Also links to");
				
		record.store();
		
		// Now check that the links have been added
		Path adrFile = fileSystem.getPath("/test/0102-contains-some-links.md");
	    Stream<String> lines = Files.lines(adrFile);
	    
	    //boolean result = lines.anyMatch("* Links to [ADR 4](004-contains-some-links.md)"::equals);
	    String contents = lines.collect(Collectors.joining("\n"));
		lines.close();
		
		assertTrue(contents.contains("* Links to [ADR 4](0004-linked-to.md)"));
		assertTrue(contents.contains("* Also links to [ADR 5](0005-also-linked-to.md)"));
		
// TODO reinstate these tests
//		assertTrue(contents.contains("\n<!--* {{{link.comment= \"Links to\"}}} [ADR {{{link.id=\"4\"}}}]({{{link.file=\"0004-contains-some-links.md\"}}})-->\n"));
//		assertTrue(contents.contains("\n<!--* {{{link.comment= \"Also links to\"}}} [ADR {{{link.id=\"5\"}}}]({{{link.file=\"0005-also-linked-to.md\"}}})-->\n"));
				
				//TODO test reverse links

	}

	@Test
	@Order(5)
	public void testRecordConstructionWithDefaultAuthor() throws Exception {
		String expectedContents = "# 66. This is a new record with default author\n" +
				"\n" +
				"Author: {{author}}";

		expectedContents = expectedContents.replace("{{author}}", System.getProperty("user.name"));

		// Build the record
		Record record = new Record.Builder(docPath, dateFormatter)
				.id(66)
				.name("This is a new record with default author")
				.template("rsrc:template_with_author.md")
				.build();

		record.store();

		// Check if the ADR file has been created
		assertTrue(Files.exists(fileSystem.getPath("/test/0066-this-is-a-new-record-with-default-author.md")));

		// Read in the file
		Path adrFile = fileSystem.getPath("/test/0066-this-is-a-new-record-with-default-author.md");
		Stream<String> lines = Files.lines(adrFile);
		String actualContents = lines.collect(Collectors.joining("\n"));
		lines.close();

		assertEquals(expectedContents, actualContents);

	}

	@Test
	@Order(6)
	public void testRecordConstructionWithGivenAuthor() throws Exception {
		String expectedContents = "# 67. This is a new record with given author\n" +
				"\n" +
				"Author: Andrew Doble";

		// Build the record
		Record record = new Record.Builder(docPath, dateFormatter)
				.id(67)
				.name("This is a new record with given author")
				.author("Andrew Doble")
				.template("rsrc:template_with_author.md")
				.build();

		record.store();

		// Check if the ADR file has been created
		assertTrue(Files.exists(fileSystem.getPath("/test/0067-this-is-a-new-record-with-given-author.md")));

		// Read in the file
		Path adrFile = fileSystem.getPath("/test/0067-this-is-a-new-record-with-given-author.md");

		Stream<String> lines = Files.lines(adrFile);
		String actualContents = lines.collect(Collectors.joining("\n"));
		lines.close();

		assertEquals(expectedContents, actualContents);

	}
	
	@Test
	@Order(7)
	public void testExplicitDateFormatter() throws Exception {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

		String expectedContents = "Date: {{date}}";

		expectedContents = expectedContents.replace("{{date}}", dateFormatter.format(LocalDate.now()));

		// Build the record
		Record record = new Record.Builder(docPath, dateFormatter)
				.id(77)
				.name("Only date")
				.template("rsrc:template_only_date.md")
				.build();

		record.store();

		// Check if the ADR file has been created
		assertTrue(Files.exists(fileSystem.getPath("/test/0077-only-date.md")));

		// Read in the file
		Path adrFile = fileSystem.getPath("/test/0077-only-date.md");
		Stream<String> lines = Files.lines(adrFile);
		String actualContents = lines.collect(Collectors.joining("\n"));
		lines.close();

		assertEquals(expectedContents, actualContents);

	}
		
}
