package eu.tanov.android.bptcommon;

public interface EstimatesResolver {

	public void query();
	
	public void showResult(boolean onlyBuses);
	
	public String getResultAsString();
	
	public boolean hasBusSupport();
}
