package eu.tanov.android.sptn.favorities;

public class BusStopItem {

    private final String provider;
    private final int position;
	private final String code;
	private final String label;
	
	public BusStopItem(String provider, int position, String code, String label) {
	    this.provider = provider;
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
	public String getProvider() {
        return provider;
    }
	@Override
	public String toString() {
		return label;
	}
	
}
