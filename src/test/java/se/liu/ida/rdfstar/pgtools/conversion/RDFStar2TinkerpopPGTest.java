package se.liu.ida.rdfstar.pgtools.conversion;

import static org.junit.Assert.*;

import java.awt.List;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
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
		
		//check for correct amount of edges and vertices
		assert(IteratorUtils.count(g.vertices()) == 2);
		assert(IteratorUtils.count(g.edges()) == 1);
		
		GraphTraversalSource traversalG = g.traversal();
		//check for correct properties for vertices
		assertTrue(traversalG.V("v1").has("URI", "http://example.com/coolio").hasNext());
		assertTrue(traversalG.V("v2").has("URI", "http://example.com/cal").hasNext());
		
		//check for correct edges
		boolean hasOutEdge = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/knows").hasNext();
		assertTrue(hasOutEdge);
		boolean hasInEdge = traversalG.V("v2").inE("http://xmlns.com/foaf/0.1/knows").hasNext();
		assertTrue(hasInEdge);

	}
	
	@Test
	public void nestedSubject()
	{
		final Graph g = convertTTL2PG("nestedsubject.ttls");
		assert(IteratorUtils.count(g.vertices()) == 2);
		assert(IteratorUtils.count(g.edges()) == 1);
		
		GraphTraversalSource traversalG = g.traversal();
		//check for correct properties for vertices
		assertTrue(traversalG.V("v1").has("URI", "http://nuda.se/tja").hasNext());
		assertTrue(traversalG.V("v2").has("URI", "http://example.com/bob").hasNext());
		
		//check for correct edges
		boolean hasOutEdge = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/age").hasNext();
		
		assertTrue(hasOutEdge);
		boolean hasInEdge = traversalG.V("v2").inE("http://xmlns.com/foaf/0.1/age").hasNext();
		assertTrue(hasInEdge);
		
		//check for correct egde properties
		
		Optional<Edge> e = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/age").tryNext();
		Edge edge = e.get();
		Property<Object> key = edge.properties("http://purl.org/dc/terms/source").next();
		assertEquals(key.value(), "http://example.net/listing.html");

	}
	
	@Test
	public void sameVertices()
	{
		final Graph g = convertTTL2PG("samevertices.ttls");
		assert(IteratorUtils.count(g.vertices()) == 3);
		assert(IteratorUtils.count(g.edges()) == 2);
		
		GraphTraversalSource traversalG = g.traversal();
		//check for correct properties for vertices
		assertTrue(traversalG.V("v1").has("URI", "http://example.com/jesper").hasNext());
		assertTrue(traversalG.V("v2").has("Literal", "Jesper Eriksson").hasNext());
		assertTrue(traversalG.V("v3").has("Literal", "25").hasNext());
		
		//check for correct edges
		boolean hasOutEdge1 = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/age").hasNext();
		boolean hasOutEdge2 = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/name").hasNext();
		assertTrue(hasOutEdge1);
		assertTrue(hasOutEdge2);
		
		boolean hasInEdge1 = traversalG.V("v2").inE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasInEdge2 = traversalG.V("v3").inE("http://xmlns.com/foaf/0.1/age").hasNext();
		assertTrue(hasInEdge1);
		assertTrue(hasInEdge2);

	}
	
	@Test
	public void sameObject() {
		final Graph g = convertTTL2PG("sameobject.ttls");
		assert(IteratorUtils.count(g.vertices()) == 6);
		assert(IteratorUtils.count(g.edges()) == 5);
		
		GraphTraversalSource traversalG = g.traversal();
		//check for correct properties for vertices
		assertTrue(traversalG.V("v1").has("URI", "http://example.com/alice").hasNext());
		assertTrue(traversalG.V("v2").has("Literal", "Alice the queen").hasNext());
		assertTrue(traversalG.V("v3").has("URI", "http://example.com/amir").hasNext());
		assertTrue(traversalG.V("v4").has("Literal", "Amir Hakim").hasNext());
		assertTrue(traversalG.V("v5").has("URI", "http://example.com/jesper").hasNext());
		assertTrue(traversalG.V("v6").has("Literal", "Jesper Eriksson").hasNext());
		
		//check for correct edges
		boolean hasOutEdge1 = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasOutEdge2 = traversalG.V("v5").outE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasOutEdge3 = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/knows").hasNext();
		boolean hasOutEdge4 = traversalG.V("v3").outE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasOutEdge5 = traversalG.V("v3").outE("http://xmlns.com/foaf/0.1/knows").hasNext();
		assertTrue(hasOutEdge1);
		assertTrue(hasOutEdge2);
		assertTrue(hasOutEdge3);
		assertTrue(hasOutEdge4);
		assertTrue(hasOutEdge5);
		
		boolean hasInEdge1 = traversalG.V("v2").inE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasInEdge2 = traversalG.V("v4").inE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasInEdge3 = traversalG.V("v6").inE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasInEdge4 = traversalG.V("v5").inE("http://xmlns.com/foaf/0.1/knows").hasNext();
		boolean hasInEdge5 = traversalG.V("v5").inE("http://xmlns.com/foaf/0.1/knows").hasNext();
		assertTrue(hasInEdge1);
		assertTrue(hasInEdge2);
		assertTrue(hasInEdge3);
		assertTrue(hasInEdge4);
		assertTrue(hasInEdge5);
	}
	
	@Test
	public void sameObjectMeta() {
		final Graph g = convertTTL2PG("sameobjectmeta.ttls");
		assert(IteratorUtils.count(g.vertices()) == 6);
		assert(IteratorUtils.count(g.edges()) == 5);
		
		GraphTraversalSource traversalG = g.traversal();
		//check for correct properties for vertices
		assertTrue(traversalG.V("v1").has("URI", "http://example.com/alice").hasNext());
		assertTrue(traversalG.V("v2").has("Literal", "Alice the queen").hasNext());
		assertTrue(traversalG.V("v3").has("URI", "http://example.com/amir").hasNext());
		assertTrue(traversalG.V("v4").has("Literal", "Amir Hakim").hasNext());
		assertTrue(traversalG.V("v5").has("URI", "http://example.com/jesper").hasNext());
		assertTrue(traversalG.V("v6").has("Literal", "Jesper Eriksson").hasNext());
		
		//check for correct edges
		boolean hasOutEdge1 = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasOutEdge2 = traversalG.V("v5").outE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasOutEdge3 = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/knows").hasNext();
		boolean hasOutEdge4 = traversalG.V("v3").outE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasOutEdge5 = traversalG.V("v3").outE("http://xmlns.com/foaf/0.1/knows").hasNext();
		assertTrue(hasOutEdge1);
		assertTrue(hasOutEdge2);
		assertTrue(hasOutEdge3);
		assertTrue(hasOutEdge4);
		assertTrue(hasOutEdge5);
		
		boolean hasInEdge1 = traversalG.V("v2").inE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasInEdge2 = traversalG.V("v4").inE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasInEdge3 = traversalG.V("v6").inE("http://xmlns.com/foaf/0.1/name").hasNext();
		boolean hasInEdge4 = traversalG.V("v5").inE("http://xmlns.com/foaf/0.1/knows").hasNext();
		boolean hasInEdge5 = traversalG.V("v5").inE("http://xmlns.com/foaf/0.1/knows").hasNext();
		assertTrue(hasInEdge1);
		assertTrue(hasInEdge2);
		assertTrue(hasInEdge3);
		assertTrue(hasInEdge4);
		assertTrue(hasInEdge5);
		
		//check for correct egde properties
		Optional<Edge> e = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/knows").tryNext();
		Edge edge = e.get();
		Property<Object> key = edge.properties("http://purl.org/dc/terms/source").next();
		System.out.println(key);
		assertEquals(key.value(), "http://example.net/listing.html");
	}
	
	@Test
	public void oneSimpleAndTwoMetaSameEdge() {
		final Graph g = convertTTL2PG("onesimpleandtwometasameedge.ttls");
		assert(IteratorUtils.count(g.vertices()) == 2);
		assert(IteratorUtils.count(g.edges()) == 1);
		
		GraphTraversalSource traversalG = g.traversal();
		//check for correct properties for vertices
		assertTrue(traversalG.V("v1").has("URI", "http://example.com/coolio").hasNext());
		assertTrue(traversalG.V("v2").has("URI", "http://example.com/cal").hasNext());
		
		//check for correct edges
		boolean hasOutEdge1 = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/knows").hasNext();
		assertTrue(hasOutEdge1);

		
		boolean hasInEdge1 = traversalG.V("v2").inE("http://xmlns.com/foaf/0.1/knows").hasNext();
		assertTrue(hasInEdge1);

		
		//check for correct egde properties
		Optional<Edge> e = traversalG.V("v1").outE("http://xmlns.com/foaf/0.1/knows").tryNext();
		Edge edge = e.get();
		System.out.println(edge);
		Property<Object> key = edge.properties("http://purl.org/dc/terms/certainty").next();
		System.out.println(key);
		assertEquals(key.value(), "0.8");
		Property<Object> key2 = edge.properties("http://purl.org/dc/terms/source").next();
		System.out.println(key2);
		assertEquals(key2.value(), "http://example.net/listing.html");
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
