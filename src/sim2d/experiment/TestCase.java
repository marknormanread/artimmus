package sim2d.experiment;

import org.w3c.dom.Document;

/**
 * Class encapsulates an individual test case: the parameter configuration file to be fed into the simulator; the path of the parameter being tested, and the value
 * that that parameter is assuming in the test. (note that all other parameters assume their default values). 
 * @author mark
 *
 */
public class TestCase 
{
	public Document parameters;									// the parameters file to be fed into the simulation. 
	public String parameterName;								// the parameter that is being tested.
	public String parameterValue;								// the value that the parameter being tested has assumed. 
	
	
	public TestCase(Document params, String paramName, String paramValue)
	{
		this.parameters = params;
		this.parameterName = paramName;
		this.parameterValue = paramValue;
	}

}
