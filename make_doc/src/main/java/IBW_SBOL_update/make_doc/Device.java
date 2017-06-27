package IBW_SBOL_update.make_doc;

import java.util.ArrayList;

public class Device {

	public ArrayList<Biopart> parts;
	public String name;
	
	Device(ArrayList<Biopart> deviceParts, String deviceName) {
		parts = deviceParts;
		name = deviceName;
	}
	
}
