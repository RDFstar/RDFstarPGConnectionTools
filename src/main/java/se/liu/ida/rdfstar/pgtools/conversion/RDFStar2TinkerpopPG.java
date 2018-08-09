package se.liu.ida.rdfstar.pgtools.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import se.liu.ida.rdfstar.tools.parser.lang.LangTurtleStar;

/**
 * 
 * @author Olaf Hartig
 * @author Ebba Lindström
 */
public class RDFStar2TinkerpopPG
{
	protected long vertexId;
	protected long edgeId;
	protected ArrayList<Edge> edgeList = new ArrayList<Edge>();
	
	protected static final String ID = "ID";
	protected static final String KIND = "Kind";
	protected static final String LITERAL = "Literal";
	protected static final String URI = "URI";
	protected static final String BLANK = "Blank";
	
	protected Graph pg;
	
	public Graph convert(String filename)
	{
		final PipedRDFIterator<Triple> it = new PipedRDFIterator<>(16000);
	    final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(it);
		RDFParser.create().source(filename)
					      .checking(false)
					      .lang(LangTurtleStar.TURTLESTAR)
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
		pg = TinkerGraph.open();

		vertexId = 0L;
		edgeId   = 0L;
		
		//Stream triples and process one by one.
		while (input.hasNext()) {	
			Triple next = input.next();
        	
			Node s = next.getSubject();
			Node p = next.getPredicate();
	        Node o = next.getObject();
	        Vertex v1 = null;
	        Vertex v2 = null;

	        if( o instanceof Node_Triple )
	        {
	        	throw new IllegalArgumentException("Nested triple not allowed as object");
	        }
	        
	        //metadata triple
	        else if (s instanceof Node_Triple) {
	        	Triple subTriple = ((Node_Triple)s).get();
	        	Node metaS = subTriple.getSubject();
				Node metaP = subTriple.getPredicate();
		        Node metaO = subTriple.getObject();

		        
		        if( metaS instanceof Node_Triple ||  metaP instanceof Node_Triple ||  metaO instanceof Node_Triple)
		        {
		        	throw new IllegalArgumentException("Nested triple within nested subject not allowed");
		        }
		        
		      //checks if subject is blank or uri, and if vertex does not yet exist creates the new vertex
	        	if (metaS.isBlank()) {
	        		v1 = findOrCreateNewVertex(BLANK, metaS.getBlankNodeLabel());
	        	}
	        	else if (metaS.isURI()) {
	        		v1 = findOrCreateNewVertex(URI, metaS.getURI());
	        	}
		        
	        	//find or create new meta object
		        if (metaO.isLiteral()) {
		        	v2 = pg.addVertex(T.id, generateVertexId(), KIND, LITERAL, LITERAL, metaO.getLiteralLexicalForm());
		        }
		        else if (metaO.isURI()) {
		        	v2 = findOrCreateNewVertex(URI, metaO.getURI());
		        }
		        
		        else if (metaO.isBlank()) {
		        	v2 = findOrCreateNewVertex(BLANK, metaO.getBlankNodeLabel());
		        }

	        	//find kind for object to correctly turn into string for edge property
		        String objectValue = null;
		        if (o.isLiteral()) {
		        	objectValue = o.getLiteralLexicalForm();
		        }
		        else if (o.isURI()) {
		        	objectValue = o.getURI();
		        }
		        
		        else if (o.isBlank()) {
		        	objectValue = o.getBlankNodeLabel();
		        }
		        
		        GraphTraversalSource g = pg.traversal();
		        boolean edgeAlreadyExist = g.V(v1).out(metaP.getURI()).is(v2).hasNext();
		        
		        //create edge if not already existing, else add property to existing edge
		        if (! edgeAlreadyExist) {
		        	Edge newEdge = v1.addEdge(metaP.getURI(), v2, p.getURI(), objectValue);
		        	edgeList.add(newEdge);
		        }
		        else {
		        	for (Edge e : edgeList) {
		        		if (e.inVertex() == v1 && e.outVertex() == v2) {
		        			if (e.label() == metaP.getURI()) {
		        					e.property(p.getURI(), objectValue);
		        				}
		        			}
		        	}
		        }
		      }
	        
	        //simple triple
	        else if (s instanceof Node && o instanceof Node) 
	        {
	        	//checks if subject is blank or uri, and if vertex does not yet exist creates the new vertex
	        	if (s.isBlank()) {
	        		v1 = findOrCreateNewVertex(BLANK, s.getBlankNodeLabel());
	        	}
	        	else if (s.isURI()) {
	        		v1 = findOrCreateNewVertex(URI, s.getURI());
	        	}

	        	//checks if object is blank, literal or uri, and if new vertex should be created
		        if (o.isLiteral()) {
		        	System.out.println("o.getLexicalForm() = " +  o.getLiteralLexicalForm());
		        	v2 = pg.addVertex(T.id, generateVertexId(), KIND, LITERAL, LITERAL, o.getLiteralLexicalForm());
		        }
		        else if (o.isURI()) {
		        	v2 = findOrCreateNewVertex(URI, o.getURI());
		        }
		        else if (o.isBlank()) {
		        	v2 = findOrCreateNewVertex(BLANK, o.getBlankNodeLabel());
		        }
		        
		        GraphTraversalSource g = pg.traversal();
		        boolean edgeAlreadyExist = g.V(v1).out(p.getURI()).is(v2).hasNext();
		        
		        //create edge if not already existing, else do nothing since no meta should be added
		        if (! edgeAlreadyExist) {
		        	Edge newEdge = v1.addEdge(p.getURI(), v2);
		        	edgeList.add(newEdge);
		        }  
	        }
		}
		return pg;
	}
	
	protected Vertex findOrCreateNewVertex(String kind, String value) {
		GraphTraversalSource g = pg.traversal();
		Optional<Vertex> v = g.V().has(kind, value).tryNext();
		if (v.isPresent()) {
			System.out.println("Found existing vertex already!");
			return v.get();
		}
		else {
			Vertex newV = pg.addVertex(T.id, generateVertexId(), KIND, kind, kind, value);
			return newV;
		}
	}
	
	protected String generateVertexId()
	{
		return "v" +  ++vertexId;
	}
	
}
