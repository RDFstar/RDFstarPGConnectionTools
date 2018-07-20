package se.liu.ida.rdfstar.pgtools.conversion;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.util.iterator.ExtendedIterator;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * 
 * @author Olaf Hartig
 * @author Ebba Lindström
 */
public class RDFStar2TinkerpopPG
{
	public Graph convert(String filename)
	{
		final PipedRDFIterator<Triple> it = new PipedRDFIterator<>(16000);
	    final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(it);
		RDFParser.create().source(filename)
					      .checking(false)
					      .build()
					      .parse(inputStream);

		final org.apache.tinkerpop.gremlin.structure.Graph result = convert(it);
		it.close();

		return result; 
	}

	public Graph convert(org.apache.jena.graph.Graph jenaRDFStarGraph)
	{
		final ExtendedIterator<Triple> it = jenaRDFStarGraph.find();

		final org.apache.tinkerpop.gremlin.structure.Graph result = convert(it);
		it.close();

		return result; 
	}

	public Graph convert(Iterator<Triple> input)
	{
		final Graph pg = TinkerGraph.open();

		// TODO: populate the graph by iterating over the input

		return pg;
	}

}
