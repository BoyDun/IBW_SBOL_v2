package roadblock.dataprocessing.export;

import java.util.List;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.Submodel;

import roadblock.emf.ibl.Ibl.Cell;
import roadblock.emf.ibl.Ibl.Device;
import roadblock.emf.ibl.Ibl.FlatModel;
import roadblock.emf.ibl.Ibl.MolecularSpecies;
import roadblock.emf.ibl.Ibl.Region;
import roadblock.emf.ibl.Ibl.Rule;

public class SBML_Export {
    
	//Helper marker for uniqifying display IDs via incrementation
	private static int ID = 1;
	//SBML level
	private static int level = 3;
	//Version of components being created
	private static int version = 1;
	
	/**
	 * This helper function turns a string into an SBOL-compatible ID.
	 * 
	 * @param displayId is the ID to be made SBOL-compatible if in an improper format / null
	 * @return the fixed displayId
	 */
	private static String fixDisplayID(String displayId) {
		if(displayId == null || displayId.equals("")) return "Unnamed" + (ID++);
		int index = Math.max(displayId.lastIndexOf('/'), Math.max(displayId.lastIndexOf('#'), displayId.lastIndexOf(':')));
		if (index != -1) displayId = displayId.substring(index + 1);
		displayId = displayId.replaceAll("[^a-zA-Z0-9_]", "_");
		displayId = displayId.replace(" ", "_");
		if (Character.isDigit(displayId.charAt(0))) { 
			displayId = "_" + displayId;
		}
		return displayId;
	}
	
	/**
	 * 
	 * @param doc is the SBML document from which the plugin is created.
	 * @return the created plugin.
	 */
    private static CompSBMLDocumentPlugin getCompDocPlugin(SBMLDocument doc) {
		CompSBMLDocumentPlugin compDoc = new CompSBMLDocumentPlugin(doc);
		doc.addExtension(CompConstants.namespaceURI, compDoc);
		return compDoc;
    }
    
    /**
     * 
     * @param model is the model from which the plugin is created.
     * @return the created plugin.
     */
    private static CompModelPlugin getCompModelPlugin(Model model) {
    	CompModelPlugin compModel = new CompModelPlugin(model);
    	model.addExtension(CompConstants.namespaceURI, compModel);
    	return compModel;
    }
    
    /**
     * 
     * @param sb is the SBML base object from which the plugin is created.
     * @return the created plugin.
     */
    private static CompSBasePlugin getCompSBasePlugin(SBase sb) {
    	CompSBasePlugin compBase = new CompSBasePlugin(sb);
    	sb.addExtension(CompConstants.namespaceURI, compBase);
    	return compBase;
    }
    
    private static void createUnits(Model m) {
    	//ADD UNITS
    	UnitDefinition ud1 = m.createUnitDefinition("M");
    	ud1.addUnit("");
    	UnitDefinition ud2 = m.createUnitDefinition("mM");
    	Unit u2 = ud2.createUnit();
    	u2.setId("");
    	UnitDefinition ud3 = m.createUnitDefinition("uM");
    	Unit u3 = ud3.createUnit();
    	u3.setId("");
    	UnitDefinition ud4 = m.createUnitDefinition("nM");
    	Unit u4 = ud4.createUnit();
    	u4.setId("");
    	UnitDefinition ud5 = m.createUnitDefinition("pM");
    	Unit u5 = ud5.createUnit();
    	u5.setId("");
    	UnitDefinition ud6 = m.createUnitDefinition("fM");
    	Unit u6 = ud6.createUnit();
    	u6.setId("");
    	UnitDefinition ud7 = m.createUnitDefinition("MOLECULE");
    	ud7.addUnit("item");
    }
    
    private static Species createSpecies(MolecularSpecies ms, String displayID, Model model, Compartment compartment) {
		Species species = model.createSpecies(displayID, compartment);
		species.setHasOnlySubstanceUnits(false);
		species.setBoundaryCondition(false);
		species.setConstant(false);
		if (ms.getAmount() != 0) species.setInitialAmount(ms.getAmount());
		else species.setInitialAmount(0);
		if(ms.getUnit() != null) {
			String unit = ms.getUnit().getLiteral();
			if (model.findUnitDefinition(unit) == null) model.createUnitDefinition(unit);
			species.setUnits(unit);
			if (unit == "MOLECULE") species.setHasOnlySubstanceUnits(true);
		}
		return species;
    }
    
