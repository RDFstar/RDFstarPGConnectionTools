package se.liu.ida.rdfstar.pgtools.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RiotException;

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
	public void noMetadata() throws IOException
	{
		final Graph g = createGraphFromCSVFiles("vtest1.csv", "etest1.csv", null);

		assertEquals( 6, g.size() );
		checkNestedTriples(g, 0);

	}
	
	@Test
	public void metadata() throws IOException
	{
		final Graph g = createGraphFromCSVFiles("vtest1.csv", "etest2.csv", null);

		assertEquals( 7, g.size() );
		checkNestedTriples(g, 2);
	}
	
	@Test
	public void test3() throws IOException
	{
		final Graph g = createGraphFromCSVFiles("vtest1.csv", "etest3.csv", null);

		assertEquals( 7, g.size() );
		checkNestedTriples(g, 2);
	}
	
	@Test
	public void biggerGraph() throws IOException
	{
		final Graph g = createGraphFromCSVFiles("vtest2.csv", "etest4.csv", null);

		assertEquals( 12, g.size() );
		checkNestedTriples(g, 4);
	}
	
	@Test
	public void differentOrderColumns() throws IOException
	{
		
		final String fullFilenameV = getClass().getResource("/CSVFiles/vtest3.csv").getFile().substring(1);
		final String fullFilenameE = getClass().getResource("/CSVFiles/etest5.csv").getFile().substring(1);
		String fullFilenameP = null;

		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		new PG2RDFStar().convert(fullFilenameV, fullFilenameE, os, fullFilenameP);

		final String result = os.toString();
		System.out.println(result);
		
		boolean thrown = false;

		  try {		
			LangTurtleStarTest.createGraphFromTurtleStarSnippet(result);
		  } catch (RiotException e) {
		    thrown = true;
		  }
		  assertTrue(thrown);
	}
	
	@Test
	public void prefixFileTest() throws IOException 
	{
		final String fullFilenameV = getClass().getResource("/CSVFiles/vtest3.csv").getFile().substring(1);
		final String fullFilenameE = getClass().getResource("/CSVFiles/etest5.csv").getFile().substring(1);
		String fullFilenameP = getClass().getResource("/CSVFiles/prefixfile1.txt").getFile().substring(1);
		
		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		new PG2RDFStar().convert(fullFilenameV, fullFilenameE, os, fullFilenameP);

		final String result = os.toString();
		System.out.println(result);
		
		boolean thrown = false;

		  try {		
			LangTurtleStarTest.createGraphFromTurtleStarSnippet(result);
		  } catch (RiotException e) {
		    thrown = true;
		  }
		  assertTrue(thrown);
		
	}


	//---------helper methods-------------

	protected Graph createGraphFromCSVFiles(String filenameV, String filenameE, String filenamePrefixes) throws IOException
	{
		final String fullFilenameV = getClass().getResource("/CSVFiles/"+filenameV).getFile().substring(1);
		final String fullFilenameE = getClass().getResource("/CSVFiles/"+filenameE).getFile().substring(1);
		final String fullFilenameP;
		if ( filenamePrefixes != null )
			fullFilenameP = getClass().getResource("/CSVFiles/"+filenamePrefixes).getFile().substring(1);
		else
			fullFilenameP = null;

		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		new PG2RDFStar().convert(fullFilenameV, fullFilenameE, os, fullFilenameP);

		final String result = os.toString();

		System.out.println(result);

		final Graph g = LangTurtleStarTest.createGraphFromTurtleStarSnippet(result);
		return g;
	}
	
	protected void checkNestedTriples(Graph g, int estimatedNumberOfNestedTriples) {
		int nestedTriples = 0;
		final Iterator<Triple> iter = g.find();
		
		while (iter.hasNext()) {
			final Triple t = iter.next();
			if (t.getSubject() instanceof Node_Triple) {
				nestedTriples ++;
			}
			assertFalse( t.getPredicate() instanceof Node_Triple );
			assertFalse( t.getObject() instanceof Node_Triple );
		}
		assertEquals(estimatedNumberOfNestedTriples, nestedTriples);
	}
	
}
