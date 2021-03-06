package IBW_SBOL_update.make_doc;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.Sequence;


public class App 
{
	
    public static void main( String[] args ) {
    	
    	//Chose an arbitrary, SBOL-compliant part to extract its DNA sequence from.
    	String partName = "BO_28543";
    	
    	System.out.println(getSequenceFromNCL(partName));
    }

    /**
     * Returns a DNA sequence in string form from a specified SBOL part
     * @param partName
     * @return
     */
    private static String getSequenceFromNCL(String partName) {

    	String sequence = "";
    	try {
    		InputStream url = new URL("http://sbol.ncl.ac.uk:8081/part/" + partName + "/sbol").openStream();
    		SBOLDocument sbol = SBOLReader.read(url);
    		Set<Sequence> sequences = sbol.getSequences();
    		sequence = sequences.iterator().next().getElements();
    	} catch (Exception e) {
    		System.out.println(e);
//    		throw new UnknownPartInVirtualPartRepository(partName);
    	}

    	return sequence;
    }
    
}
