package se.liu.ida.rdfstar.pgtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.ARQInternalErrorException;

import arq.cmdline.ModLangParse;
import arq.cmdline.ModTime;
import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import jena.cmd.CmdGeneral;
import se.liu.ida.rdfstar.pgtools.conversion.PG2RDFStar;

/**
 * 
 * @author Ebba Lindström
 * @author Olaf Hartig
 */


public class ConverterPG2RDFStar extends CmdGeneral
{
	protected ModTime modTime                   = new ModTime();
    protected ModLangParse modLangParse         = new ModLangParse();
    
    protected ArgDecl prefixinfile    = new ArgDecl(ArgDecl.HasValue, "prefix", "prefixfile", "prefixinfile");
    
    protected String inputFileVertex;
    protected String inputFileEdge;
    protected OutputStream os;
    protected String prefixFilename = null;
    protected boolean outStreamOpened = false;

    public static void main(String... argv)
    {
        new ConverterPG2RDFStar(argv).mainRun();
    }

    protected ConverterPG2RDFStar(String[] argv)
    {
    	super(argv);

    	super.addModule(modTime);
    	super.addModule(modLangParse);
  
    }
    
    static String usage = ConverterPG2RDFStar.class.getName() + " [--time] [--check|--noCheck] [--sink] [--base=IRI] [--prefix=file] vertexinfile edgeinfile outfile";

    @Override
    protected String getSummary()
    {
        return usage;
    }
    
    
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs();

        if ( getNumPositional() == 0 ) {
        	cmdError("No files specified");
        }  
        
        // initialize the output stream
        final String prefixFilename = getValue(prefixinfile);
        if ( prefixFilename != null )
        {
        	this.prefixFilename = prefixFilename;
        	final File prefixFile = new File(prefixFilename); 
            if ( ! prefixFile.exists() ) {
            	cmdError("The given input file for the vertices does not exist");
            } 
            if ( ! prefixFile.isFile() ) {
            	cmdError("The given input file for the vertices is not a file");
            }
        }

        inputFileVertex = getPositionalArg(0);

        // check whether the first input file actually exists and is indeed a file
        
        final File inputFilev = new File(inputFileVertex); 
        if ( ! inputFilev.exists() ) {
        	cmdError("The given input file for the vertices does not exist");
        } 
        if ( ! inputFilev.isFile() ) {
        	cmdError("The given input file for the vertices is not a file");
        }
        
        inputFileEdge = getPositionalArg(1);

        // check whether the first input file actually exists and is indeed a file
        final File inputFilee = new File(inputFileEdge); 
        if ( ! inputFilee.exists() ) {
        	cmdError("The given input file for the edges does not exist");
        } 
        if ( ! inputFilee.isFile() ) {
        	cmdError("The given input file for the edges is not a file");
        }

        // initialize the file writer
        final String outFileName = getPositionalArg(2);
        
        final File outputFile = new File( outFileName );

        // verify that the output file does not yet exist
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
    

    @Override
    protected String getCommandName() { return Lib.className(this) ; }

    @Override
    protected void exec()
    {
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
