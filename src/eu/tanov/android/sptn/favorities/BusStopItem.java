package eu.tanov.android.sptn.favorities;

public class BusStopItem {

	private final int position;
	private final String code;
	private final String label;
	
	public BusStopItem(int position, String code, String label) {
		this.position = position;
		this.code = code;
		this.label = label;
	}
	
	public int getPosition() {
		return position;
	}
	public String getCode() {
		return code;
	}
	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return label;
	}
	
}
