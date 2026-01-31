package dto;

public class BankConfig {
    private int id;
    private String bankId; 
    private String accountNumber; 
    private String accountName; 
    private String template; 

    public BankConfig() {
    }

    public BankConfig(String bankId, String accountNumber, String accountName, String template) {
        this.bankId = bankId;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.template = template;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getaccountNumber() {
        return accountNumber;
    }

    public void setaccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}