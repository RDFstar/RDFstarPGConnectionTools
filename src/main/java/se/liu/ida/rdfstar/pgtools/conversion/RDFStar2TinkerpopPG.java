package se.liu.ida.rdfstar.pgtools.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.util.iterator.ExtendedIterator;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * 
 * @author Olaf Hartig
 * @author Ebba Lindström
 */
public class RDFStar2TinkerpopPG
{
	private final static Node node = NodeFactory.createLiteral("label");
	
	protected long vertexId;
	protected long edgeId;
	
	private static final String ID = "ID";
	private static final String KIND = "Kind";
	private static final String LITERAL = "Literal";
	private static final String IRI = "IRI";
	private static final String BLANK = "Blank";
	protected HashMap<Node, String> vertexMap;
	protected HashMap<Triple, ArrayList<Pair<Node, Node>>> edgeMap;
	
	/*
	protected long vertexId;
	protected long edgeId;
	protected ArrayList<String> valueList;
	
	private static final String ID = "ID";
	private static final String KIND = "Kind";
	private static final String LITERAL = "Literal";
	private static final String IRI = "IRI";
	private static final String BLANK = "Blank";
	*/
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
	
	public Graph convert(Iterator<Triple> input) {
		
		//final Graph pg = TinkerGraph.open();
		
		vertexId = 0L;
		edgeId   = 0L;
		vertexMap = new HashMap<Node, String>();
		edgeMap   = new HashMap<Triple,ArrayList<Pair<Node, Node>>>();
		
		while (input.hasNext()) {	
			Triple next = input.next();
        	fillMaps(next);
		}
		
		Graph pg = makeGraphFromMaps();
		
		return pg;
	}
	
	private Graph makeGraphFromMaps() {
		final Graph pg = TinkerGraph.open();
		
		//create the vertices
		for (Map.Entry<Node, String> entry : vertexMap.entrySet()) {
		    Node key = entry.getKey();
		    String value = entry.getValue();
		    
		    if(key.isLiteral())
		    {	
		    	pg.addVertex(value, KIND, LITERAL, LITERAL, key.getLiteral());
		    }
		    else if(key.isURI())
		    {
		    	pg.addVertex(value, KIND, IRI, IRI, key.getURI());
		    }
		    else if(key.isBlank())
		    {
		    	//not sure if I should get the id or the label here??
		    	pg.addVertex(value, KIND, BLANK, BLANK, key.getBlankNodeId());
		    }
		    else
		    {
		    	throw new IllegalArgumentException("Invalid node type");
		    }
		    
		}
		
		//create the edges
		
		return pg;
	}

	protected String generateVertexId()
	{
		return "v" +  ++vertexId;
	}
	
	protected String generateEdgeId()
	{
		return "e" +  ++edgeId;
	}
	
	protected void fillMaps(Triple triple)
	{
		Node s = triple.getSubject();
		Node p = triple.getPredicate();
        Node o = triple.getObject();

        if( o instanceof Node_Triple )
        {
        	throw new IllegalArgumentException("Nested triple not allowed as object");
        }
        //If triple is nested, add to edgeMap but also add metadata in arraylist
        //Else normal triple, just add to edgemap
        else if ( s instanceof Node_Triple )
		{       	
	        	Triple subTriple = ((Node_Triple)s).get();       	
	        	updateVertexMap(subTriple);
	    	
	        	//Update edgeMap
	        	if(edgeMap.get(subTriple) != null)
	        	{
	        		edgeMap.get(subTriple).add(Pair.of(p,o));
	        	}
	        	else 
	        	{
	        		ArrayList<Pair<Node, Node>> list = new ArrayList<Pair<Node,Node>>();
	        		list.add(Pair.of(node, subTriple.getPredicate()));
	        		list.add(Pair.of(p,o));
	        		edgeMap.put(subTriple,list); 
	   
	        	}
	        	
		}
        else
        {
        	updateVertexMap(triple);
        	 	
        	if(edgeMap.get(triple) == null)
        	{
        		ArrayList<Pair<Node, Node>> list = new ArrayList<Pair<Node,Node>>();
        		list.add(Pair.of(node, p));
        		edgeMap.put(triple,list);    
        	}   		 	
        }
       
	}
	
	protected void updateVertexMap(Triple triple)
	{
		String id = null;
	    String id2 = null;
	    //Generate vertexID if subject already doesn't have one
		if((id = vertexMap.get(triple.getSubject())) == null )
    	{	
    		id = generateVertexId();
    		vertexMap.put(triple.getSubject(), id);
    	}
    	
		//Generate vertexID if object already doesn't have one
    	if((id2 = vertexMap.get(triple.getObject())) == null)
    	{
    		id2 = generateVertexId();
    		vertexMap.put(triple.getObject(), id2);
    	}
	}

	/*
	public Graph convert(Iterator<Triple> input)
	{
		final Graph pg = TinkerGraph.open();
		
		valueList = new ArrayList<String>();
		vertexId = 0L;
		edgeId   = 0L;

		// TODO: populate the graph by iterating over the input
		
		//Stream triples and process one by one.
		while (input.hasNext()) {	
			Triple next = input.next();
        	
			Node s = next.getSubject();
			Node p = next.getPredicate();
	        Node o = next.getObject();
	        Vertex v1;
	        Vertex v2;

	        if( o instanceof Node_Triple )
	        {
	        	throw new IllegalArgumentException("Nested triple not allowed as object");
	        }
	        //simple triple
	        else if (s instanceof Node && o instanceof Node) 
	        {
	        	//TODO: also handle blank nodes here
		        if (! isAlreadyVertex(s.getURI())) {
		        	pg.addVertex(T.id, generateVertexId(), KIND, "IRI", IRI, s.getURI());
		        	valueList.add(s.getURI());
		        }
		        else {
		        }
		        
		        if (metaO.isLiteral()) {
		        	v2 = pg.addVertex(T.id, generateVertexId(), KIND, "literal", LITERAL, metaO.getLiteral());
		        }
		        else if (metaO.isURI() && ! isAlreadyVertex(metaO)) {
		        	v2 = pg.addVertex(T.id, generateVertexId(), KIND, "IRI", IRI, metaO.getURI());
		        }
	        }
	        
	        else if (s instanceof Node_Triple) {
	        	Triple subTriple = ((Node_Triple)s).get();
	        	Node metaS = subTriple.getSubject();
				Node metaP = subTriple.getPredicate();
		        Node metaO = subTriple.getObject();

		        
		        if (! isAlreadyVertex(metaS)) {
		        	v1 = pg.addVertex(T.id, generateVertexId(), "kind", "IRI", "IRI", metaS.getURI());
		        	valueList.add(metaS.getURI());
		        }
		        else {
		        	v1 = 
		        }
		        
		        if (metaO.isLiteral()) {
		        	v2 = pg.addVertex(T.id, generateVertexId(), "kind", "literal", "literal", metaO.getLiteral());
		        }
		        else if (metaO.isURI()) {
		        	if (! isAlreadyVertex(metaO.getURI())) {
		        	v2 = pg.addVertex(T.id, generateVertexId(), "kind", "IRI", "IRI", metaO.getURI());
		        	valueList.add(metaO.getURI());
		        }
		      }
	        }
	     }

		return pg;
	}
	
	public boolean isAlreadyVertex(Vertex v) {
		Iterator<Vertex> vIter = gp.vertices();
		for (Vertex v: vIter) {
			
		}
	}
	
	protected String generateVertexId()
	{
		return "v" +  ++vertexId;
	}
	
	protected String generateEdgeId()
	{
		return "e" +  ++edgeId;
	}
	*/

}
