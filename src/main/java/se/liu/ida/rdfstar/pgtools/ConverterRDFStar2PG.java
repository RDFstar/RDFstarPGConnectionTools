package se.liu.ida.rdfstar.pgtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;

import arq.cmdline.ModLangParse;
import arq.cmdline.ModTime;
import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import jena.cmd.CmdGeneral;
import se.liu.ida.rdfstar.pgtools.conversion.RDFStar2PGinCSVFormat;
import se.liu.ida.rdfstar.pgtools.conversion.RDFStar2TinkerpopPG;


/**
 * 
 * @author Ebba Lindström
 * @author Olaf Hartig
 */
public class ConverterRDFStar2PG extends CmdGeneral
{
	protected ModTime modTime                   = new ModTime();
    protected ModLangParse modLangParse         = new ModLangParse();
    
    protected ArgDecl argInFile    = new ArgDecl(ArgDecl.HasValue, "infile");
    protected ArgDecl argVertexFile    = new ArgDecl(ArgDecl.HasValue, "vertexoutfile");
    protected ArgDecl argEdgeFile      = new ArgDecl(ArgDecl.HasValue, "edgeoutfile");
    protected ArgDecl argGraphMLFile      = new ArgDecl(ArgDecl.HasValue, "graphmloutfile");
    
    protected String inFilename;
    protected String vertexFilename;
    protected String edgeFilename;
    protected String graphMLFilename;
    
    protected OutputStream outputVertices;
    protected OutputStream outputEdges;
    protected boolean outStream1Opened = false;
    protected boolean outStream2Opened = false;

    public static void main(String... argv)
    {
        new ConverterRDFStar2PG(argv).mainRun();
    }

    public ConverterRDFStar2PG(String[] argv)
    {
        super(argv);

        super.addModule(modTime);
        super.addModule(modLangParse);
        
        
    }
    
    
    static String usage = ConverterRDFStar2PG.class.getName() + " [--time] [--check|--noCheck] [--sink] [--base=IRI] [--infile=file] [--vertexoutfile=file] [--edgeoutfile=file] [--graphmloutfile=file]";

    @Override
    protected String getSummary()
    {
        return usage;
    }

    
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs();
        
        //process input file
        this.inFilename = getValue(argInFile); 
        if (inFilename == null) {
        	cmdError("no input file given");
        }
        
        else {
            final File inputFile = new File(inFilename); 
            if ( ! inputFile.exists() ) {
            	cmdError("The given input file does not exist");
            } 
            if ( ! inputFile.isFile() ) {
            	cmdError("The given input file is not a file");
            }
        	
        }
        
        this.vertexFilename = getValue(argVertexFile);
        this.edgeFilename = getValue(argEdgeFile);
        if (vertexFilename == null && edgeFilename == null) {
        	this.graphMLFilename = getValue(argGraphMLFile);
        	if (graphMLFilename == null) {
        		cmdError("no output file(s) given");
        	}
        	// process GraphML file
        	else {
        		
        		
        		// initialize the first output stream
                final String outFileName1 = graphMLFilename;;
                final File outputFile1 = new File( outFileName1 );

                // verify that the output file does not yet exist
                if ( outputFile1.exists() ) {
                		cmdError("The given output file already exist");
                    }
                    try {
                    	outputFile1.createNewFile();
                    }
                    catch ( IOException e ) {
                    	cmdError("Creating the output file failed: " + e.getMessage() );
                    }
        	}
        }
        else {
        	if (vertexFilename == null) {
        		cmdError("no input file for the vertices given");
        	}
        	else if (edgeFilename == null) {
        		cmdError("no input file for the edges given");
        	}
        	//process CSV files
        	else {

                // initialize the first output stream
                final String outFileName1 = vertexFilename;;
                
                final File outputFile1 = new File( outFileName1 );

                // verify that the output file does not yet exist
                if ( outputFile1.exists() ) {
                		cmdError("The given output file (for the vertices) already exist");
                    }

                    try {
                    	outputFile1.createNewFile();
                    }
                    catch ( IOException e ) {
                    	cmdError("Creating the output file (for the vertices) failed: " + e.getMessage() );
                    }

                    try {
                    	outputVertices = new FileOutputStream(outputFile1);
                    	outStream1Opened = true;
                    }
                    catch ( FileNotFoundException e ) {
                    	cmdError("The created output file (for the vertices) does not exist");
                    	
                    }
                    
                 // initialize the second output stream
                    final String outFileName2 = edgeFilename;
                    
                    final File outputFile2 = new File( outFileName2 );

                    // verify that the output file does not yet exist
                    if ( outputFile2.exists() ) {
                    		cmdError("The given output file (for the edges) already exist");
                        }

                        try {
                        	outputFile2.createNewFile();
                        }
                        catch ( IOException e ) {
                        	cmdError("Creating the output file (for the edges) failed: " + e.getMessage() );
                        }

                        try {
                        	outputEdges = new FileOutputStream(outputFile2);
                        	outStream2Opened = true;
                        }
                        catch ( FileNotFoundException e ) {
                        	cmdError("The created output file (for the edges) does not exist");
                        	
                        }
        	}
        } 
    }
    
    @Override
    protected String getCommandName() { return Lib.className(this) ; }

    @Override
    protected void exec()
    {
    	//outStreams mean that CSV is supposed to be converted
    	if (outStream1Opened && outStream2Opened) {
    	try {
    		
    		final RDFStar2PGinCSVFormat converter = new RDFStar2PGinCSVFormat();
    		converter.convert(inFilename, outputVertices, outputEdges);
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

    		if ( outStream1Opened && outStream2Opened ) {
    			try {
    				outputVertices.close();
    				outputEdges.close();
    			}
    			catch ( IOException e ) {
    				throw new CmdException("Closing the output stream failed: " + e.getMessage(), e );
    			}
    		}
    	}
    	}
    	//GraphML file is supposed to be converted
    	else {
    		final RDFStar2TinkerpopPG converter = new RDFStar2TinkerpopPG();
    		Graph g = converter.convert(inFilename);
    		
    		try {
    			
				g.io(IoCore.graphml()).writeGraph(graphMLFilename);
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
    	}
    }
}