    private static void setReplacement(MolecularSpecies ms, String displayId, String submodelRef,
    		Model model, Model subModel, Compartment compartment, CompModelPlugin plugin) {
		String mName = fixDisplayID(ms.getDisplayName());
		createSpecies(ms, displayId, subModel, compartment);
    	if (model.containsSpecies(mName + "_molecule")) {
			Species replacementSpecies = model.getSpecies(mName + "_molecule");
			CompSBasePlugin speciesPlugin = getCompSBasePlugin(replacementSpecies);

			Port speciesPort = plugin.createPort();
			speciesPort.setId(mName + "_species_port");
			speciesPort.setIdRef(displayId);
			
			ReplacedElement reSpecies = speciesPlugin.createReplacedElement();
			reSpecies.setSubmodelRef(submodelRef);
			reSpecies.setPortRef(mName + "_species_port");
		}
    }
    
	private static void setRule(Rule r, Model model, Compartment compartment) {
		int uniqify = 1;
		String displayID = fixDisplayID(r.getDisplayName());
		String newDisplayID = displayID;
		while (model.getReaction(newDisplayID) != null) {
			newDisplayID = displayID + uniqify;
			uniqify++;
		}
		Reaction reaction = model.createReaction(newDisplayID);
		reaction.setCompartment(compartment);
		reaction.setSBOTerm("SBO:0000412"); //Biological activity
		reaction.setFast(false);
		for(MolecularSpecies ms : r.getLeftHandSide()) {
			String mName = fixDisplayID(ms.getDisplayName()) + "_molecule";
			if (!model.containsSpecies(mName)) createSpecies(ms, mName, model, compartment);
			SpeciesReference reactant = reaction.createReactant(model.getSpecies(mName));
			reactant.setConstant(false);
		}
		for(MolecularSpecies ms : r.getRightHandSide()) {
			String mName = fixDisplayID(ms.getDisplayName()) + "_molecule";
			if (!model.containsSpecies(mName)) createSpecies(ms, mName, model, compartment);
			SpeciesReference product = reaction.createProduct(model.getSpecies(mName));
			product.setConstant(false);
		}
		if (r.isIsBidirectional()) reaction.setReversible(true);
		else reaction.setReversible(false);
		KineticLaw k = reaction.createKineticLaw();
		//set kinetic law. how do i generate mathml
    }
    
    private static void convertMolecules(List<MolecularSpecies> molecules, Model model, Compartment compartment) {
		for (MolecularSpecies ms : molecules) {
			createSpecies(ms, fixDisplayID(ms.getDisplayName()) + "_molecule", model, compartment);
		}
    }
    
    private static void convertDevices(List<Device> devices, Model cModel, CompModelPlugin cModelPlugin, CompSBasePlugin cBasePlugin, CompSBMLDocumentPlugin compDoc) {
    	for (Device d : devices) {
			String deviceName = fixDisplayID(d.getDisplayName());
			Model dModel = compDoc.createModelDefinition(deviceName);
			CompModelPlugin dModelPlugin = getCompModelPlugin(dModel);
    		Submodel dSubmodel = new Submodel(deviceName + "_submodel", level, version);
    		dSubmodel.setModelRef(deviceName);
    		cModelPlugin.addSubmodel(dSubmodel);
			
			Compartment dCompartment = dModel.createCompartment(deviceName + "_compartment");
			dCompartment.setConstant(true);
			Port devicePort = dModelPlugin.createPort();
			devicePort.setId(deviceName + "_compartment_port");
			devicePort.setIdRef(deviceName + "_compartment");
			ReplacedElement re = cBasePlugin.createReplacedElement();
			re.setSubmodelRef(deviceName + "_submodel");
			re.setPortRef(deviceName + "_compartment_port");			

			//Create map of each species and how many times it occurs on left and right hand sides.
			//If it appears equal numbers on left and right, its a modifier.
			//If it appears more on left than right, its a reactant. Stoichiometry is left - right.
			//Same for more on right than left.
			//Kinetic law is take forward rate times each of reactants you found, raised to the power of its stoichiometry. If it is bidirectional, incorporate backwards reaction too. Forward - reverse rate.
			
			convertMolecules(d.getMoleculeList(), dModel, dCompartment);
			
			for (MolecularSpecies ms : d.getInputList()) {
				setReplacement(ms, fixDisplayID(ms.getDisplayName()) + "_molecule", deviceName + "_submodel", cModel, dModel, dCompartment, dModelPlugin);
			}
			for (MolecularSpecies ms : d.getOutputList()) {
				setReplacement(ms, fixDisplayID(ms.getDisplayName()) + "_molecule", deviceName + "_submodel", cModel, dModel, dCompartment, dModelPlugin);
			}
			
    		for (Rule r : d.getRuleList()) {
				setRule(r, dModel, dCompartment);
			}

		}
    }
    
