package com.zhua.scraper;

import com.zhua.dao.LdjsDao;
import com.zhua.dao.ZzjgDao;
import com.zhua.service.LdjsService;
import com.zhua.service.ZzjgService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Controller
@Component
public class LdjsSc {

    @Autowired
    LdjsService ldjsService;
    public void scrapeLdjs() {
        /* dataList 用于存储抓取到的首页信息，每个首页信息都表示为一个 ShouyeDao 对象，
         然后将这些对象添加到 dataList 中。最后，通过调用 shouyeService.saveBatch(dataList);
         方法，将 dataList 中的数据批量插入到数据库中，以完成数据持久化操作。*/
        String baseUrl = "http://www.wz.gov.cn/jkq/wzjkq/jkqld/";
        //李忠云 URL
        String lzyUrl = "http://www.wz.gov.cn/jkq/wzjkq/jkqld/lzy/";
        //程宏江 URL
        String chjUrl = "http://www.wz.gov.cn/jkq/wzjkq/jkqld/chj/";
        //吴雪峰 URL
        String wxfUrl = "http://www.wz.gov.cn/jkq/wzjkq/jkqld/wxf/";
        List<LdjsDao> dataList = new ArrayList<>();
        try {
            int lzyBid = 1;
            // 连接李忠云的 URL 并解析页面内容
            Document lzy = Jsoup.parse(new URL(lzyUrl), 30000);
            // 直接调用 extractInformation 函数
            extractInformation(lzyUrl, dataList, lzyUrl, "李忠云", lzyBid);

            // 程宏江2
            // 连接领导2 的 URL 并解析页面内容
            Document chjDoc = Jsoup.parse(new URL(chjUrl), 30000);
            // 直接调用 extractInformation 函数
            extractInformation(chjUrl, dataList, chjUrl, "程宏江", 2);

            // 吴雪峰3
            // 直接连接吴雪峰的 URL 并解析页面内容
            Document wxfDoc = Jsoup.parse(new URL(wxfUrl), 30000);
            // 直接调用 extractInformation 函数
            extractInformation(wxfUrl, dataList, wxfUrl, "吴雪峰", 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ldjsService.saveBatch(dataList);
        System.out.println("数据插入成功！");
    }

    private int extractInformation(String baseUrl, List<LdjsDao> dataList, String NewUrl, String category, int bid) throws IOException {
        LdjsDao ldjsDao = new LdjsDao();
        Document doc = Jsoup.connect(NewUrl).get();
        // 抓取领导名字
        String leaderName = "";
        // 抓取领导职务
        String leaderDuty = "";
        // 抓取领导链接
        String leaderLink = "";
        //抓取下面的信息
        Elements leaderElements = doc.select(".leader-list li");
        for (Element leaderElement  : leaderElements) {
            /*for 循环中的 newsItem 变量是用来代表 newsItems 中的每个元素，
            使得可以在循环体内对每个新闻项进行操作，比如提取新闻内容或执行其他相关操作。
            这个循环的作用是对多个新闻项进行相同或类似的处理，以便提取和处理网页中的多个新闻条目*/
            // 抓取领导名字
            leaderName = leaderElement.select(".leader-list-name").text();
            // 抓取领导职务
            leaderDuty = leaderElement.select(".leader-info-duty span").text();
            // 抓取领导链接
            leaderLink = leaderElement.select("a").attr("href");

        }
        // 抓取领导图片链接
        String leaderImageLink = doc.select(".leader-info img").attr("src");
        //提取正文
        // 使用一个选择器来选择正文内容
        Elements ccontentElements = doc.select("div.leader-right.rt");
        // 使用 StringBuilder 构建文本
        StringBuilder ccontentBuilder = new StringBuilder();
        for (Element contentElement : ccontentElements) {
            ccontentBuilder.append(contentElement.text()).append("\n");
        }
        // 最后添加一个换行符，以确保最后一行后也有一个换行符
        ccontentBuilder.append("\n");
        // 获取提取的正文内容
        String articleContent="";
        articleContent = ccontentBuilder.toString().trim();//正文结束
        /*找到图片连接、并下载、并存入*/
        // 遍历图片元素并输出其src属性值
        if (leaderImageLink!=null){
            //在div元素里面选择所有图片元素
                int lastSlashIndex = baseUrl.lastIndexOf('/');
                if (lastSlashIndex != -1) {
                    // 删除最后一个斜杠以及后面的部分
                    String newUrl = baseUrl.substring(0, lastSlashIndex);
                    //拼接新的img的Url
                    String newLink = newUrl + leaderImageLink;
                    newLink = newLink.replace("./", "/");
                    try {
                        //获取图片存放地址
                        URL imageUrl = new URL(newLink);
                        URLConnection connection = imageUrl.openConnection();
                        InputStream inputStream = connection.getInputStream();

                        // 获取原始图片文件名，图片链接的最后一部分就是文件名
                        String[] urlParts = newLink.split("/");
                        String originalFileName = urlParts[urlParts.length - 1];
                        // 构建目标文件路径，替换为您希望保存图片的文件夹路径
                        String outputPath = "D:\\java\\WanZhouImg2\\" + originalFileName;

                        FileOutputStream outputStream = new FileOutputStream(outputPath);
                                /*缓冲区的大小是1024字节，是一个合理的大小。缓冲区的作用是一次性读取和写入一定数量的字节，
                                以提高读写效率。每次从输入流中读取一部分数据（最多1024字节），然后将这些数据写入输出流。
                                循环会一直运行，直到输入流中没有更多数据可读，此时 inputStream.read(buffer) 会返回 -1，循环将终止。*/
                        byte[] buffer = new byte[1024]; // 缓冲区大小，您可以根据需要调整
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        // 关闭流
                        inputStream.close();
                        outputStream.close();

                        // 将目标文件路径保存到数据库中
                        // 这里将 outputPath 保存到数据库中，您可以根据数据库的操作进行相应的实现
                        ldjsDao.setCimgurldz(outputPath);

                    } catch (IOException e) {
                        // 处理链接无效的情况，如输出错误日志
                        e.printStackTrace();
                    }

                } else {
                    // 如果没有斜杠，则无法删除HTML部分
                    System.out.println("无法删除HTML部分，URL格式不正确。");
                }
            }


            // 将数据添加到进去  并加入dataList

            ldjsDao.setId(7);
            ldjsDao.setName("领导简介");
            ldjsDao.setBid(bid);
            ldjsDao.setBname(category);
            ldjsDao.setBurl(baseUrl);
            ldjsDao.setBzhiwu(leaderDuty);
            ldjsDao.setCimgurl(leaderImageLink);
            ldjsDao.setNeirong(articleContent);
            dataList.add(ldjsDao);
            return bid;
    }
}
