package regression.testcases;

import java.util.ArrayList;
import java.util.List;

import org.safs.Domains;
import org.safs.StringUtils;
import org.safs.text.FileUtilities.Access;
import org.safs.text.FileUtilities.Mode;

import regression.testruns.Regression;

public class TIDFileTests extends Regression{

	public static final String COUNTER = StringUtils.getClassName(0, false);

	/**
	 * <B>NOTE:</B>
	 * <pre>
	 * 1. The static field 'utils' should have been initialized, utils = new Utilities(Runner.jsafs());
	 * 2. TestBrowserName is suggested to be defined in the map file, if not defined, only firefox browser will be tested.
	 * [ApplicationContants]
	 * TestBrowserName="firefox"
	 * ;TestBrowserName="firefox chrome explorer"
	 * </pre>
	 * 
	 * @return int, the number of error occurs
	 * @throws Throwable
	 */
	private static int testAPI(String counterPrefix) throws Throwable{
		int fail = 0;
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);

		fail+= testFileAPIs(counterID);
		
		Counters.StopCounter(counterID);
		Counters.StoreCounterInfo(counterID, counterID);
		Counters.LogCounterInfo(counterID);
		
		if(fail > 0){
			Logging.LogTestFailure(counterID + " reports "+ fail +" UNEXPECTED test failures!");
		}else{
			Logging.LogTestSuccess(counterID + " did not report any UNEXPECTED test failures!");
		}
		
		return fail;
	}

	private static int testFileAPIs(String counterPrefix) throws Throwable{
		int fail = 0;
		String mapID = "Rest.Map";//TODO load its own map
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);

		if(Misc.SetApplicationMap(mapID)){

			try{
				Misc.Expressions(false);
				String result = "result";
				String file = "C:\\Temp\\restActions.html";
				String fileNo = null;//hold the file number for an opened file.
				String content = null;
				if(Files.OpenFile(file, Mode.INPUT, Access.R, result)){
					fileNo = GetVariableValue(result);
					System.out.println(file+" has been opened with file number '"+fileNo+"' for input.");

					int charsToRead = 15;
					if(Files.ReadFileChars(fileNo, charsToRead, result)){
						System.out.println(charsToRead+" characters: '"+GetVariableValue(result)+"' have been read from file '"+fileNo+"'");
					}
					if(Files.ReadFileLine(fileNo, result)){
						System.out.println(" line '"+GetVariableValue(result)+"' have been read from file '"+fileNo+"'");
					}
					
					//Read the rest
					Files.ReadFileChars(fileNo, -1, result);
					
					if(Files.CloseFile(fileNo))
						System.out.println("file '"+fileNo+"' has been closed.");
				}
				
			}catch(Exception e){
				trace(++fail);
				Logging.LogTestFailure(counterID+"Failed'! Unexpected Exception "+StringUtils.debugmsg(e));
			}

			Misc.CloseApplicationMap(mapID);
		}else{
			trace(++fail);
			Logging.LogTestFailure(counterID+"Fail to load map '"+mapID+"'!");
		}		

		Counters.StopCounter(counterID);
		Counters.StoreCounterInfo(counterID, counterID);
		Counters.LogCounterInfo(counterID);

		if(fail > 0){
			Logging.LogTestFailure(counterID + " reports "+ fail +" UNEXPECTED test failures!");
		}else{
			Logging.LogTestSuccess(counterID + " did not report any UNEXPECTED test failures!");
		}
		
		return fail;
	}


	/**
	 * 
	 * @param Runner EmbeddedHookDriverRunner
	 * @return
	 * @throws Throwable
	 */
	public static int runRegressionTest(List<String> enabledDomains) throws Throwable{
		int fail = 0;
		Counters.StartCounter(COUNTER);

		try{
			
			for(String domain: enabledDomains) Domains.enableDomain(domain);
			fail += testAPI(COUNTER);

		}catch(Throwable t){
			trace(++fail);
			Logging.LogTestFailure(COUNTER +" fatal error due to "+t.getClass().getName()+", "+ t.getMessage());
		}

		Counters.StopCounter(COUNTER);
		Counters.StoreCounterInfo(COUNTER, COUNTER);
		Counters.LogCounterInfo(COUNTER);

		if(fail > 0){
			Logging.LogTestFailure(COUNTER+" reports "+ fail +" UNEXPECTED test failures!");
		}else{
			Logging.LogTestSuccess(COUNTER+" did not report any UNEXPECTED test failures!");
		}
		return fail;
	}

	public void runTest() throws Throwable{
		List<String> enabledDomains = new ArrayList<String>();
		initUtils();
		runRegressionTest(enabledDomains);
	}

}