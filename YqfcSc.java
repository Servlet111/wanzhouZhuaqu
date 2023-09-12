package com.zhua.scraper;

import com.zhua.dao.GovPubDao;
import com.zhua.dao.YqfcDao;
import com.zhua.service.YqfcService;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Controller
@Component
public class YqfcSc {
    private int Cid = 0; // 将Cid声明为成员变量
    @Autowired
    private YqfcService yqfcService;

    public void scrapeYqfc() {
        /* dataList 用于存储抓取到的首页信息，每个首页信息都表示为一个 ShouyeDao 对象，
         然后将这些对象添加到 dataList 中。最后，通过调用 shouyeService.saveBatch(dataList);
         方法，将 dataList 中的数据批量插入到数据库中，以完成数据持久化操作。*/
        String baseUrl = "http://www.wz.gov.cn/jkq/wzjkq/yqfc";
        //图说经开URL
        String tsjkUrl = "http://www.wz.gov.cn/jkq/wzjkq/yqfc/tsjk/";
        // 图说企业 URL
        String tsqyUrl = "http://www.wz.gov.cn/jkq/wzjkq/yqfc/tsqy/";
        List<YqfcDao> dataList = new ArrayList<>();
        try {
            //图说经开
            // 连接图说经开的 URL 并解析页面内容
            Document tsjkDoc = Jsoup.parse(new URL(tsjkUrl), 30000);
            String tsjkPubHtmlContent = tsjkDoc.html();
            // 使用正则表达式匹配createPage这个函数函数、找到他后面的数字
            Pattern pattern = Pattern.compile("createPage\\((\\d+)");
            Matcher matcher = pattern.matcher(tsjkPubHtmlContent);
            while (matcher.find()) {
                //tsjkBid表示是图说经开还是图说企业
                int tsjkBid = 1;
                //找到createPage后面的第一组数字
                String param1 = matcher.group(1);
                // 将param1字符串转换为整数
                int param1Int = (Integer.parseInt(param1))-1;
                // 构建政府信息的新链接
                for (int i = 0; i <=param1Int; i++) {
                    String tsjkNewUrl;
                    if (i == 0) {
                        tsjkNewUrl = tsjkUrl + "index.html"; // 当 i 为0时使用这个连接
                    } else {
                        tsjkNewUrl = tsjkUrl + "index_" + i + ".html";
                    }
                    /*curretbid用来记数*/
                    extractInformation(tsjkUrl,dataList,tsjkNewUrl, "信息公开", tsjkBid);
                }
            }
            //图说企业2
            // 连接政策信息 的 URL 并解析页面内容
            Document tsqyDoc = Jsoup.parse(new URL(tsqyUrl), 30000);
            //获取HTml内容放在tsqyHtmlContent
            String tsqyHtmlContent = tsqyDoc.html();
            // 重新使用正则表达式匹配createPage函数调用
            Matcher matcher2 = pattern.matcher(tsqyHtmlContent);
            while (matcher2.find()) {
                int tsqyBid= 2;
                //获取最大页码
                String param2 = matcher2.group(1);
                int param2Int = (Integer.parseInt(param2))-1;
                // 构建企业信息的新链接
                for (int i = 0; i <=param2Int; i++) {
                    /*corpBid表示是企业信息id*/
                    String tsqyNewUrl;
                    if (i == 0) {
                        tsqyNewUrl = tsqyUrl + "index.html"; // 当 i 为0时使用这个连接
                    } else {
                        tsqyNewUrl = tsqyUrl + "index_" + i + ".html";
                    }
                    /*currentid用来及数字*/
                    extractInformation(tsqyUrl,dataList,tsqyNewUrl, "政策信息", tsqyBid);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        yqfcService.saveBatch(dataList);
        System.out.println("数据插入成功！");
    }

    private int extractInformation(String baseUrl, List<YqfcDao> dataList, String NewUrl, String category, int bid) throws IOException {
        YqfcDao yqfcDao = new YqfcDao();
        Document doc = Jsoup.connect(NewUrl).get();
        //抓取下面的信息也就是cname
        Elements newsItems = doc.select("ul.right-list li.clearfix");
        // 获取右侧标题
        String rightTitle = doc.select("div.right p.right-title").text();
        for (Element newsItem : newsItems) {
            /*for 循环中的 newsItem 变量是用来代表 newsItems 中的每个元素，
            使得可以在循环体内对每个新闻项进行操作，比如提取新闻内容或执行其他相关操作。
            这个循环的作用是对多个新闻项进行相同或类似的处理，以便提取和处理网页中的多个新闻条目*/

            Cid++; // 递增成员变量Cid的值

            String title = newsItem.select("a.lf").text();
            String date = newsItem.select("span.rt").text();
            String relativeLink = newsItem.select("a.lf").attr("href");
            String bname = newsItem.select("p.right-title").text();
            // 拼接完整连接
            String fullLink = baseUrl+relativeLink;
            String articleContent="";
            String ctitle="";
            String csource = "";
            if (relativeLink.startsWith("https://") || relativeLink.startsWith("http://")) {
                articleContent = "第三方连接404!!: " + relativeLink;
                // 这里可以将提示信息存起来备用，或者执行其他逻辑
                // 如果不需要下载，可以使用continue跳过当前循环
                // 设置其他参数为默认值或其他适当的值
                ctitle = "未知标题";
                csource = "未知来源";
                date = "未知来源！！";
                // 其他参数的初始化赋值，根据实际情况设置

                continue;


            } else {
                // 如果不以"https://"开头，进行拼接
                fullLink = baseUrl + relativeLink;
                fullLink = fullLink.replace("/./", "/");
                // 在这里可以执行其他需要的操作，如正常下载
            }

            // 在这里添加代码来检查连接是否存在
            int responseCode = -1; // 默认值，表示未知状态码
            try {
                responseCode = Jsoup.connect(fullLink).execute().statusCode();
            } catch (IOException e) {
                // 处理网络连接异常
                e.printStackTrace();
            }

            // 处理状态码
            if (responseCode == -1) {
                // 未知状态码，可能是由于异常引起的
                System.err.println("无法获取状态码，连接可能存在问题：" + fullLink);
            } else if (responseCode == 404) {
                // 处理连接不存在的情况
                articleContent = "连接不存在 (HTTP 404): " + fullLink;
                ctitle = "未知标题";
                csource = "未知来源";
                date = "未知来源！！";
                // 其他参数的初始化赋值，根据实际情况设置
                System.err.println("HTTP错误：" + responseCode + "，连接：" + fullLink);
                continue; // 跳过当前循环迭代
            } else if (responseCode != 200) {
                // 处理其他错误的情况
                System.out.println("HTTP错误：" + responseCode + "，连接：" + fullLink);
                // 可根据需要设置相应的逻辑
                continue; // 跳过当前循环迭代
            } else {
                // 处理状态码为200的情况
                // ...
                // 具体文章的  来提取文章内容
                Document articleDoc = Jsoup.connect(fullLink).get();

                // 提取标题
                ctitle = articleDoc.select("div.zwxl-title p.tit").text();

                // 提取来源
                // 选择包含在<script>标签中的内容
                Element scriptElement = articleDoc.select("div.zwxl-title div.zwxl-bar script").first();

                if (scriptElement != null) {
                    // 获取<script>标签中的 JavaScript 代码
                    String scriptText = scriptElement.html();
                    // 使用Jsoup提取<script>中的内容
                    Document scriptDocument = Jsoup.parse(scriptText);
                    // 选择<script>中的<span class="con">元素
                    Elements conElements = scriptDocument.select("span.con");
                    // 提取第一个<span class="con">元素的文本内容
                    if (!conElements.isEmpty()) {
                        csource = conElements.first().text();
                    }else {
                        csource = "";
                    }
                }else {
                    csource = "";
                }
                //提取正文
                // 使用一个选择器来选择正文内容
                Elements ccontentElements = articleDoc.select("div.view.TRS_UEDITOR.trs_paper_default.trs_web p, div.view.TRS_UEDITOR.trs_paper_default.trs_word p, div.zwxl-article div.view TRS_UEDITOR.trs_paper_default.trs_word p,div.view.TRS_UEDITOR.trs_paper_default.trs_external p,div.zwxl-article span");
                // 使用 StringBuilder 构建文本
                StringBuilder ccontentBuilder = new StringBuilder();
                //多个HTML元素的文本内容合并成一个字符串 每小段中间有空格
                for (Element contentElement : ccontentElements) {
                    ccontentBuilder.append(contentElement.text()).append("\n");
                }
                // 最后添加一个换行符，以确保最后一行后也有一个换行符
                ccontentBuilder.append("\n");
                // 获取提取的正文内容
                articleContent = ccontentBuilder.toString().trim();

                //抓取图片连接
                // 将HTML代码块解析为Jsoup文档
                Document imgDoc = Jsoup.parse(new URL(fullLink), 30000);
                // 选择所有图片元素
                Elements divElements = imgDoc.select("div.view.TRS_UEDITOR.trs_paper_default.trs_web,div.zwxl-article");
                // 遍历图片元素并输出其src属性值
                if (divElements!=null){
                    //在div元素里面选择所有图片元素
                    Elements imElements = divElements.select("img");
                    //遍历图片元素 找出src属性值
                    for (Element imgElement : imElements) {
                        //遍历图片元素 直接输出他的值
                        String imgSrc = imgElement.attr("src");
                        // 拼接新的连接
                        // 找到最后一个斜杠的索引
                        int lastSlashIndex = fullLink.lastIndexOf('/');
                        if (lastSlashIndex != -1) {
                            // 删除最后一个斜杠以及后面的部分
                            String newUrl = fullLink.substring(0, lastSlashIndex);
                            //拼接新的img的Url
                            String newLink = newUrl + imgSrc;
                            newLink = newLink.replace("./", "/");
                            try {
                                URL imageUrl = new URL(newLink);
                                URLConnection connection = imageUrl.openConnection();
                                InputStream inputStream = connection.getInputStream();

                                // 获取原始图片文件名，图片链接的最后一部分就是文件名
                                String[] urlParts = newLink.split("/");
                                String originalFileName = urlParts[urlParts.length - 1];

                                // 构建目标文件路径，替换为您希望保存图片的文件夹路径
                                String outputPath = "D:\\java\\WanZhouImg\\" + originalFileName;

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
                                yqfcDao.setNeirongtp(outputPath);

                            } catch (IOException e) {
                                // 处理链接无效的情况，如输出错误日志
                                e.printStackTrace();
                            }

                        } else {
                            // 如果没有斜杠，则无法删除HTML部分
                            System.out.println("无法删除HTML部分，URL格式不正确。");
                        }
                    }
                }else {
                    continue;
                }
            }

            // 将数据添加到进去  并加入dataList

            yqfcDao.setId(5);
            yqfcDao.setName("园区风采");
            yqfcDao.setBid(bid);
            yqfcDao.setBname(category);
            yqfcDao.setCid(Cid);
            yqfcDao.setCname(title);
            yqfcDao.setCtime(date);
            yqfcDao.setCurl(fullLink);
            yqfcDao.setDname(title);
            yqfcDao.setDyuan(csource);
            yqfcDao.setNeirongwz(articleContent);

            dataList.add(yqfcDao);
            /*System.out.println("模块类别"+category);
            System.out.println("模块id"+bid);
            System.out.println("模块路径"+NewUrl);
            System.out.println("消息id"+Cid);
            System.out.println("消息标题"+ctitle);
            System.out.println("消息来源"+csource);
            System.out.println("消息日期"+date);
            System.out.println("消息连接"+fullLink);
            System.out.println("图片地址"+yqfcDao.getNeirongtp());
            System.out.println("内容文字"+yqfcDao.getNeirongwz());*/

        }

        return bid;
    }
}
