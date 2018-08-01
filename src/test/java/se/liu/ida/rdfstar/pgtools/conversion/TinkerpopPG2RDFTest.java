package se.liu.ida.rdfstar.pgtools.conversion;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TinkerpopPG2RDFTest {

	@Before
	public void setup() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void simpleVertex()
	{
		//create tinkerpop-graph
		org.apache.tinkerpop.gremlin.structure.Graph pg1 = TinkerGraph.open();		
		pg1.addVertex(T.label, "bob", "name", "Bob", "age", 22);
		
		//convert
		org.apache.jena.graph.Graph jg1 = new TinkerpopPG2RDFStar().convert(pg1);
		System.out.println(jg1);
		
		assertEquals( 2, jg1.size() );

        final Triple t1 = jg1.find().next();
        assertFalse( t1.getSubject() instanceof Node_Triple );
        assertFalse( t1.getPredicate() instanceof Node_Triple );
        assertFalse( t1.getObject() instanceof Node_Triple );
	}
	
	@Test
	public void simpleEdge()
	{
		//create tinkerpop-graph
		org.apache.tinkerpop.gremlin.structure.Graph pg2 = TinkerGraph.open();		
		Vertex v1 = pg2.addVertex(T.label, "bob", "name", "Bob", "age", "22");
		Vertex v2 = pg2.addVertex(T.label, "alice", "name", "Alice", "age", "25");
		v1.addEdge("knows", v2);
		
		//convert
		org.apache.jena.graph.Graph jg2 = new TinkerpopPG2RDFStar().convert(pg2);
		System.out.println(jg2);
		
		assertEquals( 5, jg2.size() );
		
        final Triple t1 = jg2.find().next();
        String s1 = t1.getSubject().getBlankNodeLabel();
        System.out.println(s1);
        assertFalse( t1.getSubject() instanceof Node_Triple );
        assertFalse( t1.getPredicate() instanceof Node_Triple );
        assertFalse( t1.getObject() instanceof Node_Triple );
        
        final Triple t2 = jg2.find().next();
        assertFalse( t2.getSubject() instanceof Node_Triple );
        assertFalse( t2.getPredicate() instanceof Node_Triple );
        assertFalse( t2.getObject() instanceof Node_Triple );
        
        final Triple t3 = jg2.find().next();
        assertEquals( t3.getSubject().getBlankNodeId().toString(), s1);
		
	}
	
	@Test
	public void metadataEdge()
	{
		//create tinkerpop-graph
		org.apache.tinkerpop.gremlin.structure.Graph pg3 = TinkerGraph.open();		
		Vertex v1 = pg3.addVertex(T.label, "bob", "name", "Bob", "age", "22");
		Vertex v2 = pg3.addVertex(T.label, "alice", "name", "Alice", "age", "25");
		v1.addEdge("knows", v2, "certainty", 0.8);
		
		//convert
		org.apache.jena.graph.Graph jg3 = new TinkerpopPG2RDFStar().convert(pg3);
		System.out.println(jg3);
		
		assertEquals( 5, jg3.size() );

	}
	
	@Test
	public void twoEdgesTwoVertices()
	{
		//create tinkerpop-graph
		org.apache.tinkerpop.gremlin.structure.Graph pg4 = TinkerGraph.open();		
		Vertex v1 = pg4.addVertex(T.label, "bob", "name", "Bob", "age", "22");
		Vertex v2 = pg4.addVertex(T.label, "alice", "name", "Alice", "age", "25");
		v2.addEdge("mentioned", v1);
		v1.addEdge("influencedBy", v2, "certainty", 0.8);
		
		//convert
		org.apache.jena.graph.Graph jg4 = new TinkerpopPG2RDFStar().convert(pg4);
		System.out.println(jg4);
		
		assertEquals( 6, jg4.size() );
	}
	
	@Test
	public void twoEdgesThreeVertices()
	{
		//create tinkerpop-graph
		org.apache.tinkerpop.gremlin.structure.Graph pg4 = TinkerGraph.open();		
		Vertex v1 = pg4.addVertex(T.label, "bob", "name", "Bob", "age", "22");
		Vertex v2 = pg4.addVertex(T.label, "alice", "name", "Alice", "age", "25");
		Vertex v3 = pg4.addVertex(T.label, "ebba", "name", "Ebba", "age", "21");
		v1.addEdge("knows", v2);
		v1.addEdge("knows", v3, "certainty", 0.8);
		
		//convert
		org.apache.jena.graph.Graph jg4 = new TinkerpopPG2RDFStar().convert(pg4);
		System.out.println(jg4);
		
		assertEquals( 8, jg4.size() );
	}
	
}
