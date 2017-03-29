package regression.testruns;

/**
 * History:
 * MAR 29, 2017 (Lei Wang) Removed testPersist(), which has been added as unit-test to PersistTest.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.safs.Constants.BrowserConstants;
import org.safs.Domains;
import org.safs.SAFSPlus;
import org.safs.StringUtils;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.CoreInterface;
import org.safs.xml.XMLTransformer;

import regression.testcases.SE_CheckBoxTests;
import regression.testcases.TIDFileTests;
import regression.testcases.TIDRESTTests;
import regression.testcases.TIDStringTests;
import regression.util.Utilities;

/**
 * <pre>
 * 	 java -cp %CLASSPATH% regression.testruns.Regression
 * </pre>
 *
 */
public class Regression extends SAFSPlus {

	/*
	 * Insert (generally) static testcase methods or setup/teardown methods below.
	 * You call these from your runTest() method for normal testing,
 	 * or from other testcases, testcase classes, or anywhere they are needed.
	 */

	public static final String IE = "explorer";
	public static final String CH = "chrome";
	public static final String FF = "firefox";

	public static String browserID;
	public static final String MAP_FILE_SAPDEMOAPP = "SapDemoApp.map";
	public static final String MAP_FILE_DOJOAPP = "DojoApp.map";
	public static final String MAP_FILE_HTMLAPP = "HtmlApp.map";
	public static final String MAP_FILE_MISC = "MiscTests.map";

	public static final String MAP_FILE_LOCALE_SUFFIX_EN = "_en";
	public static final String MAP_FILE_LOCALE_SUFFIX_ZH = "_zh";

	public static File logsdir = null; //deduced at runtime

	/** The utilities to deduce test folders, files and assets etc.*/
	protected static Utilities utils = null;

	protected static CoreInterface core = null;

	protected static String generateID(){
		return String.valueOf((new Date()).getTime());
	}

	/**
	 * Generate the counterID for testing method.
	 * @param counterPrefix, the counterID of the method calling current method.
	 * @param methodName, the current method's full name. E.g. 'className.lastMethodName'.
	 * @return 'counterID.lastMethodName' format String.
	 */
	public static String generateCounterID(String counterPrefix, String methodName){
		int pos = methodName.lastIndexOf(".");

		if (-1 == pos) {
			return counterPrefix + "." + methodName;
		} else{
			return counterPrefix + methodName.substring(methodName.lastIndexOf("."), methodName.length());
		}
	}

	public static String startBrowser(String browser, String url, String... params){
		if(browser==null) browser = BrowserConstants.BROWSER_NAME_FIREFOX;
		String browserID = generateID();

		String[] parameters = combineParams(params, browser, ""/*timeout will be default 30 seconds*/, ""/*isRemote default is true*/);

		if (!Misc.StartWebBrowser(url, browserID, parameters)) return null;

		return browserID;
	}

	public static String launchApplication(String executable, String... params){
		if(executable==null) return null;
		String ID = generateID();

		if (!Misc.LaunchApplication(ID, executable, params)) return null;

		return ID;
	}

	/**
	 * Print out the stack-trace (where error occur) to console.
	 * In the Eclipse console, it will be easy to trace the source code where the error occur by clicking the link.
	 * @param errorCount int, the local error count.
	 * @return int, the local error count.
	 */
	protected static int trace(int errorCount){
		System.out.println("Regression Trace Error '"+errorCount+"' at "+Thread.currentThread().getStackTrace()[2]);
		return errorCount;
	}

