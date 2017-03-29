package regression.testcases;

import java.util.ArrayList;
import java.util.List;

import org.safs.Domains;
import org.safs.StringUtils;
import org.safs.model.commands.DDDriverCommands;

import regression.Map;
import regression.testruns.Regression;

public class RJ_ComboBoxTests extends Regression{

	public static final String COUNTER = StringUtils.getClassName(0, false);


	private static int testAPI(String counterPrefix) throws Throwable{
		int fail = 0;
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);

		if(Domains.isJavaEnabled()) fail+= testAPIForJava(counterID);
		
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

	private static int testAPIForJava(String counterPrefix) throws Throwable{
		int fail = 0;
		String mapID = "RJ_swing.map";
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);
		String debugmsg = StringUtils.debugmsg(false);
		String application = "javaw -jar C:\\SAFS\\samples\\JavaApp\\swingapp.jar";

		if(Misc.SetApplicationMap(mapID)){
			String ID = "Javademo";

			try{
//				ID = launchApplication(quote(application));
				if(ID!=null){
					if(Click(Map.SwingWindow.BascComp)){
						fail += test_combobox(counterID, Map.SwingWindow.ComboBox);
					}else{
						Logging.LogTestWarning(debugmsg+"SelectTab Fail to select tab '"+Map.Tab_basc_comp()+"'. All tests missed.");
						trace(++fail);
					}
				}else{
					Logging.LogTestWarning(counterID+"LaunchApplication '"+application+"' Unsuccessful.");
					trace(++fail);
				}
			}catch(Exception e){
				trace(++fail);
				Logging.LogTestFailure(counterID+"Fail to test Application '"+application+"'! Unexpected Exception "+StringUtils.debugmsg(e));
			}finally{
//				if(ID!=null) if(!Misc.CloseApplication(ID)) trace(++fail);
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

	private static int test_combobox(String counterPrefix, org.safs.model.Component combobox) throws Throwable{
		int fail = 0;
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);

		if(!ComboBox.ShowList(combobox)) trace(++fail);
		Pause(1);
		if(!ComboBox.Select(combobox, "Test8")) trace(++fail);
		Pause(1);

		Counters.StopCounter(counterID);
		Counters.StoreCounterInfo(counterID, counterID);
		Counters.LogCounterInfo(counterID);
		
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
			Runner.command(DDDriverCommands.USEROBOTJFUNCTIONS_KEYWORD, "ON");
			
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
		enabledDomains.add(Domains.JAVA_DOMAIN);
		initUtils();
		runRegressionTest(enabledDomains);
	}

}