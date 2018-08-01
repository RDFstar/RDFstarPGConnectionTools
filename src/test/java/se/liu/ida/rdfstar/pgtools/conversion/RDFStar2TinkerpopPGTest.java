package se.liu.ida.rdfstar.pgtools.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Olaf Hartig
 * @author Ebba Lindström
 */

public class RDFStar2TinkerpopPGTest
{
	@Before
	public void setup() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void nothingNested()
	{
		final Graph g = convertTTL2PG("nothingnested.ttls");
		assert(IteratorUtils.count(g.vertices()) == 2);
		assert(IteratorUtils.count(g.edges()) == 1);

	}
	
	@Test
	public void nestedSubject()
	{
		final Graph g = convertTTL2PG("nestedsubject.ttls");
		assert(IteratorUtils.count(g.vertices()) == 2);
		assert(IteratorUtils.count(g.edges()) == 1);

	}
	
	@Test
	public void sameVertices()
	{
		final Graph g = convertTTL2PG("samevertices.ttls");
		assert(IteratorUtils.count(g.vertices()) == 3);
		assert(IteratorUtils.count(g.edges()) == 2);

	}
	
	@Test
	public void onenestedandonenotnested()
	{
		final Graph g = convertTTL2PG("onenestedandonenotnested.ttls");
		assert(IteratorUtils.count(g.vertices()) == 4);
		assert(IteratorUtils.count(g.edges()) == 2);

	}
	
	@Test
	public void nestedobject() throws IOException
	{
		 boolean thrown = false;

		  try {
			  final Graph g = convertTTL2PG("nestedobject.ttls");
		  } catch (IllegalArgumentException e) {
		    thrown = true;
		  }

		  assertTrue(thrown);
	}
	
	@Test
	public void doublenestedsubject() throws IOException
	{
		 boolean thrown = false;

		  try {
			  final Graph g = convertTTL2PG("doublenestedsubject.ttls");
		  } catch (IllegalArgumentException e) {
		    thrown = true;
		  }

		  assertTrue(thrown);
	}


	//---------helper methods-------------

	protected Graph convertTTL2PG( String filename )
	{
		final String fullFilename = getClass().getResource("/TurtleStar/"+filename).getFile().substring(1);
		Graph g = new RDFStar2TinkerpopPG().convert(fullFilename);
		return g;
	}

}