	/**
	 * Initialize the Utilities for getting test directories/files.<br>
	 * Called at the beginning of our local {@link #runTest()} and
	 * the subclass's local runTest such as {@link FilesTests#runTest()}.<br>
	 * @throws Throwable
	 */
	protected void initUtils() throws Throwable{
		try {
			if(utils==null) utils = new Utilities(Runner.jsafs());
			if(core==null) core = Runner.getDriver().iDriver().getCoreInterface();
		} catch (Exception e) {
			AbortTest("Cannot initialize the Test Utilities! Met "+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Internal. Can only be run AFTER the test runtime environment is initialized.
	 * Called at the beginning of our local runTest() and after calling of {@link #initUtils()}
	 *
	 */
	void preparePostProcessing(){
		logsdir = utils.getLogsDir();
	}

	protected static boolean Pause(int seconds){
		return Misc.Pause(seconds);
	}
	public static boolean Click(org.safs.model.Component comp, String... params){
		return Component.Click(comp, params);
	}
	public static boolean DoubleClick(org.safs.model.Component comp, String... params){
		return Component.DoubleClick(comp, params);
	}
	public static boolean TypeKeys(String keystrokes){
		return Component.TypeKeys(keystrokes);
	}
	public static boolean StartWebBrowser(String URL,String BrowserID, String... params){
		return DriverCommand.StartWebBrowser(URL, BrowserID, params);
	}
	public static boolean StopWebBrowser(String BrowserID){
		return DriverCommand.StopWebBrowser(BrowserID);
	}
	public static boolean GetGUIImage(org.safs.model.Component comp, String fileName, String... params){
		return Component.GetGUIImage(comp, fileName, params);
	}
	public static boolean VerifyGUIImageToFile(org.safs.model.Component comp, String benchFile, String... params){
		return Component.VerifyGUIImageToFile(comp, benchFile, params);
	}
	public static boolean VerifyValues(String value1, String value2, String... optionals){
		return Component.VerifyValues(value1, value2, optionals);
	}
	public static boolean Highlight(boolean OnOff){
		return DriverCommand.Highlight(OnOff);
	}
	public static boolean HoverScreenLocation(String coordination, String... optionals){
		return Component.HoverScreenLocation(coordination, optionals);
	}
	public static boolean VerifyBinaryFileToFile(String benchFile, String actualFile, String... optionals){
		return Component.VerifyBinaryFileToFile(benchFile, actualFile, optionals);
	}
	public static boolean VerifyFileToFile(String benchFile, String actualFile, String... optionals){
		return Component.VerifyFileToFile(benchFile, actualFile, optionals);
	}

	/**
	 * Run ALL enabled regression tests.
	 */
	@Override
	public void runTest() throws Throwable {

		initUtils();
		preparePostProcessing();

		List<String> enabledDomains = new ArrayList<String>();
		enabledDomains.add(Domains.HTML_DOMAIN);
		enabledDomains.add(Domains.HTML_DOJO_DOMAIN);
		enabledDomains.add(Domains.HTML_SAP_DOMAIN);

		int fail = 0;
		fail += SE_CheckBoxTests.runRegressionTest(enabledDomains);
		//User can un comment TC_CheckBoxTests if 'Test Complete' has been installed correctly
//		fail += TC_CheckBoxTests.runRegressionTest(enabledDomains);
		//User can un comment RJ_ComboBoxTests if 'IBM Functional Tester' has been installed correctly
//		fail += RJ_ComboBoxTests.runRegressionTest(enabledDomains);

		fail += TIDRESTTests.runRegressionTest(enabledDomains);
		fail += TIDStringTests.runRegressionTest(enabledDomains);
		fail += TIDFileTests.runRegressionTest(enabledDomains);

		if(fail > 0){
			Logging.LogTestFailure("Regression reports "+ fail +" UNEXPECTED test failures!");
		}else{
			Logging.LogTestSuccess("Regression did not report any UNEXPECTED test failures!");
		}

		//if running from Eclipse, no local main() is needed.
		//but if running from command-line, a local main() is usually needed.
		setExitCode(fail);
		setAllowExit(false); // already false by default, but just in-case
	}

	/** Added to accomodate post-test processing for HTML reports. */
	public static void main(String[] args){

		SAFSPlus.main(args);

		// Continue with post-test processing of HTML reports.

		if(logsdir instanceof File){
			try {
				File xmlfile = new CaseInsensitiveFile(logsdir, "Regression.xml").toFile();
				File xslfile = new CaseInsensitiveFile(logsdir, "regressionsummary.xsl").toFile();
				File outfile = new File(logsdir, "Regression_Summary.htm");
				XMLTransformer.transform(xmlfile, xslfile, outfile);

				xslfile = new CaseInsensitiveFile(logsdir, "failuresummary.xsl").toFile();
				outfile = new File(logsdir, "Regression_Failures.htm");
				XMLTransformer.transform(xmlfile, xslfile, outfile);

				xslfile = new CaseInsensitiveFile(logsdir, "timeConsumedSummary.xsl").toFile();
				outfile = new File(logsdir, "Regression_ConsumedTimeSummary.htm");
				XMLTransformer.transform(xmlfile, xslfile, outfile, XMLTransformer.XSLT_VERSION_2);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.exit(exitCode);
	}

}
