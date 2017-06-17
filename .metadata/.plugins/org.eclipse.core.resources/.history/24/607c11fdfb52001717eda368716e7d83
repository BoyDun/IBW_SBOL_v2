package IBW_SBOL_update.make_doc;

public class Biopart {
	
	final public int sequenceLength;
	final public String name;
	final public String biologicalFunction;
	final public String accessionURL;
	final public int direction;
	final public Position position;
	final public String sequence;
	
	Biopart(int length, String partName, String function, String URL, int partDirection, int positionValue, String partSequence){
		sequenceLength = length;
		name = partName;
		biologicalFunction = function;
		accessionURL = URL;
		direction = partDirection;
		position = new Position(positionValue);
		sequence = partSequence;
	}
	
	public int getPosition() {
		return position.getValue();
	}
	
}
