package se.liu.ida.rdfstar.pgtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.util.iterator.ExtendedIterator;

import arq.cmdline.ModLangParse;
import arq.cmdline.ModTime;
import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import jena.cmd.CmdGeneral;
import se.liu.ida.rdfstar.pgtools.conversion.PG2RDFStar;
import se.liu.ida.rdfstar.pgtools.conversion.TinkerpopPG2RDFStar;
import se.liu.ida.rdfstar.tools.serializer.SinkTripleStarOutput;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * 
 * @author Ebba Lindström
 * @author Olaf Hartig
 */


public class ConverterPG2RDFStar extends CmdGeneral
{
	protected ModTime modTime                   = new ModTime();
    protected ModLangParse modLangParse         = new ModLangParse();
    
    protected ArgDecl argPrefixFile    = new ArgDecl(ArgDecl.HasValue, "prefix", "prefixfile", "prefixinfile");
    protected ArgDecl argOutputFile    = new ArgDecl(ArgDecl.HasValue, "out", "output", "outfile", "outputfile");
    protected ArgDecl argVertexFile    = new ArgDecl(ArgDecl.HasValue, "vertexfile");
    protected ArgDecl argEdgeFile      = new ArgDecl(ArgDecl.HasValue, "edgefile");
    protected ArgDecl argGraphMLFile   = new ArgDecl(ArgDecl.HasValue, "graphmlfile");
    
    protected String inputFileVertex;
    protected String inputFileEdge;
    protected String inputFileGraphML;
    protected OutputStream os;
    protected String prefixFilename = null;
    protected boolean outStreamOpened = false;
    protected boolean tinkerpopFileGiven;

    public static void main(String... argv)
    {
        new ConverterPG2RDFStar(argv).mainRun();
    }

    protected ConverterPG2RDFStar(String[] argv)
    {
    	super(argv);

    	super.addModule(modTime);
    	super.addModule(modLangParse);

        super.getUsage().startCategory("Output options");
        super.add(argOutputFile, "--out", "Output file (optional, printing to stdout if omitted)");

        super.getUsage().startCategory("Input files, CSV");
        super.add(argVertexFile, "--vertexfile", "CSV file containing the vertex data");
        super.add(argEdgeFile, "--edgefile", "CSV file containing the edge data");
        super.add(argPrefixFile, "--prefixfile", "Prefix file (optional)");
        
        super.getUsage().startCategory("Input file, GraphML");
        super.add(argGraphMLFile, "--graphmlfile", "GraphML file containing a Tinkerpop-graph");
    }

    static String usage = ConverterPG2RDFStar.class.getName() + " [--time] [--check|--noCheck] [--sink] [--base=IRI] [--prefixfile=file] [--out=file] [--vertexfile=file] [--edgefile=file] [--graphmlfile=file]";

    @Override
    protected String getSummary()
    {
        return usage;
    }
    
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs();

        // check if prefix file is given and if it exists
        final String prefixFilename = getValue(argPrefixFile);
        if ( prefixFilename != null )
        {
        	this.prefixFilename = prefixFilename;
        	final File prefixFile = new File(prefixFilename); 
            if ( ! prefixFile.exists() ) {
            	cmdError("The given input file for the prefixes does not exist");
            } 
            if ( ! prefixFile.isFile() ) {
            	cmdError("The given input file for the prefixes is not a file");
            }
        }

        // check if there is CSV files specified, else check for the GraphML file
        final String vertexFilename = getValue(argVertexFile);
        final String edgeFilename = getValue(argEdgeFile);
        if ( vertexFilename == null && edgeFilename == null) {
        	final String graphmlFilename = getValue(argGraphMLFile);
        	if (graphmlFilename == null) {
        		cmdError("no input files specified");
        	}
        	
        	//process GraphML file
        	else {
        		System.out.println(graphmlFilename);
        		tinkerpopFileGiven = true;
            	this.inputFileGraphML = graphmlFilename;
            	final File inputFile = new File(graphmlFilename); 
                if ( ! inputFile.exists() ) {
                	cmdError("The given input file for the Tinkerpop-graph does not exist");
                } 
                if ( ! inputFile.isFile() ) {
                	cmdError("The given input file is not a file");
                }    		
        	}
        }
        
