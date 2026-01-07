package dto;

public class Permission {
    private int permId;
    private String permCode;
    private String permName;

    public Permission() {}

    public Permission(int permId, String permCode, String permName) {
        this.permId = permId;
        this.permCode = permCode;
        this.permName = permName;
    }

    public int getPermId() { return permId; }
    public void setPermId(int permId) { this.permId = permId; }

    public String getPermCode() { return permCode; }
    public void setPermCode(String permCode) { this.permCode = permCode; }

    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }
}
