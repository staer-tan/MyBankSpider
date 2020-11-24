package DataObject;

public class BankData {
    // 数据库id
    int id;
    // 银行种类
    private String bankType;
    // 银行名称
    private String bankName;
    // 银行层级
    private String bankLevel;
    // 省份
    private String province;
    // 城市
    private String city;
    // 区县
    private String area;
    // 地址
    private String address;
    // 电话
    private String telephone;
    // 上级机构
    private String parentBank;
    // 经度
    private String longitudeX;
    // 纬度
    private String latitudeY;

    public String getBankType() {
        return bankType;
    }

    public void setBankType(String bankType) {
        this.bankType = bankType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankLevel() {
        return bankLevel;
    }

    public void setBankLevel(String bankLevel) {
        this.bankLevel = bankLevel;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getParentBank() {
        return parentBank;
    }

    public void setParentBank(String parentBank) {
        this.parentBank = parentBank;
    }

    public String getLongitudeX() {
        return longitudeX;
    }

    public void setLongitudeX(String longitudeX) {
        this.longitudeX = longitudeX;
    }

    public String getLatitudeY() {
        return latitudeY;
    }

    public void setLatitudeY(String latitudeY) {
        this.latitudeY = latitudeY;
    }



}
