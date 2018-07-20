package se.liu.ida.rdfstar.pgtools.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Graph;

import se.liu.ida.rdfstar.tools.parser.lang.LangTurtleStarTest;


/**
 * 
 * @author Olaf Hartig
 * @author Ebba Lindström
 */
public class PG2RDFStarTest
{
	@Before
	public void setup() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void todoWriteTests() throws IOException
	{
		createGraphFromCSVFiles("test.csv", "etest.csv");

	}

//---------helper methods-------------
	
	protected Graph createGraphFromCSVFiles(String filenameV, String filenameE) throws IOException {
		
		final String fullFilenameV = getClass().getResource("/CSVFiles/"+filenameV).getFile();
		final String fullFilenameE = getClass().getResource("/CSVFiles/"+filenameE).getFile();
		
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		new PG2RDFStar().convert(fullFilenameV, fullFilenameE, os, null);
		
		String result = os.toString();
		
		System.out.println(result);
		
		Graph g = LangTurtleStarTest.createGraphFromTurtleStarSnippet(result);
		
		return g;
	
	}
	
}
