package se.liu.ida.rdfstar.pgtools.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
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
 * @author Ebba Lindstr�m
 */


public class RDFStar2PGTest
{
	@Before
	public void setup() {
	}

	@After
	public void tearDown() {
	}

	
	@Test
	public void nothingNested() throws IOException
	{
		final TwoCSVs result = convertAndMakeCSVList("nothingnested.ttls");
		
		checkSizeofCSVs(3, 2, result);
		checkRowsVertex(result);
		checkRowsEdge(result);
	}
	
	@Test
	public void nestedsubject() throws IOException
	{
		final TwoCSVs result = convertAndMakeCSVList("nestedsubject.ttls");
		
		checkSizeofCSVs(3, 2, result);
		checkRowsVertex(result);	
		checkRowsEdge(result);
	}
	
	
	@Test
	public void onenestedandonenotnested() throws IOException
	{
		final TwoCSVs result = convertAndMakeCSVList("onenestedandonenotnested.ttls");
		
		checkSizeofCSVs(5, 3, result);
		checkRowsVertex(result);
		checkRowsEdge(result);
	}
	
	@Test
	public void nestedobject() throws IOException
	{
		 boolean thrown = false;

		  try {
			  convertAndMakeCSVList("nestedobject.ttls");
		  } catch (IllegalArgumentException e) {
		    thrown = true;
		  }

		  assertTrue(thrown);
	}
	
	@Test
	public void doublenestedsubject() throws IOException
	{
		 boolean thrown = false;

		  try {
			  convertAndMakeCSVList("doublenestedsubject.ttls");
		  } catch (IllegalArgumentException e) {
		    thrown = true;
		  }

		  assertTrue(thrown);
	}

	
	// ---- helpers ----

	protected TwoCSVs convertAndMakeCSVList( String filename) throws IOException {

		String fullFilename = getClass().getResource("/TurtleStar/"+filename).getFile();

		ByteArrayOutputStream vos = new ByteArrayOutputStream();
		ByteArrayOutputStream eos = new ByteArrayOutputStream();

		new RDFStar2PG().convert(fullFilename, vos, eos);

		String vResult = vos.toString();
		String eResult = eos.toString();
		vos.close();
		eos.close();

		// the following two lines can be uncommented for debugging purposes
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
	
	protected void checkSizeofCSVs(int vertexSize, int edgeSize, TwoCSVs result) {
		assertEquals(vertexSize, result.vertexCSV.size());
		assertEquals(edgeSize, result.edgeCSV.size());
	}
	
	protected void checkRowsVertex(TwoCSVs result) {
		final Iterator<CSVRecord> vertexIter = result.vertexCSV.iterator();

		// first row
		final CSVRecord row1 = vertexIter.next();
		assertEquals("ID", row1.get(0));

		// other rows
		while (vertexIter.hasNext()) {
			final CSVRecord row = vertexIter.next();
			final String elem = row.get(0);
			assertEquals("v", elem.substring(0, 1));
		}
	}

	protected void checkRowsEdge(TwoCSVs result) {
		final Iterator<CSVRecord> edgeIter = result.edgeCSV.iterator();

		// first row
		final CSVRecord row1 = edgeIter.next();
		assertEquals("ID:String", row1.get(0));

		// other rows
		while (edgeIter.hasNext()) { 
			final CSVRecord row = edgeIter.next();
			final String elem = row.get(0);
			assertEquals("e", elem.substring(0, 1));
		}
	}

	static class TwoCSVs {
		final List<CSVRecord> vertexCSV;
		final List<CSVRecord> edgeCSV;
		TwoCSVs( List<CSVRecord> vCSV, List<CSVRecord> eCSV) { vertexCSV = vCSV; edgeCSV = eCSV; }
	}

}
