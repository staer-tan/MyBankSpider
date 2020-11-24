package DataService.mybatis;

import DataObject.BankData;
import DataService.DataService;

import DatabaseAssist.mybatis.mapper.BankDataMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 银行数据数据的服务类,使用mybatis功能
 */
public class BankDataService implements DataService<BankData> {

    private static SqlSessionFactory sqlSessionFactory;

    @Override
    public void init() throws IOException {
        String resource = "properties/mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        System.out.println("BankDataService init over");
    }

    @Override
    public int add(BankData bdObject) {
        SqlSession session = sqlSessionFactory.openSession(true);
        BankDataMapper bankDataMapper = session.getMapper(BankDataMapper.class);
        bankDataMapper.insert(bdObject);
        session.close();
        return bdObject.getId();
    }

    @Override
    public void adds(List<BankData> bdObj){
        SqlSession session = sqlSessionFactory.openSession(true);
        BankDataMapper bankDataMapper = session.getMapper(BankDataMapper.class);
        bankDataMapper.inserts(bdObj);
        session.close();
    }

}
