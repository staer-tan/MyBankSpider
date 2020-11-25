package DatabaseAssist.mybatis;

import java.util.List;

public interface BaseMapper<E> {
    int insert(E obj);

    void inserts(List<E> objs);
}
