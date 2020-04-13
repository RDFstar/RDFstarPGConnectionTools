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

import se.liu.ida.rdfstar.tools.graph.RDFStarUtils;


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
		final Graph g = createGraphFromCSVFiles("test.csv", "etest.csv", null);

	}


	//---------helper methods-------------

	protected Graph createGraphFromCSVFiles(String filenameV, String filenameE, String filenamePrefixes) throws IOException
	{
		final String fullFilenameV = getClass().getResource("/CSVFiles/"+filenameV).getFile();
		final String fullFilenameE = getClass().getResource("/CSVFiles/"+filenameE).getFile();

		final String fullFilenameP;
		if ( filenamePrefixes != null )
			fullFilenameP = getClass().getResource("/CSVFiles/"+filenamePrefixes).getFile();
		else
			fullFilenameP = null;

		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		new PG2RDFStar().convert(fullFilenameV, fullFilenameE, os, fullFilenameP);

		final String result = os.toString();

		System.out.println(result);

		final Graph g = RDFStarUtils.createGraphFromTurtleStarSnippet(result);
		return g;
	}
	
}
