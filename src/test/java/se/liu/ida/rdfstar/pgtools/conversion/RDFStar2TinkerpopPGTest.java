package se.liu.ida.rdfstar.pgtools.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.tinkerpop.gremlin.structure.Graph;
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

		// TODO: check whether the resulting graph looks like expected
		// ...
	}


	//---------helper methods-------------

	protected Graph convertTTL2PG( String filename )
	{
		final String fullFilename = getClass().getResource("/TurtleStar/"+filename).getFile();
		return new RDFStar2TinkerpopPG().convert(fullFilename);
	}

}
