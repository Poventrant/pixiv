
package utils;

import models.Pixiv;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.PixivService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings(value="unchecked")
public class PixivCrawler {
    //打印日志
    private final static Logger logger = LoggerFactory.getLogger(PixivCrawler.class);
    public static LinkedList<String> AUTHOR_IDS = new LinkedList<String>();
    //同步文件表，用户同步文件的删除和添加
    private static ConcurrentMap HANDLING_FILE_MAP = new ConcurrentHashMap<String, String>();
    private static DefaultHttpClient httpclient = PixivCrawlerUtil.getHttpClient();

    private static PixivService pixivService = null;


    //获取一共多少页
    private static List<Elements> getPageWorks(String authorId) {
        List<Elements> works = new ArrayList<Elements>();
        int pageCount = 1;
        while (true) {
            String illusts_url = "http://www.pixiv.net/member_illust.php?id=" + authorId + "&type=all&p=" + pageCount;
            HttpGet httpget = new HttpGet(illusts_url);
            String entity = null;
            try {
                HttpResponse response = httpclient.execute(httpget);
                entity = PixivCrawlerUtil.decodeResponse(response);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpget.releaseConnection();
            }

            Document doc = Jsoup.parse(entity.toString());

            Elements eles = doc.getElementsByClass("_work");
            works.add(eles);

            Elements pages = doc.select("a[rel=next]._button");
            if (pages.size() != 0) { //如果还有下一页,另外启动线程并发下载
                ++pageCount;
            } else return works;
        }
    }

    public static void scrawlPagePics(String authorId, Elements finalEles, String url, AtomicInteger totalDownloaded) {
        for (Element e : finalEles) {
            String target = Constants.BASE_URL + e.attr("href").toString().substring(1);
            if (target == null) return;
            Pixiv arpTemp = new Pixiv();

            //获取缩略图路径
            Elements _thumbnail = e.getElementsByClass("_thumbnail");
            String masterUrl = _thumbnail.get(0).attr("src");

            String targetPath = Constants.MASTER_PATH + authorId + "/";
            File dir = new File(targetPath);            //作者的文件夹
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String masterPath =  PixivCrawlerUtil.downloadFile(httpclient, masterUrl, url, targetPath);

            arpTemp.setMasterPath(masterPath);
            enterIllust(authorId, target, url, arpTemp, totalDownloaded);

        }
    }

