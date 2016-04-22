//
//package utils;
//
//import models.Pixiv;
//import org.apache.http.*;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpRequestRetryHandler;
//import org.apache.http.client.entity.GzipDecompressingEntity;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.params.ClientPNames;
//import org.apache.http.client.params.CookiePolicy;
//import org.apache.http.conn.routing.HttpRoute;
//import org.apache.http.conn.scheme.PlainSocketFactory;
//import org.apache.http.conn.scheme.Scheme;
//import org.apache.http.conn.scheme.SchemeRegistry;
//import org.apache.http.conn.ssl.SSLSocketFactory;
//import org.apache.http.entity.ContentType;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.impl.conn.PoolingClientConnectionManager;
//import org.apache.http.impl.cookie.BasicClientCookie;
//import org.apache.http.message.BasicHeader;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.params.BasicHttpParams;
//import org.apache.http.params.CoreConnectionPNames;
//import org.apache.http.params.CoreProtocolPNames;
//import org.apache.http.params.HttpParams;
//import org.apache.http.protocol.ExecutionContext;
//import org.apache.http.protocol.HttpContext;
//import org.apache.http.util.EntityUtils;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import services.PixivService;
//
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLHandshakeException;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import java.io.*;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class PixivUtilBackup {
//
//    /**
//     * 打印日志
//     */
//    private final static Logger logger = LoggerFactory.getLogger(PixivUtilBackup.class);
//
//    public final static String DOWNLOAD_PATH = "public/images/pixiv/";
//    public final static String MASTER_PATH = "public/images//pixiv_master/";
//    public final static String BASE_URL = "http://www.pixiv.net/";
//    public final static String PIXIV_LOGIN_INFO_PATH = "PixivLoginInfo";
//    private String authorId = null;
//    public static LinkedList<String> AUTHOR_IDS = new LinkedList<String>();
//    public static Date RANK_DATE = null;
//    private AtomicInteger TOTALDOWNLOADED = null;
//    private PixivService ARPIXIVSERVICE = null;
//    //同步文件表，用户同步文件的删除和添加
//    private static ConcurrentMap HANDLING_FILE_MAP = new ConcurrentHashMap<String, String>();
//
//    /**
//     * 连接池里的最大连接数
//     */
//    private static final int MAX_TOTAL_CONNECTIONS = 100;
//
//    /**
//     * 每个路由的默认最大连接数
//     */
//    private static final int MAX_ROUTE_CONNECTIONS = 80;
//
//    /**
//     * 连接超时时间
//     */
//    private static final int CONNECT_TIMEOUT = 60000;
//
//    /**
//     * 套接字超时时间
//     */
//    private static final int SOCKET_TIMEOUT = 80000;
//
//    /**
//     * 连接池中 连接请求执行被阻塞的超时时间
//     */
//    private static final long CONN_MANAGER_TIMEOUT = 100000;
//
//    /**
//     * http连接相关参数
//     */
//    private static HttpParams parentParams;
//
//    /**
//     * http线程池管理器
//     */
//    private static PoolingClientConnectionManager cm;
//    /**
//     * http客户端
//     */
//    private static DefaultHttpClient httpclient;
//    /**
//     * 默认目标主机
//     */
//    private static final HttpHost DEFAULT_TARGETHOST = new HttpHost(BASE_URL, 80);
//
//    public PixivUtilBackup() {
//    }
//
//    public PixivUtilBackup(String authorId, PixivService pixivService) throws Exception {
//        if (!checkLoginInfoEffective()) {
//            throw new Exception("用于登录PIXIV的Cookie过期，请重新设置");
//        } else {
//            if (AUTHOR_IDS.contains(authorId)) {
//                throw new Exception("作者已经在下载列表中！");
//            }
//            if (HANDLING_FILE_MAP.containsKey(authorId)) {
//                throw new Exception("文件操作错误");      //如果文件正在被进行其他操作，比如删除，那么异常中断
//            }
//            AUTHOR_IDS.add(authorId);
//            HANDLING_FILE_MAP.put(authorId, "downloading...");
//            this.authorId = authorId;
//            ARPIXIVSERVICE = pixivService;
//            TOTALDOWNLOADED = new AtomicInteger(0);
//        }
//    }
//
//    static {
//        //取消打印日志
//        java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
//        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);
//        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
//
//        SchemeRegistry schemeRegistry = new SchemeRegistry();
//
//        //添加SSL证书
//        SSLContext ctx = null;
//        try {
//            ctx = SSLContext.getInstance("TLS");
//            X509TrustManager tm = new X509TrustManager() {
//                public X509Certificate[] getAcceptedIssuers() {
//                    return null;
//                }
//
//                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
//                }
//
//                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
//                }
//            };
//            ctx.init(null, new TrustManager[]{tm}, null);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        }
//
//        schemeRegistry.register(
//                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
//        schemeRegistry.register(
//                new Scheme("https", 443, new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));
//
//        cm = new PoolingClientConnectionManager(schemeRegistry);
//
//        cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
//
//        cm.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
//
//        cm.setMaxPerRoute(new HttpRoute(DEFAULT_TARGETHOST), 100);        //设置对目标主机的最大连接数
//
//
//        parentParams = new BasicHttpParams();
//        parentParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
//
//        parentParams.setParameter(ClientPNames.DEFAULT_HOST, DEFAULT_TARGETHOST);    //设置默认targetHost
//
//        parentParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
//
//        parentParams.setParameter(ClientPNames.CONN_MANAGER_TIMEOUT, CONN_MANAGER_TIMEOUT);
//        parentParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);
//        parentParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT);
//
//        parentParams.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
//        parentParams.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
//
//        //代理,翻墙
//      /*  HttpHost proxy = new HttpHost("127.0.0.1", 8787);
//        parentParams.setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);*/
//
//        //设置头信息,模拟浏览器
//        Collection collection = new ArrayList();
//        collection.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"));
//        collection.add(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
//        collection.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6"));
//        collection.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));
//
//        parentParams.setParameter(ClientPNames.DEFAULT_HEADERS, collection);
//        //请求重试处理
//        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
//            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
//                if (executionCount >= 5) {
//                    // 如果超过最大重试次数，那么就不要继续了
//                    return false;
//                }
//                if (exception instanceof NoHttpResponseException) {
//                    // 如果服务器丢掉了连接，那么就重试
//                    return true;
//                }
//                if (exception instanceof SSLHandshakeException) {
//                    // 不要重试SSL握手异常
//                    return false;
//                }
//                HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
//                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
//                if (idempotent) {
//                    // 如果请求被认为是幂等的，那么就重试
//                    return true;
//                }
//                return false;
//            }
//        };
//
//
//        httpclient = new DefaultHttpClient(cm, parentParams);
//
//        httpclient.setHttpRequestRetryHandler(httpRequestRetryHandler);
//
//
//    }
//
//    public void login(String username, String password) {
//        if (checkLoginInfoEffective()) {
//            return;
//        }
//        HttpPost httppost = new HttpPost("https://www.secure.pixiv.net/login.php");
//        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//        formparams.add(new BasicNameValuePair("mode", "login"));
//        formparams.add(new BasicNameValuePair("return_to", "/"));
//        formparams.add(new BasicNameValuePair("pixiv_id", username));
//        formparams.add(new BasicNameValuePair("pass", password));
//        formparams.add(new BasicNameValuePair("skip", "1"));
//        UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
//        httppost.setEntity(reqEntity);
//
//        //Execute post
//        String entity = "";
//        try {
//            HttpResponse response = httpclient.execute(httppost);
//            if (response.getStatusLine().getStatusCode() == 302) {
//                logger.debug("Login success!");
//                Header[] headers = response.getAllHeaders();
//                saveLoginInfo(headers);
//            } else {
//                logger.error("Login failed! Please check your username and password!");
//                System.exit(0);
//            }
//
//            entity = EntityUtils.toString(response.getEntity());
//        } catch (IOException e) {
//            e.printStackTrace();
//            logger.error("登录超时.");
//            System.exit(0);
//        } finally {
//            httppost.releaseConnection();
//        }
//
//    }
//
//    //检查cookie是否还有效
//    private Boolean checkLoginInfoEffective() {
//        String url = "http://www.pixiv.net/";
//        HttpGet httpget = new HttpGet(url);
//        String[] cookies = getLoginInfo();
//        if (cookies == null) return false;
//        String cookieStr = "";
//        for (String s : cookies) {
//            if (s == null) continue;
//            cookieStr += s;
//            cookieStr += ";";
//        }
//        cookieStr = cookieStr.substring(0, cookieStr.length() - 1);
//        httpget.setHeader("Cookie", cookieStr);
//        String entity = null;
//        try {
//            HttpResponse response = httpclient.execute(httpget);
//            String data = decodeResponse(response);
//            Matcher m = Pattern.compile("var\\s*user_id\\s*=\\s*([^;]*)").matcher(data);
//            if (m.find()) {
//                if (m.group(1).length() == 2) { //user_id不是""（两个双引号），代表登录状态仍存在，COOKIE仍有效
//                    return false;
//                } else {
//                    org.apache.http.client.CookieStore cookieStore = httpclient.getCookieStore();
//                    for (String s : cookies) {
//                        if (s == null) continue;
//                        m = Pattern.compile("^([^=]*)=(.*)").matcher(s);
//                        if (m.find()) {
//                            //若果cookie有效，设置到httpclient中
//                            BasicClientCookie cookie = new BasicClientCookie(m.group(1), m.group(2));
//                            cookie.setDomain(".pixiv.net");
//                            cookie.setPath("/");
//                            cookieStore.addCookie(cookie);
//                        }
//                    }
//                    httpclient.setCookieStore(cookieStore);
//                    return true;
//                }
//            } else {
//                throw new Exception("找不到user_id");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            httpget.releaseConnection();
//        }
//        return false;
//    }
//
//    private String[] getLoginInfo() {
//        String[] res = new String[3];
//        File file = new File(PIXIV_LOGIN_INFO_PATH);
//        if (!file.exists()) return null;
//        try (BufferedReader br = new BufferedReader(new FileReader(PIXIV_LOGIN_INFO_PATH))) {
//            String line;
//            int count = 0;
//            while ((line = br.readLine()) != null) {
//                res[count++] = line;
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return res;
//    }
//
//    private void saveLoginInfo(Header[] headers) {
//        File file = new File(PIXIV_LOGIN_INFO_PATH);
//        if (file.exists()) {
//            file.delete();
//        }
//        PrintWriter out = null;
//        try {
//            out = new PrintWriter(PIXIV_LOGIN_INFO_PATH);
//            boolean flag = false;
//            for (Header h : headers) {
//                if (h.getName().equals("Set-Cookie")) {
//                    if (flag == false) {
//                        flag = true;
//                        continue;
//                    }
//                    Matcher m = Pattern.compile("(PHPSESSID=[^;]*)").matcher(h.getValue());
//                    if (m.find()) {
//                        out.println(m.group(1));
//                    }
//                    m = Pattern.compile("(device_token=[^;]*)").matcher(h.getValue());
//                    if (m.find()) {
//                        out.println(m.group(1));
//                    }
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            out.close();
//        }
//    }
//
//    public static void saveLoginInfo(String header) {
//        File file = new File(PIXIV_LOGIN_INFO_PATH);
//        if (file.exists()) {
//            file.delete();
//        }
//        PrintWriter out = null;
//        try {
//            out = new PrintWriter(PIXIV_LOGIN_INFO_PATH);
//            Matcher m = Pattern.compile("(PHPSESSID=[^;]*)").matcher(header);
//            if (m.find()) {
//                out.println(m.group(1));
//            }
//            m = Pattern.compile("(device_token=[^;]*)").matcher(header);
//            if (m.find()) {
//                out.println(m.group(1));
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            out.close();
//        }
//    }
//
//    //同步获取
//    public void getByAuthor(String page) {
//        String illusts_url = "http://www.pixiv.net/member_illust.php?id=" + authorId + "&type=all&p=" + page;
//        HttpGet httpget = new HttpGet(illusts_url);
//        String entity = null;
//        try {
//            HttpResponse response = httpclient.execute(httpget);
//            entity = decodeResponse(response);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            httpget.releaseConnection();
//        }
//
//        Document doc = Jsoup.parse(entity.toString());
//        Elements eles = doc.getElementsByClass("_work");
//        for (Element e : eles) {
//            String target = BASE_URL + e.attr("href").toString().substring(1);
//            if (target == null) return;
//            Pixiv arpTemp = new Pixiv();
//            enterIllust(target, illusts_url, arpTemp);
//        }
//        Elements pages = doc.select("a[rel=next]._button");
//        if (pages.size() != 0) {
//            getByAuthor(String.valueOf(Integer.parseInt(page) + 1));
//        }
//    }
//
//    //获取一共多少页
//    private List<Elements> getPageWorks() {
//        List<Elements> works = new ArrayList<Elements>();
//        int pageCount = 1;
//        while (true) {
//            String illusts_url = "http://www.pixiv.net/member_illust.php?id="
//                    + authorId + "&type=all&p=" + pageCount;
//            HttpGet httpget = new HttpGet(illusts_url);
//            String entity = null;
//            try {
//                HttpResponse response = httpclient.execute(httpget);
//                entity = decodeResponse(response);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                httpget.releaseConnection();
//            }
//
//            Document doc = Jsoup.parse(entity.toString());
//
//            Elements eles = doc.getElementsByClass("_work");
//            works.add(eles);
//
//            Elements pages = doc.select("a[rel=next]._button");
//            if (pages.size() != 0) { //如果还有下一页,另外启动线程并发下载
//                ++pageCount;
//            } else return works;
//        }
//    }
//
//    //并发获取
//    public void getByAuthorAync() {
//        List<Elements> works = getPageWorks();
//        int len = works.size();
//        Thread[] threads = new Thread[len];
//        for (int i = 0; i < len; ++i) {   //按页爬取
//            Elements eles = works.get(i);
//            int page = i + 1;
//            final String illusts_url = "http://www.pixiv.net/member_illust.php?id="
//                    + authorId + "&type=all&p=" + page;
//            final Elements finalEles = eles;
//            threads[i] = new Thread() {
//                @Override
//                public void run() {
//                    for (Element e : finalEles) {
//                        String target = BASE_URL + e.attr("href").toString().substring(1);
//                        if (target == null) return;
//                        Pixiv arpTemp = new Pixiv();
//
//                        //获取缩略图路径
//                        Elements _thumbnail = e.getElementsByClass("_thumbnail");
//                        String masterUrl = _thumbnail.get(0).attr("src");
//
//                        String targetPath = MASTER_PATH + authorId + "/";
//                        File dir = new File(targetPath);            //作者的文件夹
//                        if (!dir.exists()) {
//                            dir.mkdirs();
//                        }
//                        String masterPath = downloadFile(masterUrl, illusts_url, targetPath);
//
//                        arpTemp.setMasterPath(masterPath);
//                        enterIllust(target, illusts_url, arpTemp);
//
//                    }
//                }
//            };
//            threads[i].start();
//        }
//        for (int i = 0; i < len; i++) {
//            try {
//                threads[i].join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("-----------" + "下载完成。");
//        System.out.println("-----------" + "共下载了" + TOTALDOWNLOADED.get() + "张图片。");
//        AUTHOR_IDS.remove(this.authorId);
//        HANDLING_FILE_MAP.remove(this.authorId);     //下载完之后解除操作锁定
//        this.authorId = null;   //下载完之后还原
//        ARPIXIVSERVICE = null;
//    }
//
//    public void doGetByAuthorAync() {
//        Thread mythread = new Thread() {
//            @Override
//            public void run() {
//                getByAuthorAync();
//            }
//        };
//        mythread.start();
//    }
//
//
//    //进入“图片详细信息”页面
//    private void enterIllust(String target, String refer, Pixiv arpTemp) {
//
//        HttpGet httpget = new HttpGet(target);
//        httpget.setHeader(HttpHeaders.REFERER, refer);
//        String original_image = null;
//        boolean flag = false; //false代表是普通的图片，true表示多图
//        try {
//            HttpResponse response = httpclient.execute(httpget);
//            //String entity = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
//            String entity = decodeResponse(response);
//            Document doc = Jsoup.parse(entity);
//            int score = Integer.parseInt(doc.getElementsByClass("score-count").get(0).text());
//            int count = Integer.parseInt(doc.getElementsByClass("rated-count").get(0).text());
//            double rate = score * 1.0 / (count == 0 ? 1 : count);
//            String authorNmae = doc.getElementsByClass("user").get(0).text();
//            Element EworkInfo = doc.getElementsByClass("work-info").get(0);
//            String workName = EworkInfo.select(".title").text();
//            String workSize = EworkInfo.select(".meta>li").get(1).text();
//
//            //设置实体---------------------------------------------------------
//            arpTemp.setName(workName);
//            arpTemp.setSize(workSize);
//            arpTemp.setOriginHref(target);
//            arpTemp.setAuthor(authorNmae + "(" + authorId + ")");//----------------------
//            arpTemp.setRate(rate);      //----------------------
//            Matcher m = Pattern.compile("illust_id=([0-9]*)").matcher(target);
//            if (m.find()) {
//                arpTemp.setPicid(m.group(1));
//            }
//
//
//            Elements eles = doc.getElementsByClass("original-image");
//            if (eles.size() == 0) {
//                eles = doc.select("a._work.multiple");
//                if (eles.size() == 0) {
//                    logger.error("找不到下一层");
//                    return;
//                } else {
//                    original_image = BASE_URL + eles.get(0).attr("href");
//                    flag = true;
//                }
//            } else {
//                original_image = eles.get(0).attr("data-src");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            httpget.releaseConnection();
//        }
//        if (original_image == null) return;
//        if (!flag) {
//
//            String targetPath = DOWNLOAD_PATH + authorId + "/";
//            File dir = new File(targetPath);            //作者的文件夹
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            String savePath = downloadFile(original_image, target, targetPath);
//            if (savePath == null) {
//                logger.error("图片下载失败或者图片已经存在!");
//                return;
//            } else {
//                //设置实体---------------------------------------------------------
//                arpTemp.setSavePath(savePath);
//                arpTemp.setCreateTime(new Date());
//                TOTALDOWNLOADED.incrementAndGet();
//                if (ARPIXIVSERVICE != null) {
//                    logger.debug("持久pixiv实体到数据库");
//                    ARPIXIVSERVICE.create(arpTemp);
//                }
//            }
//        } else {
//            String savePath = handleMutipleFile(original_image, target, arpTemp);
//            if (savePath == null) {
//                logger.error("图片下载失败或者图片已经存在!");
//                return;
//            }
//            arpTemp.setSavePath(savePath);
//            arpTemp.setCreateTime(new Date());
//            TOTALDOWNLOADED.addAndGet(arpTemp.getPicno());
//            if (ARPIXIVSERVICE != null) {
//                logger.debug("持久pixiv实体到数据库");
//                ARPIXIVSERVICE.create(arpTemp);
//            }
//        }
//    }
//
//    private String handleMutipleFile(String url, String refer, Pixiv arpTemp) {
//        HttpGet httpget = new HttpGet(url);
//        httpget.setHeader(HttpHeaders.REFERER, refer);
//        String savePath = null;
//        try {
//            HttpResponse response = httpclient.execute(httpget);
//            //String entity = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
//            String entity = decodeResponse(response);
//            Document doc = Jsoup.parse(entity);
//            Elements eles = doc.select("a.full-size-container._ui-tooltip");
//            for (Element e : eles) {
//                String target = BASE_URL + e.attr("href").substring(1);
//                HttpGet tempget = new HttpGet(target);
//                tempget.setHeader(HttpHeaders.REFERER, url);
//                HttpResponse tempResp = httpclient.execute(tempget);
//                String tempEntity = decodeResponse(tempResp);
//                Document tempDoc = Jsoup.parse(tempEntity);
//                String imgSrc = tempDoc.getElementsByTag("img").get(0).attr("src");
//
//                String targetPath = DOWNLOAD_PATH + authorId + "/";
//                File dir = new File(targetPath);            //作者的文件夹
//                if (!dir.exists()) {
//                    dir.mkdirs();
//                }
//                String tsavePath = downloadFile(imgSrc, target, targetPath);
//
//                if (savePath == null) savePath = tsavePath;      //只以第一张图作为保存路径
//                tempget.releaseConnection();
//            }
//            arpTemp.setPicno(Short.parseShort(String.valueOf(eles.size())));    //设置有多少张图
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            httpget.releaseConnection();
//        }
//        return savePath;
//    }
//
//    private void saveToLocal(HttpEntity entity, File file) throws IOException {
//        if (entity == null) return;
//        InputStream in = entity.getContent();
//        FileOutputStream fout = new FileOutputStream(file);
//        try {
//            int l;
//            byte[] tmp = new byte[32 * 2048];
//            while ((l = in.read(tmp)) != -1) {
//                fout.write(tmp, 0, l);
//            }
//        } finally {
//            fout.close();
//            in.close();
//        }
//    }
//
//    public String downloadFile(String url, String refer, String targetPath) {
//        logger.debug("----------->>>>> " + url);
//        System.out.println("Thread " + Thread.currentThread().getId() + " >> " + url);
//       /* String targetPath = basePath + authorId + "/";
//        File dir = new File(targetPath);            //作者的文件夹
//        if(!dir.exists()) {
//            dir.mkdirs();
//        }*/
//        String[] temp = url.split("/");
//        String destfilename = temp[temp.length - 1];
//        File file = new File(targetPath + destfilename);
//        if (file.exists()) {    //如果已经存在文件，不进行下载
//            return null;
//            //return targetPath + destfilename; //pixiv_test
//        }
//
//        File tempFile = new File(targetPath + destfilename.split("\\.")[0]);    //缓存
//        if (tempFile.exists()) tempFile.delete();
//
//        HttpGet httpget = new HttpGet(url);
//        httpget.setHeader(HttpHeaders.REFERER, refer);
//        try {
//            HttpResponse response = httpclient.execute(httpget);
//            HttpEntity entity = response.getEntity();
//            ContentType.getOrDefault(entity).getMimeType();
//            saveToLocal(entity, tempFile);
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//            return null;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//            httpget.releaseConnection();
//        }
//        tempFile.renameTo(file);    //图片下完，修改文件类型为图片类型
//        return targetPath + destfilename;
//    }
//
//    //解压*****************************
//    private String decodeResponse(HttpResponse response) {
//        StringBuilder entity = new StringBuilder();
//        /*try {
//            System.out.println(EntityUtils.toString(response.getEntity()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }*/
//        GzipDecompressingEntity gd = new GzipDecompressingEntity(response.getEntity());
//        InputStream is = null;
//        try {
//            is = gd.getContent();
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                entity.append(line);
//                entity.append("\r\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return entity.toString();
//    }
//
//    private void logout() {
//        String url = "http://www.pixiv.net/logout.php?return_to=%2Fapps.php%3Fref%3Dlogout";
//        HttpGet httpget = new HttpGet(url);
//        try {
//            httpclient.execute(httpget);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            httpget.releaseConnection();
//            //注销，删除暂存信息
//            File file = new File(PIXIV_LOGIN_INFO_PATH);
//            if (file.exists()) {
//                file.delete();
//            }
//        }
//    }
//
//    public static List<String> getAuthorIdList() {
//        List<String> list = new ArrayList<String>(64);
//        File file = new File(DOWNLOAD_PATH);
//        File[] files = file.listFiles();
//        for (File f : files) {
//            if (f.isDirectory()) {
//                list.add(f.getName());
//            }
//        }
//        return list;
//    }
//
//    public static void deleteAuthor(String authorId) throws Exception {
//        if (HANDLING_FILE_MAP.containsKey(authorId)) {
//            throw new Exception("文件操作错误");      //如果文件正在被进行其他操作，比如删除，那么异常中断
//        }
//        HANDLING_FILE_MAP.put(authorId, "deleting...");
//
//
//        File file = new File(DOWNLOAD_PATH + authorId);
//        if (!deleteDirectory(file)) {
//            throw new Exception("删除错误");
//        }
//        file = new File(MASTER_PATH + authorId);
//        if (!deleteDirectory(file)) {
//            throw new Exception("删除错误");
//        }
//
//        HANDLING_FILE_MAP.remove(authorId);
//    }
//
//    public static boolean deleteDirectory(File directory) {
//        if (directory.exists()) {
//            File[] files = directory.listFiles();
//            if (null != files) {
//                for (int i = 0; i < files.length; i++) {
//                    if (files[i].isDirectory()) {
//                        deleteDirectory(files[i]);
//                    } else {
//                        files[i].delete();
//                    }
//                }
//            }
//        }
//        return (directory.delete());
//    }
//
//    public static void main(String[] args) {
//
//        //login("296306654@qq.com", "密码");
//        //   new PixivUtilBackup("396769", null).doGetByAuthorAync();
//      /*  this.AUTHOR_ID = "396769";
//       // getByAuthor("1");
//      //  logout();
//        /*doGetByAuthorAync();*//*
//        File file = new File(DOWNLOAD_PATH);
//        File[] files = file.listFiles();
//        for(File f : files) {
//            if(f.isDirectory()) {
//                System.out.println(f.getName());
//            }
//        }*/
//        String destfilename = "dsadas/dsadas.jpg";
//        String[] temps = destfilename.split("\\.");
//        System.out.println(temps[1] + " " + temps[0]);
//    }
//}
