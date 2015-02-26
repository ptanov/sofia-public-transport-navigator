package eu.tanov.android.sptn.sumc;

public interface EstimatesResolver {

	public void query();
	
	public void showResult(boolean onlyBuses);
	
	public boolean hasBusSupport();
}
