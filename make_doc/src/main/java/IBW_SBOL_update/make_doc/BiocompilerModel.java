package IBW_SBOL_update.make_doc;

import java.util.ArrayList;

public class BiocompilerModel {
	
	public ArrayList<Cell> cells = new ArrayList<Cell>();
	public String name;
	
	public void addCell(String cellname, ArrayList<Device> cellDevices){
		Cell c = new Cell();
		c.name = cellname;
		c.devices = cellDevices;
		cells.add(c);
	}
	
	BiocompilerModel(String modelName) {
		name = modelName;
	}
	
}