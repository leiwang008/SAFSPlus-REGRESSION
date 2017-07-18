package regression.testcases;

import java.util.ArrayList;
import java.util.List;

import org.safs.Domains;
import org.safs.StringUtils;
import org.safs.persist.PersistenceType;
import org.safs.rest.service.Request;
import org.safs.rest.service.Response;
import org.safs.text.FileUtilities.FileType;

import regression.Map;
import regression.testruns.Regression;

public class TIDRESTTests extends Regression{

	public static final String COUNTER = StringUtils.getClassName(0, false);

	/**
	 * <B>NOTE:</B>
	 * <pre>
	 * 1. The static field 'utils' should have been initialized, utils = new Utilities(Runner.jsafs());
	 * </pre>
	 *
	 * @return int, the number of error occurs
	 * @throws Throwable
	 */
	private static int testAPI(String counterPrefix) throws Throwable{
		int fail = 0;
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);

		fail+= testRESTAPIs_withBayerService(counterID);
		fail+= restTestOauth2_withSASFolderService(counterID);

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

	private static final String[] responseSuffixes = (new Response()).getPersitableFields().values().toArray(new String[0]);
	private static final String[] requestSuffixes = (new Request()).getPersitableFields().values().toArray(new String[0]);

	private static void LogResponseVariables(String variablePrefix, boolean includeRequest){
		String var = null;

		for(String suffix: responseSuffixes){
			var = variablePrefix+".Response."+suffix;
			Logging.LogMessage(var+"="+GetVariableValue(var));
			//^var will not be evaluated, as the implementation of LogMessage has quoted the message
//			Logging.LogMessage(var+"=^"+var);
		}

		if(includeRequest){
			for(String suffix: requestSuffixes){
				var = variablePrefix+".Request."+suffix;
				Logging.LogMessage(var+"="+GetVariableValue(var));
				//^var will not be evaluated, as the implementation of LogMessage has quoted the message
//				Logging.LogMessage(var+"=^"+var);
			}
		}
	}

	private static int testRESTAPIs_withBayerService(String counterPrefix) throws Throwable{
		int fail = 0;
		String mapID = "Rest.Map";
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);
		String responseID = null;
		String responseFile = null;
		String persistenceType = null;
		String fileType = null;

