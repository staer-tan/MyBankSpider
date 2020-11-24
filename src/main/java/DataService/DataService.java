package DataService;

import java.io.IOException;
import java.util.List;

public interface DataService<E> {

    void init() throws IOException;

    int add(E obj);

    void adds(List<E> obj) throws ClassNotFoundException;

}
