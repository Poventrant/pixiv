package utils;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 枫叶 on 2016/4/18.
 */
public class PixivCrawlerUtil {

    private final static Logger logger = LoggerFactory.getLogger(PixivCrawlerUtil.class);

    private static DefaultHttpClient httpclient = generateHttpClient();

    public static DefaultHttpClient getHttpClient() {
        return httpclient;
    }

    /**
     * 初始化httpclient
     */
    public static DefaultHttpClient generateHttpClient() {

        //http连接相关参数
        HttpParams parentParams;

        //http线程池管理器
        PoolingClientConnectionManager cm;
        //默认目标主机
        HttpHost DEFAULT_TARGETHOST = new HttpHost(Constants.BASE_URL, 80);

        SchemeRegistry schemeRegistry = new SchemeRegistry();

        //添加SSL证书
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        schemeRegistry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(
                new Scheme("https", 443, new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));

        cm = new PoolingClientConnectionManager(schemeRegistry);

        cm.setMaxTotal(Constants.MAX_TOTAL_CONNECTIONS);

        cm.setDefaultMaxPerRoute(Constants.MAX_ROUTE_CONNECTIONS);

        cm.setMaxPerRoute(new HttpRoute(DEFAULT_TARGETHOST), 100);        //设置对目标主机的最大连接数


        parentParams = new BasicHttpParams();
        parentParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        parentParams.setParameter(ClientPNames.DEFAULT_HOST, DEFAULT_TARGETHOST);    //设置默认targetHost

        parentParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        parentParams.setParameter(ClientPNames.CONN_MANAGER_TIMEOUT, Constants.CONN_MANAGER_TIMEOUT);
        parentParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Constants.CONNECT_TIMEOUT);
        parentParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, Constants.SOCKET_TIMEOUT);

        parentParams.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        parentParams.setParameter(ClientPNames.HANDLE_REDIRECTS, true);

        //代理,翻墙
        HttpHost proxy = new HttpHost("127.0.0.1", 8787);
        parentParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

        //设置头信息,模拟浏览器
        Collection collection = new ArrayList();
        collection.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"));
        collection.add(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
        collection.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6"));
        collection.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));

        parentParams.setParameter(ClientPNames.DEFAULT_HEADERS, collection);
        //请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount >= 5) {
                    // 如果超过最大重试次数，那么就不要继续了
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    // 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {
                    // 不要重试SSL握手异常
                    return false;
                }
                HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // 如果请求被认为是幂等的，那么就重试
                    return true;
                }
                return false;
            }
        };

        DefaultHttpClient httpclient = new DefaultHttpClient(cm, parentParams);

        httpclient.setHttpRequestRetryHandler(httpRequestRetryHandler);

        return httpclient;
    }

    /**
     * 解压
     *
     * @param response
     * @return
     */
    public static String decodeResponse(HttpResponse response) {
        StringBuilder entity = new StringBuilder();
        GzipDecompressingEntity gd = new GzipDecompressingEntity(response.getEntity());
        InputStream is = null;
        try {
            is = gd.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                entity.append(line);
                entity.append("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entity.toString();
    }

    /**
     * 检查cookie是否还有效
     *
     * @param httpclient
     * @return
     */
    public static Boolean checkLoginInfoEffective(DefaultHttpClient httpclient) {
        String url = "http://www.pixiv.net/";
        HttpGet httpget = new HttpGet(url);
        String[] cookies = getLoginInfo();
        if (cookies == null) return false;
        String cookieStr = "";
        for (String s : cookies) {
            if (s == null) continue;
            cookieStr += s;
            cookieStr += ";";
        }
        cookieStr = cookieStr.substring(0, cookieStr.length() - 1);
        httpget.setHeader("Cookie", cookieStr);
        String entity = null;
        try {
            HttpResponse response = httpclient.execute(httpget);
            String data = decodeResponse(response);
            Matcher m = Pattern.compile("var\\s*user_id\\s*=\\s*([^;]*)").matcher(data);
            if (m.find()) {
                if (m.group(1).length() == 2) { //user_id不是""（两个双引号），代表登录状态仍存在，COOKIE仍有效
                    return false;
                } else {
                    org.apache.http.client.CookieStore cookieStore = httpclient.getCookieStore();
                    for (String s : cookies) {
                        if (s == null) continue;
                        m = Pattern.compile("^([^=]*)=(.*)").matcher(s);
                        if (m.find()) {
                            //若果cookie有效，设置到httpclient中
                            BasicClientCookie cookie = new BasicClientCookie(m.group(1), m.group(2));
                            cookie.setDomain(".pixiv.net");
                            cookie.setPath("/");
                            cookieStore.addCookie(cookie);
                        }
                    }
                    httpclient.setCookieStore(cookieStore);
                    return true;
                }
            } else {
                throw new Exception("找不到user_id");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpget.releaseConnection();
        }
        return false;
    }

    /**
     * @return
     */
    public static String[] getLoginInfo() {
        String[] res = new String[3];
        File file = new File(Constants.PIXIV_LOGIN_INFO_PATH);
        if (!file.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(Constants.PIXIV_LOGIN_INFO_PATH))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                res[count++] = line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * @param headers
     */
    public static void saveLoginInfo(Header[] headers) {
        File file = new File(Constants.PIXIV_LOGIN_INFO_PATH);
        if (file.exists()) {
            file.delete();
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(Constants.PIXIV_LOGIN_INFO_PATH);
            boolean flag = false;
            for (Header h : headers) {
                if (h.getName().equals("Set-Cookie")) {
                    if (flag == false) {
                        flag = true;
                        continue;
                    }
                    Matcher m = Pattern.compile("(PHPSESSID=[^;]*)").matcher(h.getValue());
                    if (m.find()) {
                        out.println(m.group(1));
                    }
                    m = Pattern.compile("(device_token=[^;]*)").matcher(h.getValue());
                    if (m.find()) {
                        out.println(m.group(1));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    /**
     * @param header
     */
    public static void saveLoginInfo(String header) {
        File file = new File(Constants.PIXIV_LOGIN_INFO_PATH);
        if (file.exists()) {
            file.delete();
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(Constants.PIXIV_LOGIN_INFO_PATH);
            Matcher m = Pattern.compile("(PHPSESSID=[^;]*)").matcher(header);
            if (m.find()) {
                out.println(m.group(1));
            }
            m = Pattern.compile("(device_token=[^;]*)").matcher(header);
            if (m.find()) {
                out.println(m.group(1));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    /**
     *
     * @param entity
     * @param file
     * @throws IOException
     */
    private static void saveToLocal(HttpEntity entity, File file) throws IOException {
        if (entity == null) return;
        InputStream in = entity.getContent();
        FileOutputStream fout = new FileOutputStream(file);
        try {
            int l;
            byte[] tmp = new byte[32 * 2048];
            while ((l = in.read(tmp)) != -1) {
                fout.write(tmp, 0, l);
            }
        } finally {
            fout.close();
            in.close();
        }
    }

    /**
     *
     * @param httpclient
     * @param url
     * @param refer
     * @param targetPath
     * @return
     */
    public static String downloadFile(DefaultHttpClient httpclient, String url, String refer, String targetPath) {
        logger.debug("----------->>>>> " + url);
        System.out.println("Thread " + Thread.currentThread().getId() + " >> " + url);
       /* String targetPath = basePath + authorId + "/";
        File dir = new File(targetPath);            //作者的文件夹
        if(!dir.exists()) {
            dir.mkdirs();
        }*/
        String[] temp = url.split("/");
        String destfilename = temp[temp.length - 1];
        File file = new File(targetPath + destfilename);
        if (file.exists()) {    //如果已经存在文件，不进行下载
            return null;
            //return targetPath + destfilename; //pixiv_test
        }

        File tempFile = new File(targetPath + destfilename.split("\\.")[0]);    //缓存
        if (tempFile.exists()) tempFile.delete();

        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.REFERER, refer);
        try {
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            ContentType.getOrDefault(entity).getMimeType();
            saveToLocal(entity, tempFile);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            httpget.releaseConnection();
        }
        tempFile.renameTo(file);    //图片下完，修改文件类型为图片类型
        return targetPath + destfilename;
    }

    /**
     * @param httpclient
     * @param username
     * @param password
     */
    public static void login(DefaultHttpClient httpclient, String username, String password) {
        if (checkLoginInfoEffective(httpclient)) {
            return;
        }
        HttpPost httppost = new HttpPost("https://www.secure.pixiv.net/login.php");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("mode", "login"));
        formparams.add(new BasicNameValuePair("return_to", "/"));
        formparams.add(new BasicNameValuePair("pixiv_id", username));
        formparams.add(new BasicNameValuePair("pass", password));
        formparams.add(new BasicNameValuePair("skip", "1"));
        UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        httppost.setEntity(reqEntity);

        //Execute post
        String entity = "";
        try {
            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == 302) {
                logger.debug("Login success!");
                Header[] headers = response.getAllHeaders();
                saveLoginInfo(headers);
            } else {
                logger.error("Login failed! Please check your username and password!");
                System.exit(0);
            }

            entity = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("登录超时.");
            System.exit(0);
        } finally {
            httppost.releaseConnection();
        }
    }
}