        else if (vertexFilename != null && edgeFilename != null){
        	tinkerpopFileGiven = false;
        	//process vertex file
        	this.inputFileVertex = vertexFilename;
        	final File inputFilev = new File(vertexFilename); 
            if ( ! inputFilev.exists() ) {
            	cmdError("The given input file for the vertices does not exist");
            } 
            if ( ! inputFilev.isFile() ) {
            	cmdError("The given input file for the vertices is not a file");
            }
            //process edge file
        	this.inputFileEdge = edgeFilename;
        	final File inputFilee = new File(edgeFilename); 
            if ( ! inputFilee.exists() ) {
            	cmdError("The given input file for the edges does not exist");
            } 
            if ( ! inputFilee.isFile() ) {
            	cmdError("The given input file for the edges is not a file");
            }
        }
        
        else {
        	cmdError("wrong input files given, needs to be either two CSV-files or one GraphML-file");
        }
        
        // initialize the output stream
        final String outFileName = getValue(argOutputFile);
        if ( outFileName == null ) {
        	os = System.out; // no output file specified, write to stdout instead
        }
        
        else {
        	
            final File outputFile = new File( outFileName );

            if ( outputFile.exists() ) {
            		cmdError("The given output file already exist");
                }
                try {
                	outputFile.createNewFile();
                }
                catch ( IOException e ) {
                	cmdError("Creating the output file failed: " + e.getMessage() );
                }
                try {
                	os = new FileOutputStream(outputFile);
                	outStreamOpened = true;
                }
                catch ( FileNotFoundException e ) {
                	cmdError("The created output file does not exist");
                }
        }
    }
    

    @Override
    protected String getCommandName() { return Lib.className(this) ; }

    @Override
    protected void exec()
    {
    	//convert using GraphML file and Tinkerpop2RDFStar converter
        if (tinkerpopFileGiven) {
    		final Graph newGraph = TinkerGraph.open();
    		try {
				newGraph.io(IoCore.graphml()).readGraph(inputFileGraphML);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    		final TinkerpopPG2RDFStar converter = new TinkerpopPG2RDFStar();
    		final org.apache.jena.graph.Graph g = converter.convert(newGraph);
    		
    		SinkTripleStarOutput out = new SinkTripleStarOutput(os, null, NodeToLabel.createScopeByDocument());
    		
    		ExtendedIterator<Triple> it = g.find();
			while (it.hasNext()) {
				Triple t = it.next();
				out.send(t);
			}
			out.close();
        }
        
        //convert using CSV files and PG2RDFStar converter
        else {
        	try {
        		final PG2RDFStar converter = new PG2RDFStar();
        		converter.convert(inputFileVertex, inputFileEdge, os, prefixFilename);
    	}
        	catch (ARQInternalErrorException intEx)
        	{
        		System.err.println(intEx.getMessage()) ;
        		if ( intEx.getCause() != null )
            {
                System.err.println("Cause:");
                intEx.getCause().printStackTrace(System.err);
                System.err.println();
            }
            intEx.printStackTrace(System.err);
        }
        catch (JenaException ex)
    	{ 
            ex.printStackTrace();
            throw ex;
        } 
        catch (CmdException ex) { throw ex; } 
        catch (Exception ex)
        {
            throw new CmdException("Exception", ex);
        }
    	finally
    	{
    		if ( outStreamOpened ) {
    			try {
    				os.close();
    			}
    			catch ( IOException e ) {
    				throw new CmdException("Closing the output stream failed: " + e.getMessage(), e );
    			}
    		}
    	}
    }
    }
}