    //进入“图片详细信息”页面
    private static void enterIllust(String authorId, String target, String refer, Pixiv arpTemp, AtomicInteger totalDownloaded) {

        HttpGet httpget = new HttpGet(target);
        httpget.setHeader(HttpHeaders.REFERER, refer);
        String original_image = null;
        boolean flag = false; //false代表是普通的图片，true表示多图
        try {
            HttpResponse response = httpclient.execute(httpget);
            //String entity = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
            String entity = PixivCrawlerUtil.decodeResponse(response);
            Document doc = Jsoup.parse(entity);
            int score = Integer.parseInt(doc.getElementsByClass("score-count").get(0).text());
            int count = Integer.parseInt(doc.getElementsByClass("rated-count").get(0).text());
            double rate = score * 1.0 / (count == 0 ? 1 : count);
            String authorNmae = doc.getElementsByClass("user").get(0).text();
            Element EworkInfo = doc.getElementsByClass("work-info").get(0);
            String workName = EworkInfo.select(".title").text();
            String workSize = EworkInfo.select(".meta>li").get(1).text();

            //设置实体---------------------------------------------------------
            arpTemp.setName(workName);
            arpTemp.setSize(workSize);
            arpTemp.setOriginHref(target);
            arpTemp.setAuthor(authorNmae + "(" + authorId + ")");//----------------------
            arpTemp.setRate(rate);      //----------------------
            Matcher m = Pattern.compile("illust_id=([0-9]*)").matcher(target);
            if (m.find()) {
                arpTemp.setPicid(m.group(1));
            }


            Elements eles = doc.getElementsByClass("original-image");
            if (eles.size() == 0) {
                eles = doc.select("a._work.multiple");
                if (eles.size() == 0) {
                    logger.error("找不到下一层");
                    return;
                } else {
                    original_image = Constants.BASE_URL + eles.get(0).attr("href");
                    flag = true;
                }
            } else {
                original_image = eles.get(0).attr("data-src");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpget.releaseConnection();
        }
        if (original_image == null) return;
        if (!flag) {

            String targetPath = Constants.DOWNLOAD_PATH + authorId + "/";
            File dir = new File(targetPath);            //作者的文件夹
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String savePath =  PixivCrawlerUtil.downloadFile(httpclient, original_image, target, targetPath);
            if (savePath == null) {
                logger.error("图片下载失败或者图片已经存在!");
                return;
            } else {
                //设置实体---------------------------------------------------------
                arpTemp.setSavePath(savePath);
                arpTemp.setCreateTime(new Date());
                totalDownloaded.incrementAndGet();
                if (pixivService != null) {
                    logger.debug("持久pixiv实体到数据库");
                    pixivService.create(arpTemp);
                }
            }
        } else {
            String savePath = handleMutipleFile(authorId, original_image, target, arpTemp);
            if (savePath == null) {
                logger.error("图片下载失败或者图片已经存在!");
                return;
            }
            arpTemp.setSavePath(savePath);
            arpTemp.setCreateTime(new Date());
            totalDownloaded.addAndGet(arpTemp.getPicno());
            if (pixivService != null) {
                logger.debug("持久pixiv实体到数据库");
                pixivService.create(arpTemp);
            }
        }
    }

    private static String handleMutipleFile(String authorId, String url, String refer, Pixiv arpTemp) {
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.REFERER, refer);
        String savePath = null;
        try {
            HttpResponse response = httpclient.execute(httpget);
            //String entity = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
            String entity = PixivCrawlerUtil.decodeResponse(response);
            Document doc = Jsoup.parse(entity);
            Elements eles = doc.select("a.full-size-container._ui-tooltip");
            for (Element e : eles) {
                String target = Constants.BASE_URL + e.attr("href").substring(1);
                HttpGet tempget = new HttpGet(target);
                tempget.setHeader(HttpHeaders.REFERER, url);
                HttpResponse tempResp = httpclient.execute(tempget);
                String tempEntity = PixivCrawlerUtil.decodeResponse(tempResp);
                Document tempDoc = Jsoup.parse(tempEntity);
                String imgSrc = tempDoc.getElementsByTag("img").get(0).attr("src");

                String targetPath = Constants.DOWNLOAD_PATH + authorId + "/";
                File dir = new File(targetPath);            //作者的文件夹
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String tsavePath = PixivCrawlerUtil.downloadFile(httpclient, imgSrc, target, targetPath);

                if (savePath == null) savePath = tsavePath;      //只以第一张图作为保存路径
                tempget.releaseConnection();
            }
            arpTemp.setPicno(Short.parseShort(String.valueOf(eles.size())));    //设置有多少张图
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpget.releaseConnection();
        }
        return savePath;
    }

    private static void logout() {
        String url = "http://www.pixiv.net/logout.php?return_to=%2Fapps.php%3Fref%3Dlogout";
        HttpGet httpget = new HttpGet(url);
        try {
            httpclient.execute(httpget);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpget.releaseConnection();
            //注销，删除暂存信息
            File file = new File(Constants.PIXIV_LOGIN_INFO_PATH);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static List<String> getAuthorIdList() {
        List<String> list = new ArrayList<String>(64);
        File file = new File(Constants.DOWNLOAD_PATH);
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                list.add(f.getName());
            }
        }
        return list;
    }

