package se.liu.ida.rdfstar.pgtools.conversion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;

import se.liu.ida.rdfstar.tools.parser.lang.LangTurtleStar;

/**
 * 
 * @author Jesper Eriksson
 * @author Amir Hakim
 * @author Ebba Lindström
 */



public class RDFStar2PG
{
	
	private static HashMap<Node, String> vertexMap = new HashMap<Node, String>();
	private static HashMap<Triple, ArrayList<Pair<Node, Node>>> edgeMap = new HashMap<Triple,ArrayList<Pair<Node, Node>>>();
	private static long vertexId = 0;
	private static long edgeId = 0;
	private static List<String> edgeHeaders= new ArrayList<>();
	private static List<String> edgeTypes = new ArrayList<>();
	private final static Node node = NodeFactory.createLiteral("label");
	//Chose name of default headers in vertex file (ID is used in both edge and vertex file):
	private static final String ID = "ID";
	private static final String KIND = "Kind";
	private static final String LITERAL = "Literal";
	private static final String IRI = "IRI";
	private static final String BLANK = "Blank";
	private static final String DATATYPE = "Datatype";
	private static final String LANGUAGE = "Language";
	//Chose name of default headers in edge file:
	private static final String FROM = "From";
	private static final String TO= "To";
	private static final String LABEL = "Label";
	
	
	public void convert(String filename, FileWriter fwv, FileWriter fwe) throws IOException {
		
		LangTurtleStar.init();	
		initLists();
		
		//creates iterator which we can stream the triples with on different threads.
		PipedRDFIterator<Triple> iter = new PipedRDFIterator<>(16000);
	    final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
			  Runnable parser = new Runnable() {
				  
		            @Override
		            public void run() {
		            	
		            	RDFParser.create()
					       .labelToNode( LabelToNode.createUseLabelEncoded() )				  
					       .source(filename)
					       .checking(false)
					       .build()
					       .parse(inputStream);
		            }
		        };
		 
		executor.submit(parser);
		
		//Stream triples and process one by one.
		while (iter.hasNext()) {	
			Triple next = iter.next();
        	fillMaps(next);
	     }
		
		executor.shutdown();
		
		//Print to vertex file
		BufferedWriter bw = new BufferedWriter(fwv);
		printVertices(bw);
		bw.close();
		
		//Print to edge file
		bw = new BufferedWriter(fwe);
		printEdges(bw);
		bw.close();
	}
	
	public static void printEdges(BufferedWriter bw) throws IOException
	{
		//Print headers in edge file
		for(int i = 0; i < edgeHeaders.size(); i++)
		{
			if(i != edgeHeaders.size()-1)		
				bw.write(edgeHeaders.get(i) + ":" + edgeTypes.get(i) + ", ");
			else
				bw.write(edgeHeaders.get(i) + ":" + edgeTypes.get(i) +"\n");
		}
		
				
		//Print other rows in edge file
		for (Map.Entry<Triple, ArrayList<Pair<Node, Node>>> entry : edgeMap.entrySet())
		{
			Triple key = entry.getKey();
			ArrayList<Pair<Node, Node>> value = entry.getValue();
			String[] metaData = new String[edgeHeaders.size() - 4];
			
			//initialize values in metaData
			for(int i = 0; i < metaData.length; i++)
			{
				metaData[i] = ", ";
			}

			//Always print edgeID, from, to and the relationship
			bw.write(generateEdgeId() + ", " + vertexMap.get(key.getSubject()) + ", " +  vertexMap.get(key.getObject()) + ", " +  key.getPredicate().toString());
			
			//if statement-level metadata exists, get and print them this also
			for(int i = 0; i < value.size(); i++)
			{
				if(edgeHeaders.indexOf(value.get(i).getLeft().toString()) != -1)
				{
					if(value.get(i).getRight().isLiteral())
						metaData[edgeHeaders.indexOf(value.get(i).getLeft().toString())-4] = ", " + value.get(i).getRight().getLiteralLexicalForm();
					else
						metaData[edgeHeaders.indexOf(value.get(i).getLeft().toString())-4] = ", " + value.get(i).getRight().toString();
				}
			}
			for(int i = 0; i < metaData.length; i++)
				bw.write(metaData[i]);
			bw.write("\n");
		}
	}
	
	
	//Print everything in the vertex file
	public static void printVertices(BufferedWriter bw) throws IOException
	{
		//Print the headers first
		bw.write(ID + ", "+ KIND + ", "+ LITERAL + ", "+ IRI+ ", "+ BLANK+ ", " + DATATYPE + ", " + LANGUAGE + "\n");
		
		//Print all the entries
		for (Map.Entry<Node, String> entry : vertexMap.entrySet()) {
		    Node key = entry.getKey();
		    String value = entry.getValue();

		    if(key.isLiteral())
		    {	
		    	if(key.getLiteralDatatypeURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"))
		    		bw.write(value + ", " + LITERAL + ", " + key.getLiteralLexicalForm() + ", , , " + key.getLiteralDatatypeURI() +", " + key.getLiteralLanguage() + "\n");
		    	else	
		    		bw.write(value + ", " + LITERAL + ", " + key.getLiteralLexicalForm() + ", , , " + key.getLiteralDatatypeURI() +",\n");
		    }
		    else if(key.isURI())
		    {
		    	bw.write(value + ", " + IRI + ", , " + key.toString() + ", , ," + "\n");
		    }
		    else if(key.isBlank())
		    {
		    	bw.write(value + ", " + BLANK + ", , , " + key.toString() + ", ," + "\n");
		    }
		    else
		    {
		    	throw new IllegalArgumentException("Invalid node type");
		    }
		}
	}
	
	
	public static String generateVertexId()
	{
		return "v" +  ++vertexId;
	}
	
	public static String generateEdgeId()
	{
		return "e" +  ++edgeId;
	}
	
	
	public static void updateVertexMap(Triple triple)
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
	
	
	public static void fillMaps(Triple triple)
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
	        	
	        	//Since we need to know all headers and their type when we print the first row in edge file, store this information    		
	    		if(!edgeHeaders.contains(p.toString()))
	    		{
	    			edgeHeaders.add(p.toString());
	    			if(o.isLiteral())
	    			{
	    			String[] splitType = o.getLiteralDatatypeURI().split("#");
	    			edgeTypes.add(splitType[1]);
	    			}
	    			else
	    			{
	    				edgeTypes.add("String");
	    			}
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
	
	public static void initLists()
	{
		edgeHeaders.add(ID);
		edgeHeaders.add(FROM);
		edgeHeaders.add(TO);
		edgeHeaders.add(LABEL);
		
		//These types are attached to the four headers above (order matters)
		edgeTypes.add("String");
		edgeTypes.add("String");
		edgeTypes.add("String");
		edgeTypes.add("String");
	}
}