    private static void convertCells(List<Cell> cells, CompModelPlugin bModelPlugin, CompSBMLDocumentPlugin compDoc) {
		for (Cell c : cells) {
    		
			String cellName = fixDisplayID(c.getDisplayName());
    		Model cModel = compDoc.createModelDefinition(cellName);
    		Compartment cCompartment = cModel.createCompartment(cellName + "_compartment");
    		cCompartment.setConstant(true);
    		CompModelPlugin cModelPlugin = getCompModelPlugin(cModel);
			CompSBasePlugin cBasePlugin = getCompSBasePlugin(cCompartment);
    		
    		Submodel cSubmodel = new Submodel(cellName + "_submodel", level, version);
    		cSubmodel.setModelRef(c.getDisplayName());
    		bModelPlugin.addSubmodel(cSubmodel);
    		
    		for (MolecularSpecies ms : c.getMoleculeList()) {
    			if(ms.getBiologicalType() == "DNA") continue;
    			createSpecies(ms, fixDisplayID(ms.getDisplayName()) + "_molecule", cModel, cCompartment);
    		}
    		
    		for (Rule r : c.getRuleList()) {
    			setRule(r, cModel, cCompartment);
    		}
    		
    		convertDevices(c.getDeviceList(), cModel, cModelPlugin, cBasePlugin, compDoc);

    	}
    }
    
    private static void convertRegions(List<Region> regions, CompModelPlugin bModelPlugin, CompSBMLDocumentPlugin compDoc) {
    	for (Region r : regions) {
    		
    		String regionName = fixDisplayID(r.getDisplayName());
    		Model rModel = compDoc.createModelDefinition(regionName);
    		CompModelPlugin rModelPlugin = getCompModelPlugin(rModel);
    		
    		Submodel rSubmodel = new Submodel(regionName + "_submodel", level, version);
    		rSubmodel.setModelRef(regionName);
    		bModelPlugin.addSubmodel(rSubmodel);
    		
    		convertCells(r.getCellList(), rModelPlugin, compDoc);
    
    	}
    }
    
    public static SBMLDocument makeSBMLDocument(roadblock.emf.ibl.Ibl.Model model, FlatModel flatModel) {
		SBMLDocument doc = new SBMLDocument(level, version);
		doc.enablePackage(CompConstants.namespaceURI);
		CompSBMLDocumentPlugin compDoc = getCompDocPlugin(doc);
		String modelName = (model != null ? fixDisplayID(model.getDisplayName()) : "Flat_Model");
		Model bioModel = doc.createModel(modelName);
		createUnits(bioModel);
		
		CompModelPlugin bModelPlugin = getCompModelPlugin(bioModel);
    	
		if (model == null) {
    		Compartment bCompartment = bioModel.createCompartment("Flat_Model_compartment");
    		bCompartment.setConstant(true);
			convertMolecules(flatModel.getMoleculeList(), bioModel, bCompartment);
			for (Rule r : flatModel.getRuleList()) {
				setRule(r, bioModel, bCompartment);
			}
			return doc;
		}
		
    	List<Region> regions = model.getRegionList();
    	List<Cell> cells = model.getCellList();
    	List<Device> devices = model.getDeviceList();
    	List<MolecularSpecies> molecules = model.getMoleculeList();
    	
    	if (regions != null && !regions.isEmpty()) {
    		convertRegions(regions, bModelPlugin, compDoc);
    	}
    	else if (cells != null && !cells.isEmpty()) {
    		convertCells(cells, bModelPlugin, compDoc);
    	}
    	else {
    		Compartment cCompartment = bioModel.createCompartment(modelName + "_compartment");
    		cCompartment.setConstant(true);
			CompSBasePlugin cBasePlugin = getCompSBasePlugin(cCompartment);
        	if (devices != null && !devices.isEmpty()) {
        		convertDevices(devices, bioModel, bModelPlugin, cBasePlugin, compDoc);
        	}
        	else {
        		convertMolecules(molecules, bioModel, cCompartment);
        	}
    	}
		
		ID = 1;
    	return doc;
    	
    }
}
