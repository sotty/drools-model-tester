package net.vojir.droolsserver.drools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.vojir.droolsserver.xml.XmlParser;

public class ModelTesterMain {

	public static void main(String[] args) throws Exception {
		String method="confidence";
		String output="text";
		
		if ((args.length==1)&&((args[0].equals("-?")||(args[0].equals("/?"))))){
			System.out.println("Possible arguments:");
			System.out.println("XML filename");
			System.out.println("CSV filename");
			System.out.println("-method:{confidence|longerAntecedent|shorterAntecedent|support|confidenceSupport} => for example: -method:confidence");
			System.out.println("-format:{text|xml}");
		}
		
		if ((args.length<2)){
			throw new Exception("This application requires min. 2 params - XML file name, CSV file name, selection method");
		}
		
		for (String arg : args) {
			if (arg.startsWith("-method:")){
				method=arg.substring(8);
			}else if(arg.startsWith("-output:")){
				output=arg.substring(8);
			}
		}

        String xmlString = readFileToString(args[0]);
        String csvString = readFileToString(args[1]);

        run( xmlString, csvString, method, output );
    }

    public static void run( String xmlString, String csvString, String method, String output ) {
        try {

            //P�ipraven� drools stateless session
            ModelTester modelTester = ModelTester.prepareFromXml(xmlString);
            modelTester.testAllRows(csvString,method);

            System.out.println(ModelTesterMain.prepareOutput(modelTester, output));
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    private static String prepareOutput(ModelTester modelTester,String outputType){
        StringBuffer output=new StringBuffer();
        if (outputType.equals("xml")){
			output.append("<results>");
			output.append("<method>"+ModelTesterSessionHelper.getBetterARMethod()+"</method>");
			output.append("<rowsTotal>"+modelTester.getRowsTotalCount()+"</rowsTotal>");
			output.append("<rowsPositive>"+modelTester.getRowsPositiveMatch()+"</rowsPositive>");
			output.append("<rowsNegative>"+modelTester.getRowsNegativeMatch()+"</rowsNegative>");
			output.append("<rowsErrors>"+modelTester.getRowsError()+"</rowsErrors>");
			output.append("</results>");
		}else{
			output.append("Conflict resolution method: "+ModelTesterSessionHelper.getBetterARMethod()+"\n");
			output.append("Rows total: "+modelTester.getRowsTotalCount()+"\n");
			output.append("Rows positive: "+modelTester.getRowsPositiveMatch()+"\n");
			output.append("Rows negative: "+modelTester.getRowsNegativeMatch()+"\n");
			output.append("Rows errors: "+modelTester.getRowsError()+"\n");
            output.append( "Total rules fired: " + modelTester.getRulesFired() );
	    	
		}
		return output.toString();
	}
	
	
	@SuppressWarnings("resource")
	private static String readFileToString(String fileName) throws IOException{
		StringBuffer sb = new StringBuffer();
    	BufferedReader br = null;
		br = new BufferedReader(new FileReader( fileName ) );
    	for (int c = br.read(); c != -1; c = br.read()) sb.append((char)c);
		
    	return sb.toString();
	}

	@SuppressWarnings("unused")
	private static String readResourceToString(String resourceFileName,String encoding) throws IOException{
		StringBuffer sb = new StringBuffer();
    	BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(XmlParser.class.getResourceAsStream("rules big file.xml"), "UTF-8"));
    	for (int c = br.read(); c != -1; c = br.read()) sb.append((char)c);
    	return sb.toString();
	}

}
