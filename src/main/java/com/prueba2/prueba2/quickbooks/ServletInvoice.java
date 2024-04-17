package com.prueba2.prueba2.quickbooks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.data.*;
import com.intuit.ipp.services.QueryResult;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/invoice")
public class ServletInvoice extends HttpServlet {

    @Inject
    Helper helper;
    private static final String ACCOUNT_QUERY = "select * from Account where AccountType='%s' maxresults 1";

    protected void doGet(HttpServletRequest request, HttpServletResponse response){
        //obtenemos de la sesion el REALMID
        HttpSession session = request.getSession();
        String realmId = (String) session.getAttribute("realmId");
        String accessToken = (String) session.getAttribute("access_token");
        //String realmId = (String) request.getSession().getAttribute("realmId");
        //String accessToken = (String)request.getSession().getAttribute("access_token");
        if (StringUtils.isEmpty(realmId))
        {
            System.out.println("El realmId está vacío.");
        }
        if (StringUtils.isEmpty(accessToken))
        {
            System.out.println("El acces está vacío.");
        }
        try {
            //get DataService
            DataService service = helper.getDataService(realmId, accessToken);
            //add customer
            Customer customer = getCustomerWithAllFields();
            Customer savedCustomer = service.add(customer);

            //add item
            Item item = getItemFields(service);
            Item savedItem = service.add(item);

            //create invoice using customer and item created above
            Invoice invoice = getInvoiceFields(savedCustomer, savedItem);
            Invoice savedInvoice = service.add(invoice);

            //send invoice email to customer
            service.sendEmail(savedInvoice, customer.getPrimaryEmailAddr().getAddress());

            //receive payment for the invoice
            Payment payment = getPaymentFields(savedCustomer, savedInvoice);
            Payment savedPayment = service.add(payment);

            //return response back
            String jsonResponse = createResponse(savedPayment);
            System.out.println(jsonResponse);

            //request.getSession().setAttribute("jsonResponse", jsonResponse);
            response.sendRedirect(request.getContextPath() + "/connectOK.xhtml");

        } catch (FMSException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Customer getCustomerWithAllFields() {
        Customer customer = new Customer();
        customer.setDisplayName(RandomStringUtils.randomAlphanumeric(6));
        customer.setCompanyName("ABC Corporations");

        EmailAddress emailAddr = new EmailAddress();
        emailAddr.setAddress("testconceptsample@mailinator.com");
        customer.setPrimaryEmailAddr(emailAddr);

        return customer;
    }

    private Item getItemFields(DataService service) throws FMSException {

        Item item = new Item();
        item.setName("Item" + RandomStringUtils.randomAlphanumeric(5));
        item.setTaxable(false);
        item.setUnitPrice(new BigDecimal("200"));
        item.setType(ItemTypeEnum.SERVICE);

        Account incomeAccount = getIncomeBankAccount(service);
        item.setIncomeAccountRef(createRef(incomeAccount));

        return item;
    }

    private Account getIncomeBankAccount(DataService service) throws FMSException {
        QueryResult queryResult = service.executeQuery(String.format(ACCOUNT_QUERY, AccountTypeEnum.INCOME.value()));
        List<? extends IEntity> entities = queryResult.getEntities();
        if(!entities.isEmpty()) {
            return (Account)entities.get(0);
        }
        return createIncomeBankAccount(service);
    }

    private Account createIncomeBankAccount(DataService service) throws FMSException {
        Account account = new Account();
        account.setName("Incom" + RandomStringUtils.randomAlphabetic(5));
        account.setAccountType(AccountTypeEnum.INCOME);

        return service.add(account);
    }

    private ReferenceType createRef(IntuitEntity entity) {
        ReferenceType referenceType = new ReferenceType();
        referenceType.setValue(entity.getId());
        return referenceType;
    }

    private Invoice getInvoiceFields(Customer customer, Item item) {

        Invoice invoice = new Invoice();
        invoice.setCustomerRef(createRef(customer));

        List<Line> invLine = new ArrayList<Line>();
        Line line = new Line();
        line.setAmount(new BigDecimal("100"));
        line.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);

        SalesItemLineDetail silDetails = new SalesItemLineDetail();
        silDetails.setItemRef(createRef(item));

        line.setSalesItemLineDetail(silDetails);
        invLine.add(line);
        invoice.setLine(invLine);

        return invoice;
    }

    private Payment getPaymentFields(Customer customer, Invoice invoice) {

        Payment payment = new Payment();
        payment.setCustomerRef(createRef(customer));

        payment.setTotalAmt(invoice.getTotalAmt());

        List<LinkedTxn> linkedTxnList = new ArrayList<LinkedTxn>();
        LinkedTxn linkedTxn = new LinkedTxn();
        linkedTxn.setTxnId(invoice.getId());
        linkedTxn.setTxnType(TxnTypeEnum.INVOICE.value());
        linkedTxnList.add(linkedTxn);

        Line line1 = new Line();
        line1.setAmount(invoice.getTotalAmt());
        line1.setLinkedTxn(linkedTxnList);

        List<Line> lineList = new ArrayList<Line>();
        lineList.add(line1);
        payment.setLine(lineList);

        return payment;
    }

    private String createResponse(Object entity) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString;
        try {
            jsonInString = mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            return createErrorResponse(e);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
        return jsonInString;
    }


    private String createErrorResponse(Exception e) {
        return new JSONObject().put("response","Failed").toString();
    }



}
