package se.liu.ida.rdfstar.pgtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.ARQInternalErrorException;

import arq.cmdline.ModLangParse;
import arq.cmdline.ModTime;
import jena.cmd.CmdException;
import jena.cmd.CmdGeneral;
import se.liu.ida.rdfstar.pgtools.conversion.RDFStar2PG;




/**
 * 
 * @author Ebba Lindström
 * @author Olaf Hartig
 */
public class ConverterRDFStar2PG extends CmdGeneral
{
	protected ModTime modTime                   = new ModTime();
    protected ModLangParse modLangParse         = new ModLangParse();
    
    protected String inputFilename;
    protected FileWriter fwVertices;
    protected FileWriter fwEdges;
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
    
    
    static String usage = ConverterRDFStar2PG.class.getName() + " [--time] [--check|--noCheck] [--sink] [--base=IRI] infile vertexoutfile edgeoutfile";

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
        else if ( getNumPositional() != 3 ) {
        	cmdError("Given files cannot be interpreted");
        }

        inputFilename = getPositionalArg(0);

        // check whether the input file actually exists and is indeed a file
        final File inputFile = new File(inputFilename); 
        if ( ! inputFile.exists() ) {
        	cmdError("The given input file does not exist");
        } 
        if ( ! inputFile.isFile() ) {
        	cmdError("The given input file is not a file");
        }

        // initialize the first output stream
        final String outFileName1 = getPositionalArg(1);
        
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
            	fwVertices = new FileWriter(outputFile1);
            	outStream1Opened = true;
            }
            catch ( FileNotFoundException e ) {
            	cmdError("The created output file (for the vertices) does not exist");
            	
            } catch (IOException e) {
            	cmdError("Writing to the output file (for the vertices) failed: " + e.getMessage() );
			}
            
         // initialize the second output stream
            final String outFileName2 = getPositionalArg(2);
            
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
                	fwEdges = new FileWriter(outputFile2);
                	outStream2Opened = true;
                }
                catch ( FileNotFoundException e ) {
                	cmdError("The created output file (for the edges) does not exist");
                	
                } catch (IOException e) {
					cmdError("Writing to the output file (for the edges) failed: " + e.getMessage() );
				}      
               
    }
    
    @Override
    protected String getCommandName() { return Lib.className(this) ; }

    @Override
    protected void exec()
    {
    	try {
    		
    		final RDFStar2PG converter = new RDFStar2PG();
    		converter.convert(inputFilename, fwVertices, fwEdges);
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
    				fwVertices.close();
    				fwEdges.close();
    			}
    			catch ( IOException e ) {
    				throw new CmdException("Closing the output stream failed: " + e.getMessage(), e );
    			}
    		}
    	}
    }
}
