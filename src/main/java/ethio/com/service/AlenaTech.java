package ethio.com.service;

import ethio.com.models.Request;
import ethio.com.session.SessionManager;
import ethio.com.dao.CustomerInfoViewDao;
import ethio.com.dto.RequestResponseCol;
import ethio.com.models.CustomerInfoView;
import ethio.com.models.L10n;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.swing.text.html.Option;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Entity;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.json.JSONObject;
import javax.net.ssl.SSLContext;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
@Path("/ethio")
@RequestScoped
@Stateful
public class AlenaTech {
//    private static final Logger LOGGER = Logger.getLogger(AlenaTech.class.getName());
private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    @PersistenceContext(unitName = "primary")
    private EntityManager em;
    @EJB
    CustomerInfoViewDao customerInfoViewDao;
    private static final String nextCharacter = "+";
    private static final int maxBankListValue = 8;
    private static final int accountListStartIteration = 3;
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response call(final RequestResponseCol reqres) throws Exception {
        List<CustomerInfoView> customerAccounts = this.customerInfoViewDao.getAccounts(reqres.getMsisdn());
        javax.ws.rs.core.Response.ResponseBuilder builder = null;
        RequestResponseCol sessionState = SessionManager.getSession(reqres.getSessionId());
        System.out.println("session state"+sessionState);
        if (sessionState != null) {
            reqres.setOption(sessionState.getOption());
            reqres.setBankNumber(sessionState.getBankNumber());
            if (sessionState.getIteration()==6){
                System.out.println("current iteration: "+reqres.getCurrentItteration()+ " incremenbted: "+(reqres.getCurrentItteration()+1));
                reqres.setCurrentItteration(reqres.getCurrentItteration()+1);
            }
        }
        int originalIteration = this.calculateOriginalIteration(reqres);
        System.out.print(customerAccounts);
        try {
            if (customerAccounts.isEmpty()) {
                String message = this.getLanguageString(reqres.getLanguage(), "no_account");
                ethio.com.models.Response res = new ethio.com.models.Response();
                res.setAction("end");
                res.setTransactionId(reqres.getSessionId());
                builder = buildResponse(reqres, message, res);
                SessionManager.removeSession(reqres.getSessionId());

            }
            System.out.println("*********" + originalIteration);
            if (originalIteration == 2) {
                builder = returnAccountList(reqres, customerAccounts, builder);
                return builder.build();
            } else if (originalIteration == 3) {
              String bankNumber = reqres.getCurrentRequest();
                System.out.println("account number before"+bankNumber);
               reqres.setBankNumber(bankNumber);
//               reqres.setAccountNumber(bankNumber);
                SessionManager.storeSession(reqres.getSessionId(), reqres);
              System.out.println("account number"+bankNumber);
                int selectedIndex;
                LOGGER.info("selectedBankNumber: " + bankNumber);
                try {
                    selectedIndex = Integer.parseInt(bankNumber) - 1;
                } catch (NumberFormatException e) {
                    String message = this.getLanguageString(reqres.getLanguage(), "inv");
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setAction("end");
                    res.setTransactionId(reqres.getSessionId());
                    builder = buildResponse(reqres, message, res);
                    SessionManager.removeSession(reqres.getSessionId());
                    return builder.build();
                }
                if (selectedIndex < 0 || selectedIndex >= customerAccounts.size()) {
                    String message = this.getLanguageString(reqres.getLanguage(), "inv");
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setAction("end");
                    res.setTransactionId(reqres.getSessionId());
                    builder = buildResponse(reqres, message, res);
                    SessionManager.removeSession(reqres.getSessionId());
                }else {
                    String message = this.getLanguageString(reqres.getLanguage(), "topUp");
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setTransactionId(reqres.getSessionId());
                    res.setAction("request");
                    builder = buildResponse(reqres, message, res);
                    return builder.build();
                }
            }
            else if (originalIteration == 4) {
                String option = reqres.getCurrentRequest();
                reqres.setOption(option);
                SessionManager.storeSession(reqres.getSessionId(), reqres);
                if ("1".equals(option)) {
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setTransactionId(reqres.getSessionId());
                    res.setAction("request");
                    String message = this.getLanguageString(reqres.getLanguage(), "topam");
                    builder = buildResponse(reqres, message, res);
                    reqres.setIteration(6);
                    SessionManager.storeSession(reqres.getSessionId(), reqres);
//                    SessionManager.removeSession(reqres.getSessionId());

                } else if ("2".equals(option)) {
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setTransactionId(reqres.getSessionId());
                    res.setAction("request");
                    String message = this.getLanguageString(reqres.getLanguage(), "mobil");
                    builder = buildResponse(reqres, message, res);
                    return builder.build();
                } else {
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setTransactionId(reqres.getSessionId());
                    res.setAction("end");
                    String message = this.getLanguageString(reqres.getLanguage(), "inv");
                    builder = buildResponse(reqres, message, res);
                    SessionManager.removeSession(reqres.getSessionId());
                    return builder.build();
                }
            }
            else if (originalIteration == 5) {
                String phoneNumber = reqres.getCurrentRequest();
                System.out.println("5 iteration +++++++++++++++"+phoneNumber);
                    if (!phoneNumber.matches("^(2519[0-9]{8}|09[0-9]{8}|9[0-9]{8})$")) {
                        String message = this.getLanguageString(reqres.getLanguage(), "inv");
                        ethio.com.models.Response res = new ethio.com.models.Response();
                        res.setAction("end");
                        res.setTransactionId(reqres.getSessionId());
                        builder = buildResponse(reqres, message, res);
                        SessionManager.removeSession(reqres.getSessionId());
                    } else {
                        reqres.setPhoneNumber(phoneNumber);
                        SessionManager.storeSession(reqres.getSessionId(), reqres);
                        ethio.com.models.Response res = new ethio.com.models.Response();
                        res.setTransactionId(reqres.getSessionId());
                        res.setAction("request");
                        String message = this.getLanguageString(reqres.getLanguage(), "topam");
                        builder = buildResponse(reqres,message, res);
                    }
                }
            else if (originalIteration == 6) {
                 assert sessionState != null;
                String option=sessionState.getOption();
                System.out.println("OPTION "+option);
                if (option == null) {
                    String message = this.getLanguageString(reqres.getLanguage(), "inv");
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setAction("end");
                    res.setTransactionId(reqres.getSessionId());
                    builder = buildResponse(reqres, message, res);
                    SessionManager.removeSession(reqres.getSessionId());
                    return builder.build();
                }
                else if ("1".equals(option)) {
                    String amountStr = reqres.getCurrentRequest();
                    int amount;
                try {
                    amount = Integer.parseInt(amountStr);
                    if (amount <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    String message = this.getLanguageString(reqres.getLanguage(), "invAm");
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setAction("end");
                    res.setTransactionId(reqres.getSessionId());
                    builder = buildResponse(reqres, message, res);
                    SessionManager.removeSession(reqres.getSessionId());
                    return builder.build();
                }
                    String phoneNumber = reqres.getMsisdn();
                        String owenbankNumber = reqres.getRequestList().get(reqres.getRequestList().size() - 2).getUssdrequestString();
                    int selectedIndex;
                    LOGGER.info("selectedBankNumber: OwenbankNumber " + owenbankNumber);

                    try {
                        selectedIndex = Integer.parseInt(owenbankNumber) - 1; // Adjust for 0-indexed list
                    } catch (NumberFormatException e) {
                        String message = this.getLanguageString(reqres.getLanguage(), "inv");
                        ethio.com.models.Response res = new ethio.com.models.Response();
                        res.setAction("end");
                        res.setTransactionId(reqres.getSessionId());
                        SessionManager.removeSession(reqres.getSessionId());
                        return buildResponse(reqres, message, res).build();
                    }
                    if (selectedIndex < 0 || selectedIndex >= customerAccounts.size()) {
                        String message = this.getLanguageString(reqres.getLanguage(), "inv");
                        ethio.com.models.Response res = new ethio.com.models.Response();
                        res.setAction("end");
                        res.setTransactionId(reqres.getSessionId());
                        SessionManager.removeSession(reqres.getSessionId());
                        return buildResponse(reqres, message, res).build();
                    } else {
                        CustomerInfoView selectedAccount = customerAccounts.get(selectedIndex);
                        String selectedBankNumber = selectedAccount.getAccount();
                        LOGGER.info("selectedBankNumber: " + selectedBankNumber);
                        reqres.setBankNumber(selectedBankNumber);
                        ethio.com.models.Response res = new ethio.com.models.Response();
                        String message = this.getLanguageString(reqres.getLanguage(), "upCon");
                        message = message.replace("@account", selectedBankNumber)
                                .replace("@amount", amountStr)
                                .replace("@phone", phoneNumber);
                        res.setTransactionId(reqres.getSessionId());
                        res.setAction("request");
                        builder=buildResponse(reqres,message , res);
                    }
                }
                else if ("2".equals(option)) {
                    String amountStr = reqres.getCurrentRequest();
                    int amount;
                    try {
                        amount = Integer.parseInt(amountStr);
                        if (amount <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        String message = this.getLanguageString(reqres.getLanguage(), "invAm");
                        ethio.com.models.Response res = new ethio.com.models.Response();
                        res.setAction("end");
                        res.setTransactionId(reqres.getSessionId());
                        builder = buildResponse(reqres, message, res);
                        SessionManager.removeSession(reqres.getSessionId());
                        return builder.build();
                    }
                    String phoneNumber = reqres.getRequestList().get(reqres.getRequestList().size() - 1).getUssdrequestString();
//                    String OtherbankNumber = reqres.getRequestList().get(reqres.getRequestList().size() - 2).getUssdrequestString();
                    String OtherbankNumber3 = reqres.getRequestList().get(reqres.getRequestList().size() - 3).getUssdrequestString();

                    System.out.println("Otherbank number using s-3"+OtherbankNumber3);
//                    System.out.println("Otherbank number using s-2"+OtherbankNumber);
                    Response Responser = handleBankSelection(reqres, customerAccounts, amountStr, OtherbankNumber3,phoneNumber);
                    return Responser;
                }
                else {
                    String message = this.getLanguageString(reqres.getLanguage(), "inv");
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setTransactionId(reqres.getSessionId());
                    res.setAction("end");
                    builder = buildResponse(reqres, message,res);
                    SessionManager.removeSession(reqres.getSessionId());
                }
            }

            else if (originalIteration == 7) {
                if ("1".equals(reqres.getCurrentRequest())) {
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    String message = this.getLanguageString(reqres.getLanguage(), "pass");
                    res.setTransactionId(reqres.getSessionId());
                    res.setAction("request");
                    builder = buildResponse(reqres, message, res);
                } else {
                     String message = this.getLanguageString(reqres.getLanguage(), "tcnl");
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setTransactionId(reqres.getSessionId());
                    res.setAction("end");
                    builder = buildResponse(reqres, message, res);
                    SessionManager.removeSession(reqres.getSessionId());
                }
            }
            else if (originalIteration == 8) {
               String pin = reqres.getCurrentRequest();
               String phoneNumberPin = reqres.getMsisdn();
                    boolean auth=  PinAuthentication(pin,phoneNumberPin);
                    System.out.println("auth====="+auth);
                String option=sessionState.getOption();
                int size = reqres.getRequestList().size();
                LOGGER.info("Request list size: " + size);
                for (int i = 0; i < reqres.getRequestList().size(); i++) {
                    LOGGER.info("Request at index " + i + ": " + reqres.getRequestList().get(i).getUssdrequestString());
                }

                String bankNumber;
            if (auth) {
                if ("1".equals(option))
                    bankNumber = reqres.getRequestList().get(reqres.getRequestList().size() - 4).getUssdrequestString();
            else {  bankNumber = reqres.getRequestList().get(reqres.getRequestList().size() - 5).getUssdrequestString();
                }
                LOGGER.info("selectedBankNumber: bankNumber index" + bankNumber);
                int selectedIndex;
//                LOGGER.info("selectedBankNumber: bankNumber4" + bankNumber);
                try {
                    selectedIndex = Integer.parseInt(bankNumber) - 1;
                } catch (NumberFormatException e) {
                    String message = this.getLanguageString(reqres.getLanguage(), "inv");
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setAction("end");
                    res.setTransactionId(reqres.getSessionId());
                    builder = buildResponse(reqres, message, res);
                    SessionManager.removeSession(reqres.getSessionId());
                    return builder.build();
                }
                if (selectedIndex < 0 || selectedIndex >= customerAccounts.size()) {
                    String message = this.getLanguageString(reqres.getLanguage(), "inv");
                    ethio.com.models.Response res = new ethio.com.models.Response();
                    res.setAction("end");
                    res.setTransactionId(reqres.getSessionId());
                    builder = buildResponse(reqres, message, res);
                    SessionManager.removeSession(reqres.getSessionId());
                } else {
                    CustomerInfoView selectedAccount = customerAccounts.get(selectedIndex);
                    String selectedBankNumber =selectedAccount.getAccount();
                    LOGGER.info("selectedBankNumber: after " + selectedBankNumber);
                    reqres.setBankNumber(selectedBankNumber);
                    System.out.println("Option on it 8"+option);
                    String phoneNumber;
                    if ("1".equals(option)) {
                        String owenPhoneNumber = reqres.getMsisdn();
               if (owenPhoneNumber.startsWith("251")) {
                    owenPhoneNumber = "0" + owenPhoneNumber.substring(3);
               }
               else if (owenPhoneNumber.startsWith("+251")) {
                   owenPhoneNumber = "0" + owenPhoneNumber.substring(4); // Remove '+251' and prepend '0'
                    }
                   phoneNumber = owenPhoneNumber;
                    } else {
                       String otherphoneNumber = reqres.getRequestList().get(reqres.getRequestList().size() - 3).getUssdrequestString();
                        if (otherphoneNumber.startsWith("251")) {
                            otherphoneNumber = "0" + otherphoneNumber.substring(3); // Remove '251' and prepend '0'
                        }
                        else if (otherphoneNumber.startsWith("+251")) {
                            otherphoneNumber = "0" + otherphoneNumber.substring(4); // Remove '+251' and prepend '0'
                        }
                        else if (otherphoneNumber.startsWith("9")) {
                            otherphoneNumber = "0" + otherphoneNumber; // Prepend '0' without removing the '9'
                        }
                        phoneNumber = otherphoneNumber;
                      }
                    int amount = Integer.parseInt(reqres.getRequestList().get(reqres.getRequestList().size() - 2).getUssdrequestString());
                   String limitResponse=  limitCheck(amount,phoneNumber);
                  JSONObject limitjsonResponse = new JSONObject(limitResponse);
                    Boolean status=limitjsonResponse.optBoolean("status");
                  String  description=limitjsonResponse.optString("description");
                    System.out.println("limitResponse +++++++++++"+limitResponse);
                    System.out.println("description +++++++++++"+description);
                    System.out.println("status +++++++++++"+status);
                    if (!status) {
                        ethio.com.models.Response res = new ethio.com.models.Response();
                        res.setTransactionId(reqres.getSessionId());
                        res.setAction("end");
                        builder = buildResponse(reqres, description, res);
                        SessionManager.removeSession(reqres.getSessionId());
                        return builder.build();
                    }
                    String Topupresponse = processTopUp(phoneNumber, selectedBankNumber, amount);
//                    Topupresponse = Topupresponse.replace("[\"", "").replace("\"]", "");
                    System.out.println("Topup process response before replace" + Topupresponse);
                    if (Topupresponse != null) {
                        Topupresponse = Topupresponse.replace("[\"", "").replace("\"]", "");
                        System.out.println("Topup process response after replace" + Topupresponse);
                        try {
                            JSONObject jsonResponse = new JSONObject(Topupresponse);
                            String type = jsonResponse.optString("type");
                            System.out.println("type: " + type);
                            String txnStatus = jsonResponse.optString("txnStatus");
                            System.out.println("txnStatus: " + txnStatus);
                            String date = jsonResponse.optString("date");
                            System.out.println("date: " + date);
                            String extRefNum = jsonResponse.optString("extRefNum");
                            System.out.println("extRefNum: " + extRefNum);
                            String txnID = jsonResponse.optString("txnID");
                            System.out.println("txnID: " + txnID);
                            String messageResponse = jsonResponse.optString("message");
                            System.out.println("message: " + messageResponse);
                            String cbsTxnStatus = jsonResponse.optString("cbsTxnStatus");
                            System.out.println("cbsTxnStatus: " + cbsTxnStatus);
                            String txnamt = jsonResponse.optString("txnamt");
                            System.out.println("txnamt: " + txnamt);
                            if ("000".equals(txnStatus.trim())) {
                                System.out.println("Transaction Status is SUCCESS");
                                ethio.com.models.Response res = new ethio.com.models.Response();
                                res.setTransactionId(reqres.getSessionId());
                                res.setAction("end");
                                String message = this.getLanguageString(reqres.getLanguage(), "toSuc");
                                System.out.println("Success message: " + message);
                                message = message.replace("@ref", extRefNum);
                                builder = buildResponse(reqres, message , res);
                                System.out.println("Response after building success: " + builder);
                                SessionManager.removeSession(reqres.getSessionId());

                            } else if ("0".equals(txnStatus.trim())) {
                                System.out.println("Transaction Status is on excepetional case");
                                ethio.com.models.Response res = new ethio.com.models.Response();
                                res.setTransactionId(reqres.getSessionId());
                                res.setAction("end");
                                builder = buildResponse(reqres, messageResponse, res);
                                SessionManager.removeSession(reqres.getSessionId());

                            } else {
                                String message = this.getLanguageString(reqres.getLanguage(), "toFai");
                                System.out.println("Transaction Status is FAILURE");
                                ethio.com.models.Response res = new ethio.com.models.Response();
                                res.setTransactionId(reqres.getSessionId());
                                res.setAction("end");
                                builder = buildResponse(reqres, message, res);
                                System.out.println("Response after building failure: " + builder);
                                SessionManager.removeSession(reqres.getSessionId());

                            }
                        } catch (Exception e) {
                            System.out.println("Error occurred: " + e.getMessage());
                            e.printStackTrace();
                            String message = "An error occurred: " + e.getMessage();
                            ethio.com.models.Response res = new ethio.com.models.Response();
                            res.setTransactionId(reqres.getSessionId());
                            res.setAction("end");
                            builder = buildResponse(reqres, message, res);
                            System.out.println("Response after error: " + builder);
                            SessionManager.removeSession(reqres.getSessionId());

                        }
                    } else {
                        String message = this.getLanguageString(reqres.getLanguage(), "con");
                        ethio.com.models.Response res = new ethio.com.models.Response();
                        res.setTransactionId(reqres.getSessionId());
                        res.setAction("end");
                        builder = buildResponse(reqres, message, res);
                        SessionManager.removeSession(reqres.getSessionId());
                    }
                }
            }
            else{
                String message = this.getLanguageString(reqres.getLanguage(), "invlp");
                ethio.com.models.Response res = new ethio.com.models.Response();
                res.setTransactionId(reqres.getSessionId());
                res.setAction("end");
                builder = buildResponse(reqres,message , res);
                SessionManager.removeSession(reqres.getSessionId());
            }}
            return builder.build();
        } catch (NumberFormatException e) {
                        String message = this.getLanguageString(reqres.getLanguage(), "con");
                        ethio.com.models.Response res = new ethio.com.models.Response();
                        res.setTransactionId(reqres.getSessionId());
                        res.setAction("end");
                        builder = buildResponse(reqres, message, res);
            SessionManager.removeSession(reqres.getSessionId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder.build();
    }
    private String limitCheck(int amount, String phoneNumber) {
        if (phoneNumber.startsWith("09")) {
            phoneNumber = "251" + phoneNumber.substring(1);
        } else if (phoneNumber.startsWith("9")) {
            phoneNumber = "251" + phoneNumber;
        }
        String url = "http://10.57.40.121:8080/Limit/rest/getLimit";
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("phone", phoneNumber);
        jsonPayload.put("txnType", "FTBANK");
        jsonPayload.put("amount", amount);
        String limitPayload=jsonPayload.toString();
        System.out.println("limit  payload: " + jsonPayload);
        Client client = ClientBuilder.newBuilder().build();
        Response pinResponse = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(limitPayload, MediaType.APPLICATION_JSON));
        String responseString = pinResponse.readEntity(String.class);
        System.out.println("limit response: " + responseString);
        client.close();
        return responseString;
    }
    private Response handleBankSelection(RequestResponseCol reqres, List<CustomerInfoView> customerAccounts, String amountStr,  String owenbankNumber,String phoneNumber) throws Exception {
        int selectedIndex;
        LOGGER.info("selectedBankNumber: OtherbankNumber " + owenbankNumber);
        try {
            selectedIndex = Integer.parseInt(owenbankNumber) - 1; // Adjust for 0-indexed list
        } catch (NumberFormatException e) {
            String message = this.getLanguageString(reqres.getLanguage(), "inv");
            ethio.com.models.Response res = new ethio.com.models.Response();
            res.setAction("end");
            res.setTransactionId(reqres.getSessionId());
            return buildResponse(reqres, message, res).build();
        }
        if (selectedIndex < 0 || selectedIndex >= customerAccounts.size()) {
            String message = this.getLanguageString(reqres.getLanguage(), "inv");
            ethio.com.models.Response res = new ethio.com.models.Response();
            res.setAction("end");
            res.setTransactionId(reqres.getSessionId());
            return buildResponse(reqres, message, res).build();
        } else {
            CustomerInfoView selectedAccount = customerAccounts.get(selectedIndex);
            String selectedBankNumber = selectedAccount.getAccount();
            LOGGER.info("selectedBankNumber: " + selectedBankNumber);
            reqres.setBankNumber(selectedBankNumber);
            reqres.setTransactionAmount(amountStr);
            SessionManager.storeSession(reqres.getSessionId(), reqres);
            ethio.com.models.Response res = new ethio.com.models.Response();
            res.setTransactionId(reqres.getSessionId());
            res.setAction("request");
            String message = this.getLanguageString(reqres.getLanguage(), "upCon");
            message = message.replace("@account", selectedBankNumber)
                    .replace("@amount", amountStr)
                    .replace("@phone", phoneNumber);
            return buildResponse(reqres, message, res).build();
        }
    }
    public String processTopUp(String phoneNumber,String accountNumber, int amount) throws Exception {


        System.out.println("processTopUp inside processTopUp" );
        String response_token=fetchToken();
        JSONObject token = new JSONObject(response_token);
        String access_token = token.getString("access_token");
        System.out.println("New acccess token " + access_token);
        JSONObject topupJson = new JSONObject();
//        topupJson.put("debitAccount", "0083920830101");
        topupJson.put("debitAccount", accountNumber);
        topupJson.put("amount", amount);
//        topupJson.put("phoneNumber", phoneNumber);
        topupJson.put("phoneNumber", phoneNumber);
        topupJson.put("source", "USSD");
        String TopUpPayload = topupJson.toString();
        System.out.println("topup Payload " + TopUpPayload);
        String url = "https://internalgateway-uat.wegagenbanksc.com.et/airtimetopup_api/1.0.0/airtimetopup_rest";
//        String url = "https://internalgateway-uat.wegagenbanksc.com.et/airtimetopup_api/1.0.0/airtimetopup_rest";
        Client client = ClientBuilder.newClient();
        try {
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + access_token)
                    .post(Entity.entity(TopUpPayload, MediaType.APPLICATION_JSON));
            int status = response.getStatus();
            if (status == Response.Status.OK.getStatusCode()) {
                return response.readEntity(String.class);
            } else {
                String errorMessage = response.readEntity(String.class);
                JSONObject errorResponse = new JSONObject();
                errorResponse.put("status", "error");
                errorResponse.put("message", errorMessage);
                int statusCode = response.getStatus();
                errorResponse.put("statusCode", statusCode);
                return errorResponse.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return errorResponse.toString();
        } finally {
            client.close();
        }
    }
    public int calculateOriginalIteration(RequestResponseCol reqres) {
        return reqres.getCurrentItteration() - nextCount(reqres);
    }
    public int nextCount(RequestResponseCol reqres) {
        boolean isTrackingNext = false;
        int nextCountValue = 0;
        if (reqres.getRequestList() != null && !reqres.getRequestList().isEmpty()) {
            for (int i = 0; i < reqres.getRequestList().size(); i++) {
                Request request = reqres.getRequestList().get(i);
                if (i == accountListStartIteration || request
                        .getUssdrequestString().equalsIgnoreCase(nextCharacter))
                    isTrackingNext = true;
                if (request.getUssdrequestString().equalsIgnoreCase(nextCharacter)) {
                    if (isTrackingNext)
                        nextCountValue++;
                    continue;
                }
                isTrackingNext = false;
            }
            if (reqres.getCurrentRequest().equalsIgnoreCase(nextCharacter))
                nextCountValue++;
        }
        return nextCountValue;
    }
    public int calculateQueryStartValue(RequestResponseCol reqres) {
        return this.nextCount(reqres) * maxBankListValue;
    }
    public List<String> returnTenatList(RequestResponseCol reqres) throws Exception {
        List<String> allTenantIds = this.customerInfoViewDao.getAllTenantIds();
        List<String> customerTenantIds = new ArrayList<>();
        if (allTenantIds != null && !allTenantIds.isEmpty()) {
            String message = this.getLanguageString(reqres.getLanguage(), "sltid");
            message = message.replace("@tenant", "");
            customerTenantIds.add(message);
            for (int i = 0; i < allTenantIds.size(); i++) {
                String temp = i + ". " + allTenantIds.get(i);
                customerTenantIds.add(temp);
            }
        } else return null;
        return customerTenantIds;
    }
    public String paginateBankList(String lang, String stringid, RequestResponseCol reqres) throws Exception {
        StringBuilder bankListSection = new StringBuilder();
        int calculatedStartValue = calculateQueryStartValue(reqres);
        List<String> banksList = returnTenatList(reqres);
        List<String> refinedBanks = new ArrayList<>();
        String nextMessage = this.getLanguageString(reqres.getLanguage(), "nmsg");
        int startIndex = calculatedStartValue;
        int endIndex = 0;
        if (startIndex > banksList.size()) {
            endIndex = banksList.size() - 1;
            startIndex = banksList.size() - 1 - maxBankListValue;
        } else if (startIndex + maxBankListValue > banksList.size()) {
            endIndex = banksList.size() - 1;
        } else {
            endIndex = startIndex + maxBankListValue;
        }
        if (calculatedStartValue == 0) {
            if (endIndex > banksList.size()) {
                refinedBanks.addAll(banksList.subList(startIndex, endIndex));
            } else {
                refinedBanks.addAll(banksList.subList(startIndex, endIndex + 1));
            }
        } else {
            refinedBanks.add(banksList.get(0));
            if (endIndex >= banksList.size() - 1) {
                endIndex = banksList.size();
                refinedBanks.addAll(banksList.subList(startIndex + 1, endIndex));
            } else {
                refinedBanks.addAll(banksList.subList(startIndex + 1, endIndex + 1));
            }
        }
        if (endIndex + 2 < banksList.size())
            refinedBanks.add(nextMessage);
        for (String bank : refinedBanks)
            bankListSection.append(bank).append("\n");
        return bankListSection.toString();
    }

    private Response.ResponseBuilder getTenantList(RequestResponseCol reqres) throws Exception {
        List<CustomerInfoView> customerAccounts = this.customerInfoViewDao.getAccounts(reqres.getMsisdn());
        int size = customerAccounts.size();
        boolean isInvalid = this.validationAccountSize(reqres, size);
        System.out.println("isInvalid: " + isInvalid);
        List<String> bankWithHeader = returnTenatList(reqres);
        List<String> banks = bankWithHeader.subList(1, bankWithHeader.size() - 1);
        ethio.com.models.Response res = new ethio.com.models.Response();
        String message = paginateBankList(reqres.getLanguage(), "bank", reqres);
        if (!reqres.getCurrentRequest().equalsIgnoreCase(nextCharacter)) {
            if (!isInvalid) {
                res.setAction("request");
            } else {
                message = this.getLanguageString(reqres.getLanguage(), "inv");
                res.setAction("end");
            }
        } else {
            if (maxBankListValue * this.nextCount(reqres) > banks.size()) {
                res.setAction("end");
                return this.buildResponse(reqres, this.getLanguageString(reqres.getLanguage(), "inv"), res);
            }
            res.setAction("request");
        }
        return this.buildResponse(reqres, message, res);
    }
    public boolean validationAccountSize(RequestResponseCol reqres, int size) {
        boolean isInvalid = true;
        for (int i = 1; i <= size; i++) {
            if (reqres.getCurrentRequest().equals(i + ""))
                isInvalid = false;
        }
        return isInvalid;
    }
    public Boolean PinAuthentication(String pin, String phone) {
        String url = "http://10.57.40.116:8080/keycloak/rest/keycloak/authenticate";
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("phone", phone);
        jsonPayload.put("password", pin);
        System.out.println("Pin Authentication payload: " + jsonPayload.toString());
        Client client = ClientBuilder.newBuilder().build();
        Response pinResponse = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(jsonPayload.toString(), MediaType.APPLICATION_JSON));
        String responseString = pinResponse.readEntity(String.class);
        System.out.println("Pin response: " + responseString);
        client.close();
        JSONObject jsonResponse = new JSONObject(responseString);
        return jsonResponse.optBoolean("status", true);
    }
    private String fetchToken() throws Exception {
//        String url = "http://10.57.40.158:8280/wegagenmpsea/1.0.0/wegagenToken";
//        String username = "mHuruOPOka2kb23tXk_OYtL1KNUa";
//        String password = "A0z8vl9OW6vwsgVdiE80iAnJFeYa";
        String url="https://wso2apim.wegagentraining.com:9443/oauth2/token";
        String username="cC1Zp4mbTl5pf0f34uCoJrd8n2Ya";
        String password ="UONX4gK8RWu8ohjyPj9z4lyH8Uoa";
        String grantType = "grant_type=client_credentials";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " +
                java.util.Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (OutputStream os = connection.getOutputStream()) {
            os.write(grantType.getBytes());
            os.flush();
        }
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (java.io.InputStream is = connection.getInputStream()) {
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } else {
            throw new Exception("Failed to fetch token. HTTP Code: " + responseCode);
        }
    }

    private javax.ws.rs.core.Response.ResponseBuilder returnAccountList(
            RequestResponseCol reqres, List<CustomerInfoView> customerAccounts,
            javax.ws.rs.core.Response.ResponseBuilder builder) throws Exception {
        String message = this.getLanguageString(reqres.getLanguage(), "acc");
        if (customerAccounts != null && !customerAccounts.isEmpty()) {
            StringBuilder accounts = new StringBuilder();
            int index = 1;
            for (CustomerInfoView account : customerAccounts) {
                accounts.append("\n").append(index).append(". ").append(account.getAccount());
                index++;
            }
            if (message.contains("@accounts")) {
                message = message.replace("@accounts", accounts.toString());
            } else {
                message += "\n" + accounts.toString();
            }
            ethio.com.models.Response res = new ethio.com.models.Response();
            res.setAction("request");
            res.setTransactionId(reqres.getSessionId());

            builder = buildResponse(reqres, message, res);
        } else {
            String message1 = this.getLanguageString(reqres.getLanguage(), "no_account");
            ethio.com.models.Response res = new ethio.com.models.Response();
            res.setAction("end");
            res.setTransactionId(reqres.getSessionId());

            builder = buildResponse(reqres, message1, res);
        }

        return builder;
    }

    private javax.ws.rs.core.Response.ResponseBuilder buildResponse(RequestResponseCol reqres, String message, ethio.com.models.Response res) {
        javax.ws.rs.core.Response.ResponseBuilder builder;
        res.setUssdresponseString(message);
        res.setTransactionId(reqres.getSessionId());
        Date date = new Date();
        res.setTransactionTime(((Long) date.getTime()).toString());
        builder = javax.ws.rs.core.Response.ok().entity(res);
        return builder;
    }
    public String getLanguageString(String lang, String stringid) throws Exception {
        String userDisplay = "";
        Query q = this.em.createNamedQuery("GetMenu");
        q.setParameter("language", lang);
        q.setParameter("stringid", stringid);
        if (q.getResultList().size() > 0) {
            userDisplay = ((L10n) q.getSingleResult()).getContent();
        } else {
            Query q1 = this.em.createNamedQuery("GetMenu");
            q1.setParameter("language", "ENG");
            q1.setParameter("stringid", stringid);
            if (q1.getResultList().size() <= 0) {
                Exception E = new Exception("ACT-ERROR-DATA Menu does not exist"+stringid);
                throw E;
            }
            userDisplay = ((L10n) q1.getSingleResult()).getContent();
        }
        return userDisplay;
    }
    public List<String> optionfinder(String options) {
        List<String> menu = new ArrayList<String>();
        menu.add("Heading");
        try {
            String[] optionList = options.split("\n");
            if (optionList.length > 0) {
                int counter = 0;
                for (String option : optionList) {
                    if (counter > 0) {
                        String[] row = option.split("\\.");
                        int index = Integer.parseInt(row[0]);
                        menu.add(index, row[1]);
                    }
                    counter++;
                }
                return menu;
            } else {
                return menu;
            }
        } catch (Exception E) {
            E.printStackTrace();
            return menu;
        }
    }
}