package se.liu.ida.rdfstar.pgtools.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
	public void test() throws IOException
	{
		convertAndMakeOutputFiles("nothingnested.ttls", "outv1.csv", "oute1.csv");
	}

/*
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
*/

	
	// ---- helpers ----

	protected TwoCSVs convertAndMakeOutputFiles( String filename, String outfileName1, String outfileName2) throws IOException {
		
		
//		final String fullFilename = "C:\\Users\\ebbli37\\Documents\\testfiles\\" + filename;
		final String fullFilename = getClass().getResource("/TurtleStar/"+filename).getFile();

//		final OutputStream fw1 = new FileOutputStream("C:\\Users\\ebbli37\\Documents\\" + outfileName1);
		final ByteArrayOutputStream vos = new ByteArrayOutputStream();
//		final OutputStream fw2 = new FileOutputStream("C:\\Users\\ebbli37\\Documents\\" + outfileName2);
		final ByteArrayOutputStream eos = new ByteArrayOutputStream();

//		new RDFStar2PG().convert(fullFilename, fw1, fw2);
		new RDFStar2PG().convert(fullFilename, vos, eos);

		final String vResult = vos.toString();
		final String eResult = eos.toString();
		vos.close();
		eos.close();

		// the following two lines may be uncommented for debugging purposes
		//System.out.println(vResult);
		//System.out.println(eResult);

		final CSVFormat csvFormat = CSVFormat.RFC4180.withIgnoreSurroundingSpaces();

		final CSVParser csvParserV = CSVParser.parse(vResult, csvFormat);
		final List<CSVRecord> vertexCSV = csvParserV.getRecords();
		csvParserV.close();

		final CSVParser csvParserE = CSVParser.parse(eResult, csvFormat);
		final List<CSVRecord> edgeCSV = csvParserE.getRecords();
		csvParserE.close();

		return new TwoCSVs(vertexCSV, edgeCSV);
	}

	static class TwoCSVs {
		public final List<CSVRecord> vertexCSV;
		public final List<CSVRecord> edgeCSV;
		public TwoCSVs( List<CSVRecord> vCSV, List<CSVRecord> eCSV) { vertexCSV = vCSV; edgeCSV = eCSV; }
	}

}
