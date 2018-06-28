package se.liu.ida.rdfstar.pgtools;

import org.apache.jena.atlas.lib.Lib;

import jena.cmd.CmdGeneral;

/**
 * 
 * @author Ebba Lindström
 * @author Olaf Hartig
 */
public class ConverterRDFStar2PG extends CmdGeneral
{
	// ...

    public static void main(String... argv)
    {
        new ConverterRDFStar2PG(argv).mainRun();
    }

    public ConverterRDFStar2PG(String[] argv)
    {
        super(argv);

        // ...
    }

    static String usage = ConverterRDFStar2PG.class.getName(); //TODO: +" [--out syntax]  \"query\" | --query <file>";

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
