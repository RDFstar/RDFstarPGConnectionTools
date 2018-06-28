package se.liu.ida.rdfstar.pgtools;

import org.apache.jena.atlas.lib.Lib;

import jena.cmd.CmdGeneral;

/**
 * 
 * @author Ebba Lindström
 * @author Olaf Hartig
 */
public class ConverterPG2RDFStar extends CmdGeneral
{
	// ...

    public static void main(String... argv)
    {
        new ConverterPG2RDFStar(argv).mainRun();
    }

    public ConverterPG2RDFStar(String[] argv)
    {
        super(argv);

        // ...
    }

    static String usage = ConverterPG2RDFStar.class.getName(); //TODO: +" [--out syntax]  \"query\" | --query <file>";

    @Override
    protected String getSummary()
    {
        return usage;
    }

    @Override
    protected String getCommandName() { return Lib.className(this) ; }

    @Override
    protected void exec()
    {
    	// TODO ...
    }

}
