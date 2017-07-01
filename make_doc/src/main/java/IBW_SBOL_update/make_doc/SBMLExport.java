package IBW_SBOL_update.make_doc;

import java.util.ArrayList;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;

public class SBMLExport {
static BiocompilerModel biocompilerModel = new BiocompilerModel("Model");
	
    public static void main( String[] args ) {
    	
    	//Manually populating data structures for makeSBOLDocument to read in
        ArrayList<Biopart> parts1 = new ArrayList<Biopart>();
        Biopart cell1part1 = new Biopart(2, "FirstCellFirstPart", "PROMOTER", "", 1, 1, "aa");
        Biopart cell1part2 = new Biopart(4, "FirstCellSecondPart", "GENE", "", 1, 2, "cccc");
        Biopart cell1part3 = new Biopart(6, "FirstCellFinalPart", "", "Random_url", 0, 3, "tttccc");
        parts1.add(cell1part1);
        parts1.add(cell1part2);
        parts1.add(cell1part3);
        Device d1 = new Device(parts1, "Device1");
        ArrayList<Device> devices1 = new ArrayList<Device>();
        devices1.add(d1);
        biocompilerModel.addCell("FirstCell", devices1);
        
        ArrayList<Biopart> parts2 = new ArrayList<Biopart>();
        Biopart cell2part1 = new Biopart(2, "SecondCellFirstPart", "TERMINATOR", "", 0, 1, "at");
        Biopart cell2part2 = new Biopart(2, "SecondCellSecondPart", "random", "", 0, 2, "gc");
        parts2.add(cell2part1);
        parts2.add(cell2part2);
        Device d2 = new Device(parts2, "Device2");
        ArrayList<Device> devices2 = new ArrayList<Device>();
        devices2.add(d2);
        biocompilerModel.addCell("SecondCell", devices2);
        
        //Attempt to create SBML document and save it to the local workspace
        try {
			SBMLDocument doc = makeSBMLDocument();
			System.out.println(doc.checkConsistency());
			System.out.println(doc.getListOfErrors()); //Which one is right error checking?
//			doc.write("Updated Export Test SBML Document"); HOW TO WRITE SBML DOCUMENT TO FILE
		} catch (SBMLError e) {
			System.out.println("SBMLError Thrown");
			e.printStackTrace();
		}
    }
    
    public static SBMLDocument makeSBMLDocument() {
    	
    	int version = 1; /* Placeholder version */
    	String prefix = "file://"; /* Placeholder prefix */
    	String namespace = "dummy.org"; /* Placeholder namespace */
    	
		SBMLDocument doc = new SBMLDocument(3, version);
		doc.addDeclaredNamespace(prefix, namespace);
		CompSBMLDocumentPlugin compDoc = new CompSBMLDocumentPlugin(doc);
		

    	for (Cell c : biocompilerModel.cells) {
    		
    		Model cModel = compDoc.createModelDefinition(c.name);
    		CompModelPlugin cModelPlugin = new CompModelPlugin(cModel);
//        	HashSet<URI> molecules = new HashSet<URI>(); /* Keep visited molecules over every cell or per cell? Is URI proper way to keep track of molecule? */
//    		for(MolecularSpecies ms : c.moleculeList) { /* Have it ignore DNA parts? */
//    			molecules.add(ms.URI);
//    			Species species = m.createSpecies(ms.name + "_molecule", compartment);
//    			species.setInitialAmount(ms.amount);
//    			String unit = species.unit; ASK ABOUT UNIT DEFINITION
//    			UnitDefinition unitDef;
//    			if((unitDef = m.findUnitDefinition(unit)) == null) unitDef = m.createUnitDefinition(unit);
//    			
//    		}
    		
    		for (Device d : c.devices){
    			Model d_model = compDoc.createModelDefinition(d.name);
//    			c_model.listof
//				DO THE SAME AS BEFORE?? For input, output, molecule list, but not parts
    		}
    	}
		
    	return doc;
    	
    }
    
}