		if(Misc.SetApplicationMap(mapID)){
			try{

				if(Rest.StartServiceSession(Map.BAYERREST.BAYERSERVICE001, quote(Map.URL_BAYER_SQLREST())/*baseURL*/, quote(Map.AUTH_MARK_SIMPLE())/*auth info*/ )){
					responseID = Map.ID_RESPONSE_CUSTOMER_GET();
					if(Rest.GetXML(Map.BAYERREST.BAYERSERVICE001, Map.PATH_CUSTOMER(), responseID)){
						if(!Rest.CleanResponseMap()){
							trace(++fail);
						}else{
							//As the internal Map has been cleaned, the following calls should fail
							if(Rest.StoreResponse("^"+responseID, Map.VAR_CUSTOMER_GET())){
								trace(++fail);
							}else{
								//We should NOT get the response content-type from the variable storage
								LogResponseVariables(Map.VAR_CUSTOMER_GET(), false);
							}

							if(Rest.DeleteResponse("^"+responseID)){
								trace(++fail);
							}
						}
					}else{
						trace(++fail);
					}

					//This OAUTH2 configuration file does NOT take any effect for now.
					if(Rest.GetXML(Map.BAYERREST.BAYERSERVICE001, Map.PATH_CUSTOMER(), responseID, "", "", quote(Map.AUTH_MARK_OAUTH2())/*auth info*/ )){
						if(Rest.StoreResponse("^"+responseID, Map.VAR_CUSTOMER_GET())){
							LogResponseVariables(Map.VAR_CUSTOMER_GET(), false);

							if(!Rest.DeleteResponse("^"+responseID)){
								trace(++fail);
							}else{
								//We should NOT get the response content-type from the variable storage, as the variable has been deleted
								LogResponseVariables(Map.VAR_CUSTOMER_GET(), false);
							}
						}else{
							trace(++fail);
						}

						persistenceType = PersistenceType.FILE.name;
						fileType = FileType.XML.name;
						responseFile = responseID+"."+fileType;
						//We ONLY store Response (no Request will be saved) to XML file.
						if(!Rest.StoreResponse("^"+responseID, responseFile, "false", persistenceType, fileType)){
							trace(++fail);
						}else{
							//Try to test the VerifyResponse keywords
							if(!Files.CopyFile(quote(utils.testFile(responseFile)), quote(utils.benchFile(responseFile)))){
								trace(++fail);
							}else{
								if(Rest.VerifyResponse("^"+responseID, responseFile)){
									//Default file format is JSON, should fail at this moment
									trace(++fail);
								}
								if(!Rest.VerifyResponse("^"+responseID, responseFile, fileType)){
									trace(++fail);
								}

								//Should fail, because there is no Request in the benchmark Response file
								if(Rest.VerifyResponse("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/)){
									trace(++fail);
								}
								//Should succeed, even there is no Request information in the bench file, we called VerifyResponseContains
								if(!Rest.VerifyResponseContains("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/)){
									trace(++fail);
								}
							}
						}

						//We store both Response and Request to XML file.
						if(!Rest.StoreResponse("^"+responseID, responseFile, "true", persistenceType, fileType)){
							trace(++fail);
						}else{
							//Try to test the VerifyResponse keywords
							if(!Files.CopyFile(quote(utils.testFile(responseFile)), quote(utils.benchFile(responseFile)))){
								trace(++fail);
							}else{
								//Should succeed, because the Request is ignored, and it will not be verified
								if(!Rest.VerifyResponse("^"+responseID, responseFile, fileType)){
									trace(++fail);
								}

								if(!Rest.VerifyResponse("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/)){
									trace(++fail);
								}

								if(!Rest.VerifyResponseContains("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/)){
									trace(++fail);
								}

								if(!Rest.VerifyResponseContains("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/, "false"/*valueContains*/, "true"/*caseSensitive*/)){
									trace(++fail);
								}
							}
						}

						//We store both Response and Request to a properties file
						fileType = FileType.PROPERTIES.name;
						responseFile = responseID+"."+fileType;
						if(!Rest.StoreResponse("^"+responseID, responseFile, "true", persistenceType, fileType)){
							trace(++fail);
						}else{
							//Try to test the VerifyResponse keywords
							if(!Files.CopyFile(quote(utils.testFile(responseFile)), quote(utils.benchFile(responseFile)))){
								trace(++fail);
							}else{
								//Should succeed, because the Request is ignored, and it will not be verified
								if(!Rest.VerifyResponse("^"+responseID, responseFile, fileType)){
									trace(++fail);
								}

								if(!Rest.VerifyResponse("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/)){
									trace(++fail);
								}

								if(!Rest.VerifyResponseContains("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/)){
									trace(++fail);
								}

								if(!Rest.VerifyResponseContains("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/, "false"/*valueContains*/, "true"/*caseSensitive*/)){
									trace(++fail);
								}
							}
						}

						//Will be saved in the default file format, currently the default format is "json"
						responseFile = responseID+"_2."+FileType.get(null);
						if(!Rest.StoreResponse("^"+responseID, responseFile, "true", persistenceType)){
							trace(++fail);
						}

						fileType = FileType.JSON.name;
						responseFile = responseID+"."+fileType;
						if(Rest.StoreResponse("^"+responseID, responseFile, "true", persistenceType, fileType)){

							//Try to test the VerifyResponse keywords
							if(!Files.CopyFile(quote(utils.testFile(responseFile)), quote(utils.benchFile(responseFile)))){
								trace(++fail);
							}else{
								//Should succeed, because the Request is ignored, and it will not be verified
								if(!Rest.VerifyResponse("^"+responseID, responseFile, fileType)){
									trace(++fail);
								}

								if(!Rest.VerifyResponse("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/)){
									trace(++fail);
								}

								if(!Rest.VerifyResponseContains("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/)){
									trace(++fail);
								}

								if(!Rest.VerifyResponseContains("^"+responseID, responseFile, fileType, ""/* variableName*/, "true"/*verifyRequest*/, "false"/*valueContains*/, "true"/*caseSensitive*/)){
									trace(++fail);
								}
							}

//							if(!Rest.DeleteResponse("^"+responseID)){
//								trace(++fail);
//							}
						}else{
							trace(++fail);
						}

					}else{
						trace(++fail);
					}

					//==================================  Test Request for method "PUT"  =========================
					responseID = Map.ID_RESPONSE_PRODUCT_PUT();
					if(Rest.Request(Map.BAYERREST.BAYERSERVICE001, "PUT", Map.PATH_PRODUCT_N(), responseID, Map.BODY_PRODUCT_PUT(), Map.HEADER_PRODUCT_PUT())){
						if(!Rest.StoreResponse("^"+responseID, Map.VAR_PRODUCT_PUT())){
							trace(++fail);
						}
					}else{
						trace(++fail);
					}

					//==================================  Test HeaderXXX  =========================
					responseID = Map.ID_RESPONSE_CUSTOMER_HEADER();
					fileType = FileType.JSON.name;
					responseFile = responseID+"."+fileType;
					if(Rest.HeaderXML(Map.BAYERREST.BAYERSERVICE001, Map.PATH_CUSTOMER(), responseID)){
						if(!Rest.StoreResponse("^"+responseID, responseFile, "true", PersistenceType.FILE.name(), FileType.JSON.name() )){
							trace(++fail);
						}

					}else{
						trace(++fail);
					}

//					if(!Rest.DeleteResponseStore()){
//						trace(++fail);
//					}

					if(!Rest.EndServiceSession(Map.BAYERREST.BAYERSERVICE001)){
						trace(++fail);
					}
				}else{
					trace(++fail);
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

	private static int restTestOauth2_withSASFolderService(String counterPrefix) throws Throwable{
		int fail = 0;
		String mapID = "Rest.Map";
		String counterID = Regression.generateCounterID(counterPrefix, StringUtils.getMethodName(0, false));
		Counters.StartCounter(counterID);
		String responseID = null;
		String responseFile = null;
		String persistenceType = null;
		String fileType = null;
		String headers = null;

		if(Misc.SetApplicationMap(mapID)){
			try{

				//Start the session without OAUTH2 configuration, then the following REST request SHOULD FAIL
				if(Rest.StartServiceSession(Map.SASFolderREST.SASFolderService001, quote(Map.URL_SAS_FOLDER())/*baseURL*/ )){
					responseID = Map.ID_RESPONSE_FOLDER_GET();
					headers = Map.SASFolderService001.FolderServiceHeader.getName();
					Logging.LogFailureOK("'GetCustom' is expected to fail.", "The session didn't start with a correct auth configuration.");
					//If the following setting is put into the .ini file, then the following test will pass
					//Currently a wrong file is assigned to "AUTH" in the .ini file, so the following test SHOULD fail.
					//If we change "AUTH" to the correct value, an un-expected failure will happen.
					//---  .ini configure file -------
					//  [SAFS_REST]
					//  AUTH=config\sas_folder_auth.xml
					//---------------------------------
					if(Rest.GetCustom(Map.SASFolderREST.SASFolderService001, Map.PATH_ROOTFolders(), responseID, null, headers /*headers info*/)){
						trace(++fail);
					}

					if(!Rest.EndServiceSession(Map.SASFolderREST.SASFolderService001)){
						trace(++fail);
					}
				}else{
					trace(++fail);
				}

				//Start the session with OAUTH2 configuration, then the following REST request should succeed with that authorization/authentication
				if(Rest.StartServiceSession(Map.SASFolderREST.SASFolderService001, quote(Map.URL_SAS_FOLDER())/*baseURL*/, quote(Map.AUTH_MARK_SAS_FOLDER_OAUTH2())/*auth info*/ )){
					//Temporarily setting the proxy, which should be configured in the .ini file.
					//We don't need the proxy setting for internal SAS service
					//setServiceProxy(Map.BAYERREST.BAYERSERVICE001);

					responseID = Map.ID_RESPONSE_FOLDER_GET();
					headers = Map.SASFolderService001.FolderServiceHeaderFile.getName();

					if(Rest.GetCustom(Map.SASFolderREST.SASFolderService001, Map.PATH_ROOTFolders(), responseID, null, headers /*headers info*/)){
						//Store the response to the SAFS Variables
						if(Rest.StoreResponse("^"+responseID, Map.VAR_FOLDER_GET())){
							LogResponseVariables(Map.VAR_FOLDER_GET(), false);

							if(!Rest.DeleteResponse("^"+responseID)){
								trace(++fail);
							}else{
								//We should NOT get the response content-type from the variable storage, as the variable has been deleted
								LogResponseVariables(Map.VAR_FOLDER_GET(), false);
							}
						}else{
							trace(++fail);
						}

						persistenceType = PersistenceType.FILE.name;
						fileType = FileType.XML.name;
						responseFile = responseID+"."+fileType;
						//We ONLY store Response (no Request will be saved) to XML file.
						if(!Rest.StoreResponse("^"+responseID, responseFile, "false", persistenceType, fileType)){
							trace(++fail);
						}else{
							//Try to test the VerifyResponse keywords
							if(!Files.CopyFile(quote(utils.testFile(responseFile)), quote(utils.benchFile(responseFile)))){
								trace(++fail);
							}else{
								if(!Rest.VerifyResponse("^"+responseID, responseFile, fileType)){
									trace(++fail);
								}
							}
						}

//						if(!Rest.DeleteResponse("^"+responseID)){
//							trace(++fail);
//						}

					}else{
						trace(++fail);
					}

//					if(!Rest.DeleteResponseStore()){
//						trace(++fail);
//					}

					if(!Rest.EndServiceSession(Map.SASFolderREST.SASFolderService001)){
						trace(++fail);
					}
				}else{
					trace(++fail);
				}

				//Start service with the "auth info" provided by map item
				if(Rest.StartServiceSession(Map.SASFolderREST.SASFolderService001, quote(Map.URL_SAS_FOLDER())/*baseURL*/, quote(Map.SASFolderService001.AuthFile.getName())/*auth info*/ )){
					if(!Rest.EndServiceSession(Map.SASFolderREST.SASFolderService001)){
						trace(++fail);
					}
				}else{
					trace(++fail);
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