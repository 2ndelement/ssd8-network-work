package space._2ndelement.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * @author 2ndElement
 * @version v1.0
 * @description
 * @date 2022/11/4 18:50
 */
public class RmiServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java RmiServer [port]");
        }
        try {
            int port = Integer.parseInt(args[0]);
            // 在localhost:1099启动rmi服务器
            LocateRegistry.createRegistry(port);
            MeetingService meetingService = new MeetingServiceImpl();
            String addr = "rmi://localhost:" + port + "/meetingService";
            Naming.rebind(addr, meetingService);
            System.out.println("RMI Server is ready in " + addr);
        } catch (RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
