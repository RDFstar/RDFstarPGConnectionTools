package se.liu.ida.rdfstar.pgtools.conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class PG2RDFStar
{
	
	private static char delimiter = '\0'; 
	private static HashMap<String, Pair<String, String>> mappings = new HashMap<String, Pair<String, String>>();
	private static ArrayList<String> printedPrefixes = new ArrayList<String>();
	
	
	public void convert(String vertexFile, String edgeFile, FileWriter fw, String prefixFile) throws IOException {
		
		System.out.println("starting conversion now. . . ");
		BufferedWriter bw = new BufferedWriter(fw);
		Reader reader = Files.newBufferedReader(Paths.get(vertexFile));
		Reader edgeReader = Files.newBufferedReader(Paths.get(edgeFile));
		FileReader fr = null;
		BufferedReader br = null;
		List<Pair<String,String>> headers = new ArrayList<Pair<String,String>>();
		
		CSVParser parser = CSVParser.parse(reader, CSVFormat.RFC4180.withIgnoreSurroundingSpaces().withDelimiter(delimiter));
		
		String prefixRow = null;
		//expects a file which is filled in the format:   "predicate prefix uri" for example "  name ex: http://example.org/   "
		if (prefixFile != null) {
			System.out.println("found a prefix file!");
		if(new File(prefixFile).isFile()) {
			fr = new FileReader(prefixFile);
			br= new BufferedReader(fr);
			//Read the prefix file and store in mappings
			while((prefixRow = br.readLine()) != null)
			{
				String[] splitData = prefixRow.split(" ");
				if(splitData.length == 3)
					mappings.put(splitData[0], Pair.of(splitData[1], splitData[2]));
				else
					mappings.put(splitData[0], Pair.of(splitData[1], ""));
			}
			br.close();
		}	
		}
		
		printPrefixes(bw);
		boolean first_row = true;
		String value;
		String[] splitHeader = null;
		String ttlBlock;
		String strSubject = "";
		//We read one row at the time from vertex file
		System.out.println("now reading vertex file:");
		for (CSVRecord csvRecord : parser) {
			System.out.println("looping...");
			ttlBlock = "";
			if(first_row) //if first row it contains headers which we retrieve and store into a string array.
			{
				System.out.println("reading the first row!");
				for(String it : csvRecord)
				{
					splitHeader = it.trim().split(":");
					if(splitHeader.length > 1)
					{
						headers.add(Pair.of(splitHeader[0],splitHeader[1]));
					}
					else
					{
						headers.add(Pair.of(it.trim(), ""));
					}
					
				}		
				first_row = false;
				continue;
			}
			else //the row contains an entry
			{
				System.out.println("row contains an entry!");
				strSubject = "_:" + csvRecord.get(0);
				System.out.println("found following subject: " + csvRecord.get(0));
				for(int i = 1; i < headers.size(); i++) //go through each column in the row.
				{	
					value = csvRecord.get(i);
					value = value.trim(); 
					if(!value.isEmpty() && value.charAt(0) == '\"')
					{
						value = value.substring(1, value.length()-1);
					}
					if(!value.isEmpty())
					{
						if(headers.get(i).getRight().toLowerCase().equals("string"))
						{	
							value = formatString(value);
							ttlBlock = buildBlock(ttlBlock, strSubject, headers.get(i).getLeft(), value);
						}
						else if(headers.get(i).getRight().toLowerCase().equals("date"))
						{
							value = formatDate(value);
							ttlBlock = buildBlock(ttlBlock, strSubject, headers.get(i).getLeft(), value);
						}
						else if(headers.get(i).getRight().toLowerCase().equals("boolean") || headers.get(i).getRight().toLowerCase().equals("bool"))
						{
							value = formatBool(value);
							ttlBlock = buildBlock(ttlBlock, strSubject, headers.get(i).getLeft(), value);
						}
						else if(headers.get(i).getLeft().toLowerCase().equals("~label") || headers.get(i).getLeft().toLowerCase().equals("label"))
							continue;
						else //if not string or date, for example int, dont modify
						{
							value = formatNumber(value,  headers.get(i).getRight());
							ttlBlock = buildBlock(ttlBlock, strSubject, headers.get(i).getLeft(), value);
						}
					}

				}
			}
			bw.write(ttlBlock + ".\n");
		}
		
		
		first_row = true;
		ttlBlock = "";
		headers.clear();
		System.out.println("now reading edge file: ");
		//Read one row at the time from edge file.
		parser = CSVParser.parse(edgeReader, CSVFormat.RFC4180.withIgnoreSurroundingSpaces().withDelimiter(delimiter));
		for (CSVRecord csvRecord : parser) {
			ttlBlock = "";
			if(first_row) //if first row it will be the header row which we save.
			{
				for(String it : csvRecord)
				{
					splitHeader = it.trim().split(":");
					if(splitHeader.length > 1)
					{
						headers.add(Pair.of(splitHeader[0],splitHeader[1]));
					}
					else
					{
						headers.add(Pair.of(it.trim(), ""));
					}
				}		
				first_row = false;
				continue;
			}
			//We are reading an entry in the edge file and creates the corresponding rdf triple.
			else 
			{
				boolean metaExists = false;
				String from = csvRecord.get(1).trim();
				String to = csvRecord.get(2).trim();
				String relationship = csvRecord.get(3).trim();
				relationship = setPredicatePrefix(relationship);
				String subject = "<<_:" + from + " " + relationship + " _:" + to + ">>";
				for(int i = 4; i < headers.size(); i++)
				{
					value = csvRecord.get(i);
					value = value.trim();
					if(!value.isEmpty() &&  value.charAt(0) == '\"')
					{
							 value = value.substring(1, value.length()-1);
					}					
					if(!value.isEmpty())
					{
						metaExists = true;
						if(headers.get(i).getRight().toLowerCase().equals("string"))
						{						
							value = formatString(value);
							ttlBlock = buildBlock(ttlBlock, subject, headers.get(i).getLeft(), value);
						}
						else if(headers.get(i).getRight().toLowerCase().equals("date"))
						{
							value = formatDate(value);
							ttlBlock = buildBlock(ttlBlock, subject, headers.get(i).getLeft(), value);
						}
						else if(headers.get(i).getRight().toLowerCase().equals("boolean") || headers.get(i).getRight().toLowerCase().equals("bool"))
						{
							value = formatBool(value);
							ttlBlock = buildBlock(ttlBlock, subject, headers.get(i).getLeft(), value);
						}
						else //if not string or date, for example int, dont modify
						{
							value = formatNumber(value,  headers.get(i).getRight());
							ttlBlock = buildBlock(ttlBlock, subject, headers.get(i).getLeft(), value);
						}		
					}
				}
				
				
				if(!metaExists)
					bw.write("_:" + from + " " + relationship + " _:" + to + " .\n");
				else
					bw.write(ttlBlock + ".\n");
			}
			
		}	
		bw.close();
		System.out.println("finished conversion!");
	}
	
	
	
	
//-------------- help methods -----------------
	
	
	
	private static void printPrefixes(BufferedWriter bw) throws IOException
	{
		System.out.println("now reading the prefix file: ");
		for (Pair<String,String> value : mappings.values()) {
		    if(value.getRight() != "" && !printedPrefixes.contains(value.getLeft()))
		    {
		    	System.out.println("found a prefix to write to file!");
		    	bw.write("@prefix " + value.getRight() + " <" + value.getLeft() + "> . \n");
		    	printedPrefixes.add(value.getLeft());
		    }	    	
		}
		if(!printedPrefixes.contains("http://www.w3.org/2001/XMLSchema#"))
			bw.write("@prefix " + "xsd:" + " <http://www.w3.org/2001/XMLSchema#> . \n");
	}
	
	private static String formatDate(String date)
	{
		if(date.length() == 10)
		{
			return "\"" + date + "\"^^xsd:date";
		}
		else if(date.length() == 16)
		{
			return "\"" + date + ":00\"^^xsd:dateTime";
		}
		else if(date.length()== 19)
		{
			return "\"" + date + "\"^^xsd:dateTime";
		}
		else//If we have the format YYYY-MM-DDTHH:mm:SSZ
		{
			return "\"" + date + "\"^^xsd:dateTime";
		}
	}
	
	private static String formatBool(String bool)
	{
		if(bool.equals("1") || bool.toLowerCase().equals("true"))
			return "\"true\"^^xsd:boolean";
		else
			return "\"false\"^^xsd:boolean";
	}
	
	private static String formatNumber(String number, String type)
	{
		if(type.toLowerCase().equals("int"))
			return "\"" + number + "\"^^xsd:integer";
		else if(type.toLowerCase().equals("byte"))
			return "\"" + number + "\"^^xsd:byte";
		else if(type.toLowerCase().equals("short"))
			return "\"" + number + "\"^^xsd:short";
		else if(type.toLowerCase().equals("long"))
			return "\"" + number + "\"^^xsd:long";
		else if(type.toLowerCase().equals("float"))
			return "\"" + number + "\"^^xsd:float";
		else if(type.toLowerCase().equals("double"))
			return "\"" + number + "\"^^xsd:double";
		return "faulty type" ;
	}
	
	private static String formatString(String s)
	{
		s = s.substring(0, s.length()).replaceAll("\"\"", "\"");
		
		if(s.contains("\"") && s.contains("\'"))
			s = "\'\'\'" + s + "\'\'\'";
		else if(s.contains("\""))
			s = "\'" + s + "\'";
		else if(s.contains("\'"))
			s = "\"" + s + "\"";
		else
			s = "\"" + s + "\"";
		
		s = s + "^^xsd:string";
		
		return s;
	}
	
	public static String setPredicatePrefix(String predicate)
	{
		Pair<String,String> prefix;
		if((prefix = mappings.get(predicate)) != null) 
		{
			if(prefix.getRight() != "")
				predicate = prefix.getRight() + predicate;
			else
				predicate = "<" + prefix.getLeft() + predicate + ">";
		}
		else
		{
			predicate = "<" + predicate + ">";
		}
		return predicate;
	}
	public static String buildBlock(String ttlBlock, String subject, String predicate, String object)
	{
		//eventually update the predicate if prefix exists
		predicate = setPredicatePrefix(predicate);
		
		//This helps to make turtle pretty format.
		if(ttlBlock.isEmpty())
		{
			ttlBlock += subject + " " + predicate + " " + object + " ";
		}
		else
		{
			ttlBlock += ";\n" + StringUtils.leftPad("", subject.length() + 1) + predicate + " " + object + " ";
		}		
		return ttlBlock;
	}


}
