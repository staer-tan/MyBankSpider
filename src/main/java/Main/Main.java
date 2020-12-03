package Main;

import BankService.*;

public class Main {
    public static void main(String[] args) throws Exception {

        // 1. 中国银行
        BankOfChinaServer bankOfChinaServer = new BankOfChinaServer();
        bankOfChinaServer.start();

        // 2. 交通银行
//        BankOfCommServer bankOfCommServer = new BankOfCommServer();
//        bankOfCommServer.start();

        // 3. 邮政储蓄银行
//        PostSavBankOfChinaServer postSavBankOfChinaServer = new PostSavBankOfChinaServer();
//        postSavBankOfChinaServer.start();

        // 4. 中国招商银行
//        ChinaMerchBankServer chinaMerchBankServer = new ChinaMerchBankServer();
//        chinaMerchBankServer.start();

        // 5. 中国农业银行
//        AgricultureBankOfChinaServer agricultureBankOfChinaServer = new AgricultureBankOfChinaServer();
//        agricultureBankOfChinaServer.start();

        // 6. 中国建设银行
//        ChinaConstructBankServer chinaConstructBankServer = new ChinaConstructBankServer();
//        chinaConstructBankServer.start();

        // 7. 中信银行
//        ChinaCiticBankServer chinaCiticBankServer = new ChinaCiticBankServer();
//        chinaCiticBankServer.start();

        // 8. 兴业银行
//        IndustrialBankServer industrialBankServer = new IndustrialBankServer();
//        industrialBankServer.start();

        // 9. 中国工商银行
        IndusCommBankOfChinaServer indusCommBankOfChinaServer = new IndusCommBankOfChinaServer();
        indusCommBankOfChinaServer.start();
    }
}
