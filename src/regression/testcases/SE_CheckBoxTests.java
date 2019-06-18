package regression.testcases;

import java.util.ArrayList;
import java.util.List;

import org.safs.Constants.BrowserConstants;
import org.safs.Domains;
import org.safs.StringUtils;
import org.safs.Utils;
import org.safs.model.commands.DDDriverCommands;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.WDLibrary;

import regression.Map;
import regression.testruns.Regression;

public class SE_CheckBoxTests extends Regression{

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

		String browsers = Map.TestBrowserName();
		if(browsers==null || browsers.trim().isEmpty()){
			browsers = FF;
			Logging.LogTestWarning(counterID+" cannot get TestBrowserName from map, use "+browsers);
		}
		browsers = browsers.replaceAll(" +", " ");
		String[] browserArray = browsers.split(" ");

		for(String browser: browserArray){
			if(Domains.isHtmlEnabled()) fail += testAPIForHtml(counterID, browser);
			if(Domains.isDojoEnabled()) fail += testAPIForDojo(counterID, browser);
			if(Domains.isSapEnabled()) fail+= testAPIForSAP(counterID, browser);

			if(SelectBrowser.BROWSER_NAME_CHROME.equalsIgnoreCase(browser)){
				testNetworkControl(counterID, browser);
			}
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

	private static int testAPIForHtml(String counterPrefix, String browser) throws Throwable{
		int fail = 0;
		String mapID = MAP_FILE_HTMLAPP;
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);

		if(Misc.SetApplicationMap(mapID)){

		}else{
			trace(++fail);
			Logging.LogTestFailure(counterID+"Fail to load map '"+mapID+"', cannot test in browser '"+browser+"'!");
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

	private static int testAPIForDojo(String counterPrefix, String browser) throws Throwable{
		int fail = 0;
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);
		String mapID = MAP_FILE_DOJOAPP;

		if(Misc.SetApplicationMap(mapID)){

		}else{
			trace(++fail);
			Logging.LogTestFailure(counterID+"Fail to load map '"+mapID+"', cannot test in browser '"+browser+"'!");
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

	private static int testAPIForSAP(String counterPrefix, String browser) throws Throwable{
		int fail = 0;
		String mapID = MAP_FILE_SAPDEMOAPP;
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);
		String debugmsg = StringUtils.debugmsg(false);

		if(Misc.SetApplicationMap(mapID)){
			String ID = null;

			try{
				ID = startBrowser(browser, Map.SAPDemoURL());
				if(ID!=null){
					if(TabControl.SelectTab(Map.SAPDemoPage.TabControl, Map.Tab_basc_comp())){
						fail += sap_test_checkbox(counterID, Map.SAPDemoPage.Basc_CheckBox);
					}else{
						Logging.LogTestWarning(debugmsg+"SelectTab Fail to select tab '"+Map.Tab_basc_comp()+"'. All tests missed.");
						trace(++fail);
					}
				}else{
					Logging.LogTestWarning(counterID+"StartWebBrowser '"+browser+"' Unsuccessful.");
					trace(++fail);
				}
			}catch(Exception e){
				trace(++fail);
				Logging.LogTestFailure(counterID+"Fail to test SAP Application in browser '"+browser+"'! Unexpected Exception "+StringUtils.debugmsg(e));
			}finally{
				if(ID!=null) if(!StopWebBrowser(ID)) trace(++fail);
			}

			Misc.CloseApplicationMap(mapID);
		}else{
			trace(++fail);
			Logging.LogTestFailure(counterID+"Fail to load map '"+mapID+"', cannot test in browser '"+browser+"'!");
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

	@SuppressWarnings("rawtypes")
	private static int testNetworkControl(String counterPrefix, String browser) throws Throwable{
		int fail = 0;
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);

		String ID = null;

		try{
			String URL = Map.GoogleURL();
			String fastNetwork = Map.networkConditions();
			String slowNetwork = Map.networkConditionsSlow();
			String networkResult = null;
			ID = startBrowser(browser, URL, BrowserConstants.KEY_SET_NETWORK_CONDITIONS, fastNetwork);
			if(ID!=null){
				networkResult = DriverCommand.GetNetworkConditions();
				java.util.Map networkConditionsBench = Utils.fromJsonString(fastNetwork, java.util.Map.class);
				java.util.Map networkConditions = Utils.fromJsonString(networkResult, java.util.Map.class);

				if(!networkConditionsBench.equals(networkConditions)){
					Logging.LogTestWarning(counterID+"networkConditions '"+networkResult+"' doesn't equal to networkConditionsBench '"+fastNetwork+"'");
					trace(++fail);
				}

				//Delete the initial 'network condition'.
				if(!DriverCommand.DeleteNetworkConditions()) trace(++fail);
				if(DriverCommand.GetNetworkConditions()!=null) trace(++fail);

				//Switch to a slow connection
				if(!DriverCommand.SetNetworkConditions(slowNetwork)) trace(++fail);
				networkResult = DriverCommand.GetNetworkConditions();
				networkConditionsBench = Utils.fromJsonString(slowNetwork, java.util.Map.class);
				networkConditions = Utils.fromJsonString(networkResult, java.util.Map.class);

				if(!networkConditionsBench.equals(networkConditions)){
					Logging.LogTestWarning(counterID+"networkConditionsSlow '"+networkResult+"' doesn't equal to networkConditionsBench '"+slowNetwork+"'");
					trace(++fail);
				}

				//Open the same url again, We will see that it is slower than the first time.
				WDLibrary.getWebDriver().get(URL);
			}else{
				Logging.LogTestWarning(counterID+"StartWebBrowser '"+browser+"' Unsuccessful.");
				trace(++fail);
			}
		}catch(Exception e){
			trace(++fail);
			Logging.LogTestFailure(counterID+"Fail to test SAP Application in browser '"+browser+"'! Unexpected Exception "+StringUtils.debugmsg(e));
		}finally{
			if(ID!=null) if(!StopWebBrowser(ID)) trace(++fail);
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

	private static int sap_test_checkbox(String counterPrefix, org.safs.model.Component checkbox) throws Throwable{
		int fail = 0;
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);

		if(!CheckBox.Check(checkbox)) trace(++fail);
		Pause(1);
		if(!CheckBox.UnCheck(checkbox)) trace(++fail);
		Pause(1);

		Counters.StopCounter(counterID);
		Counters.StoreCounterInfo(counterID, counterID);
		Counters.LogCounterInfo(counterID);

		return fail;
	}

	/**
	 *
	 * @return
	 * @throws Throwable
	 */
	public static int runRegressionTest(List<String> enabledDomains) throws Throwable{
		int fail = 0;
		Counters.StartCounter(COUNTER);

		try{
			Runner.command(DDDriverCommands.USESELENIUMFUNCTIONS_KEYWORD, "ON");

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

	@Override
	public void runTest() throws Throwable{
		List<String> enabledDomains = new ArrayList<String>();
		enabledDomains.add(Domains.HTML_SAP_DOMAIN);
		initUtils();
		runRegressionTest(enabledDomains);
	}

}