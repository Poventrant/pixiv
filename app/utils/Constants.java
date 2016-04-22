package utils;

/**
 * Created by 枫叶 on 2016/4/18.
 */
public interface Constants {

    public final static String DOWNLOAD_PATH = "public/images/pixiv/";
    public final static String MASTER_PATH = "public/images//pixiv_master/";
    public final static String BASE_URL = "http://www.pixiv.net/";
    public final static String PIXIV_LOGIN_INFO_PATH = "pixivinfo/PixivLoginInfo";
    /**
     * 连接池里的最大连接数
     */
    public static final int MAX_TOTAL_CONNECTIONS = 100;

    /**
     * 每个路由的默认最大连接数
     */
    public static final int MAX_ROUTE_CONNECTIONS = 80;

    /**
     * 连接超时时间
     */
    public static final int CONNECT_TIMEOUT = 60000;

    /**
     * 套接字超时时间
     */
    public static final int SOCKET_TIMEOUT = 80000;

    /**
     * 连接池中 连接请求执行被阻塞的超时时间
     */
    public static final long CONN_MANAGER_TIMEOUT = 100000;
}
