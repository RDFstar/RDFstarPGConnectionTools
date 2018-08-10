package se.liu.ida.rdfstar.pgtools.conversion;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.SimpleGraphMaker;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;

import se.liu.ida.rdfstar.tools.parser.lang.LangTurtleStar;

public class TinkerpopPG2RDFStar {
	
	SimpleGraphMaker gm = new SimpleGraphMaker();
	protected HashMap<Vertex, Node> vertexMap;

	public org.apache.jena.graph.Graph convert(Graph pg) {
		
		LangTurtleStar.init();
		org.apache.jena.graph.Graph g = gm.createGraph() ;
		vertexMap = new HashMap<Vertex, Node>();
		
		GraphTraversalSource traversal = pg.traversal();
		
		//check for edge uniqueness
		Iterator<Vertex> itV1 = traversal.V();
		while (itV1.hasNext()) {
			Vertex v = itV1.next();
			GraphTraversal<Vertex, Edge> edges = traversal.V(v).outE();
			while (edges.hasNext()) {
				Edge e = edges.next();
				if (traversal.V(v).outE(e.label()).toList().size() > 1) {
					throw new IllegalArgumentException("cannot convert graph, must be edge unique");
				}
			}
		}
		
		Iterator<Vertex> itV = traversal.V();
		 while (itV.hasNext()) {
			 Vertex v = itV.next();
			 
			 //create blank node in Jena that represents this vertex from Tinkerpop
			 Node s = NodeFactory.createBlankNode();
			 vertexMap.put(v, s);
			 
			 Iterator<VertexProperty<Object>> itP = v.properties();
			 while (itP.hasNext()) {
				 VertexProperty<Object> property = itP.next();
				 Node p =  NodeFactory.createURI(property.key());
				 Node o = NodeFactory.createLiteral(property.value().toString());
				 Triple t = new Triple(s, p, o);
				 g.add(t);
			 }
		}
		 
		Iterator<Edge> itE2 = traversal.E();
		while (itE2.hasNext()) {
			Edge e = itE2.next();
			
			//create triple using inVertex, outVertex and label from Tinkerpop-graph
			Vertex inV = e.inVertex();
			Vertex outV = e.outVertex();
			Node inNode = vertexMap.get(inV);
			Node outNode = vertexMap.get(outV);
			Node predicateNode = NodeFactory.createURI(e.label());
			
			Triple t = new Triple(inNode, predicateNode, outNode);
			Iterator<Property<Object>> properties = e.properties();
			//edge property means meta data to be translated to RDF*
			if (properties.hasNext()) {
				while (properties.hasNext()) {
					Node_Triple metaTriple = new Node_Triple(t);
					Property<Object> property = properties.next();
					Node p = NodeFactory.createURI(property.key());
					Node o = NodeFactory.createLiteral(property.value().toString());
					Triple nestedTriple = new Triple(metaTriple, p, o);

					g.add(nestedTriple);
				}
			}
			//no meta
			else {
				g.add(t);
			}
		}
		return g;
	}
}