    public static void deleteAuthor(String authorId) throws Exception {
        if (HANDLING_FILE_MAP.containsKey(authorId)) {
            throw new Exception("文件操作错误");      //如果文件正在被进行其他操作，比如删除，那么异常中断
        }
        HANDLING_FILE_MAP.put(authorId, "deleting...");


        File file = new File(Constants.DOWNLOAD_PATH + authorId);
        if (!deleteDirectory(file)) {
            throw new Exception("删除错误");
        }
        file = new File(Constants.MASTER_PATH + authorId);
        if (!deleteDirectory(file)) {
            throw new Exception("删除错误");
        }

        HANDLING_FILE_MAP.remove(authorId);
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    final static int POOL_SIZE = (Runtime.getRuntime().availableProcessors() + 1) * 10;

    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(POOL_SIZE);

    public static final WorkerQueue WORK_QUEUE = new WorkerQueue(POOL_SIZE);

    public static void setService(PixivService pixivService) {
        if(PixivCrawler.pixivService == null) {
            PixivCrawler.pixivService = pixivService;
        }
    }

    static class AuthorWorker extends Thread {

        private AtomicInteger totalDownloaded;

        private String authorId;

        private CountDownLatch counter;

        AuthorWorker(String authorId) {
            this.authorId = authorId;
            totalDownloaded = new AtomicInteger(0);
        }

        @Override
        public void run() {
            //申请pages个空闲的线程, 重定义这些线程的内容
            List<Elements> works = getPageWorks(authorId);
            int pages = works.size();
            counter = new CountDownLatch(pages);
            for (int i = 0; i < pages; ++i) {   //按页爬取
                final String illusts_url = "http://www.pixiv.net/member_illust.php?id=" + authorId + "&type=all&p=" + (i + 1);
                Worker worker = WORK_QUEUE.work();
                worker.reset(authorId, works.get(i), illusts_url, counter, totalDownloaded);
                EXECUTOR.execute(worker);
            }
            try {
                counter.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(authorId + ": " + "下载完成, 共下载了" + totalDownloaded.get() + "张图片。");
                AUTHOR_IDS.remove(authorId);
                HANDLING_FILE_MAP.remove(authorId);     //下载完之后解除操作锁定
            }
        }
    }

    static class Worker extends Thread {

        private String authorId;

        private Elements elements;

        private String illusts;

        private CountDownLatch counter;

        private AtomicInteger totalDownloaded;

        @Override
        public void run() {
            try {
                scrawlPagePics(authorId, elements, illusts, totalDownloaded);
            } finally {
                counter.countDown();
                //空闲当前线程
                WORK_QUEUE.rest(this);
            }
        }

        public void reset(String authorId, Elements elements, String illusts, CountDownLatch counter, AtomicInteger totalDownloaded) {
            this.authorId = authorId;
            this.elements = elements;
            this.illusts = illusts;
            this.counter = counter;
            this.totalDownloaded = totalDownloaded;
        }

    }

    static class WorkerQueue {
        BlockingQueue<Worker> availableQueue;
        final Object moniter = new Object();

        WorkerQueue(Worker[] workers) {
            availableQueue = new ArrayBlockingQueue<Worker>(POOL_SIZE);
            try {
                for (Worker worker : workers) {
                    availableQueue.put(worker);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        WorkerQueue(int num) {
            availableQueue = new ArrayBlockingQueue<Worker>(POOL_SIZE);
            try {
                for (int i = 0; i < num; i++) {
                    availableQueue.put(new Worker());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public Worker work() {
            try {
                return availableQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void rest(Worker worker) {
            try {
                availableQueue.put(worker);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void doStart(String authorId) {
        DefaultHttpClient httpClient = PixivCrawlerUtil.getHttpClient();
        PixivCrawlerUtil.login(httpClient, "296306654@qq.com", "PWQ2080064");

        new AuthorWorker(authorId).start();
    }

    public static void main(String[] args) {

        try {
            DefaultHttpClient httpClient = PixivCrawlerUtil.getHttpClient();
            PixivCrawlerUtil.login(httpClient, "296306654@qq.com", "PWQ2080064");

            List<String> authors = Arrays.asList("133714504");

            for(String authorId : authors) {
                new AuthorWorker(authorId).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static {
        //取消打印日志
        java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
    }
}
