package nl.triandria.libodoojsonrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Model {

    private Server server;
    // TODO common CRUD operations will be taken care of here with common methods, different versions should be considered

    Model(Server server) {
        this.server = server;
    }

    public static Map<String, ?> search(ArrayList domain, int offset, int limit, String order, boolean count) {



        Map<String, ?> result = new HashMap<>();
        return result;
    }

    public static int create(Map<String, ?> vars) {
        return 0;
    }

    public static void unlink(ArrayList ids) {

    }

    public static void update(int id, Map<String, ?> vars) {

    }

}
