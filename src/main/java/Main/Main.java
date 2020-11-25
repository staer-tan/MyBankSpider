package Main;

import BankService.*;

public class Main {
    public static void main(String[] args) throws Exception {

        // 中国银行
        BankOfChinaServer bankOfChinaServer = new BankOfChinaServer();
        bankOfChinaServer.start();

        // 交通银行
//        BankOfCommServer bankOfCommServer = new BankOfCommServer();
//        bankOfCommServer.start();

        // 邮政储蓄银行
        PostSavBankOfChinaServer postSavBankOfChinaServer = new PostSavBankOfChinaServer();
        postSavBankOfChinaServer.start();

        // 中国招商银行
//        ChinaMerchBankServer chinaMerchBankServer = new ChinaMerchBankServer();
//        chinaMerchBankServer.start();

        // 中国农业银行
//        AgricultureBankOfChinaServer agricultureBankOfChinaServer = new AgricultureBankOfChinaServer();
//        agricultureBankOfChinaServer.start();


        // 中国建设银行
//        ChinaConstructBankServer chinaConstructBankServer = new ChinaConstructBankServer();
//        chinaConstructBankServer.start();
    }
}
