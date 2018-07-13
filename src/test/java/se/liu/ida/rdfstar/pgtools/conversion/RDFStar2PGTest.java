package se.liu.ida.rdfstar.pgtools.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 * @author Olaf Hartig
 * @author Ebba Lindström
 */

//TODO: gives no error when creating files from all files except the one with double nested subject. 
//Not sure how to continue testing the data, since I have not tested the actual output file more than just looking at it

public class RDFStar2PGTest
{
	@Before
	public void setup() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void noreification() throws IOException
	{
		convertAndMakeOutputFiles("noreification.ttls", "outv1.csv", "oute1.csv");
	}
	
	@Test
	public void nestedsubject() throws IOException
	{
		convertAndMakeOutputFiles("nestedsubject.ttls", "outv2.csv", "oute2.csv");
	}
	
	//this one returns error at the moment, is it not possible to have a double nested subject in PG-conversion?
	@Test
	public void doublenestedsubject() throws IOException
	{
		convertAndMakeOutputFiles("doublenestedsubject.ttls", "outv3.csv", "oute3.csv");
	}
	
	@Test
	public void onenestedandonenotnested() throws IOException
	{
		convertAndMakeOutputFiles("onenestedandonenotnested.ttls", "outv4.csv", "oute4.csv");
	}

	
	// ---- helpers ----

	protected void convertAndMakeOutputFiles( String filename, String outfileName1, String outfileName2) throws IOException {
		
		
		final String fullFilename = "C:\\Users\\ebbli37\\Documents\\testfiles\\" + filename;

		final FileWriter fw1 = new FileWriter("C:\\Users\\ebbli37\\Documents\\" + outfileName1);
		final FileWriter fw2 = new FileWriter("C:\\Users\\ebbli37\\Documents\\" + outfileName2);

		new RDFStar2PG().convert(fullFilename, fw1, fw2);
		
	}
	
}
