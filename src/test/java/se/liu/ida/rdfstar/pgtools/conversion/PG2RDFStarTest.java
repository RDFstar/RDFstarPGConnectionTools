package se.liu.ida.rdfstar.pgtools.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.StringUtils;

import se.liu.ida.rdfstar.tools.parser.lang.LangTurtleStarTest;

/**
 * 
 * @author Olaf Hartig
 * @author Ebba Lindström
 */
public class PG2RDFStarTest
{
	@Before
	public void setup() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void todoWriteTests()
	{
		// TODO ...

		// The following two lines are just to make sure that the additional
		// Java libraries can be used. You can delete these lines later. --Olaf 
		final CSVFormat test = CSVFormat.RFC4180;
		final String test2 = StringUtils.SPACE;

		LangTurtleStarTest.createGraphFromTurtleStarSnippet("...");
	}

}
