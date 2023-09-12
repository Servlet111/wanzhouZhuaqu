package com.zhua.scraper;

import com.zhua.dao.GovPubDao;
import com.zhua.dao.YqfcDao;
import com.zhua.dao.ZzjgDao;
import com.zhua.service.GovPubService;
import com.zhua.service.YqfcService;
import com.zhua.service.ZzjgService;
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
public class ZzjgSc {
    private int Cid = 0; // 将Cid声明为成员变量
    @Autowired
    ZzjgService zzjgService;
    public void scrapeZzjg() {
        /* dataList 用于存储抓取到的首页信息，每个首页信息都表示为一个 ShouyeDao 对象，
         然后将这些对象添加到 dataList 中。最后，通过调用 shouyeService.saveBatch(dataList);
         方法，将 dataList 中的数据批量插入到数据库中，以完成数据持久化操作。*/

        String baseUrl = "http://www.wz.gov.cn/jkq/wzjkq/znjg/jgzn/";
        //内设机构 URL
        String nsjgUrl = "http://www.wz.gov.cn/jkq/wzjkq/znjg/jgzn/nsjg/";
        // 事业单位 URL
        String sydwUrl = "http://www.wz.gov.cn/jkq/wzjkq/znjg/jgzn/sydw/";
        // 直属企业 URL
        String zsqyUrl = "http://www.wz.gov.cn/jkq/wzjkq/znjg/jgzn/lxfs/";
        // 联系我们 URL
        String lxwmUrl = "http://www.wz.gov.cn/jkq/wzjkq/znjg/lxwm/";
        List<ZzjgDao> dataList = new ArrayList<>();

        try {
            //内设机构1
            // 连接内设机构的 URL 并解析页面内容
            Document nsjg = Jsoup.parse(new URL(nsjgUrl), 30000);
            String nsjgHtmlContent = nsjg.html();
            // 使用正则表达式匹配createPage函数调用
            Pattern pattern = Pattern.compile("createPage\\((\\d+)");
            Matcher matcher = pattern.matcher(nsjgHtmlContent);
            while (matcher.find()) {
                //是用来表示是企业还是政府govBid
                int infoBid = 1;
                String param1 = matcher.group(1);
                // 将param1字符串转换为整数
                int param1Int = (Integer.parseInt(param1))-1;
                // 构建政府信息的新链接
                for (int i = 0; i <=param1Int; i++) {
                    String nsjgNewUrl;
                    if (i == 0) {
                        nsjgNewUrl = nsjgUrl + "index.html"; // 当 i 为0时使用这个连接
                    } else {
                        nsjgNewUrl = nsjgUrl + "index_" + i + ".html";
                    }
                    /*curretbid用来记数*/
                    extractInformation(nsjgUrl,dataList,nsjgNewUrl, "内设机构", infoBid);
                }

            }
            //事业单位2
            // 连接事业单位 的 URL 并解析页面内容
            Document sydwDoc = Jsoup.parse(new URL(sydwUrl), 30000);
            String sydwHtmlContent = sydwDoc.html();
            // 重新使用正则表达式"pattern"匹配createPage函数调用
            Matcher matcher2 = pattern.matcher(sydwHtmlContent);
            while (matcher2.find()) {
                int sydwBid= 2;
                String param2 = matcher2.group(1);
                int param2Int = (Integer.parseInt(param2))-1;
                // 构建企业信息的新链接
                for (int i = 0; i <=param2Int; i++) {
                    /*corpBid表示是企业信息id*/
                    String sydwNewUrl;
                    if (i == 0) {
                        sydwNewUrl = sydwUrl + "index.html"; // 当 i 为0时使用这个连接
                    } else {
                        sydwNewUrl = sydwUrl + "index_" + i + ".html";
                    }
                    /*currentid用来及数字*/
                    extractInformation(sydwUrl,dataList,sydwNewUrl, "事业单位", sydwBid);
                }

            }
            //直属企业3
            //直属企业 的 URL 并解析页面内容
            Document zsqyDoc = Jsoup.parse(new URL(zsqyUrl), 30000);
            String  zsqyUrlHtmlContent = zsqyDoc.html();
            // 重新使用正则表达式匹配createPage函数调用
            Matcher matcher3 = pattern.matcher(zsqyUrlHtmlContent);
            while (matcher3.find()) {
                int zsqyBid= 3;
                String param2 = matcher3.group(1);
                int param2Int = (Integer.parseInt(param2))-1;
                // 构建企业信息的新链接
                for (int i = 0; i <=param2Int; i++) {
                    /*corpBid表示是企业信息id*/
                    String zsqyNewUrl;
                    if (i == 0) {
                        zsqyNewUrl = zsqyUrl + "index.html"; // 当 i 为0时使用这个连接
                    } else {
                        zsqyNewUrl = zsqyUrl + "index_" + i + ".html";
                    }
                    /*currentid用来及数字*/
                    extractInformation(zsqyUrl,dataList,zsqyNewUrl, "直属企业", zsqyBid);
                }

            }

            //联系我们 4
            // 联系我们 的 URL 并解析页面内容
            Document lxwmDoc = Jsoup.parse(new URL(lxwmUrl), 30000);
            String lxwmHtmlContent = lxwmDoc.html();
            Matcher matcher4 = pattern.matcher(lxwmHtmlContent);
            while (matcher4.find()) {
                int lxwmBid= 4;
                String param2 = matcher4.group(1);
                int param2Int = (Integer.parseInt(param2))-1;
                // 构建企业信息的新链接
                for (int i = 0; i <=param2Int; i++) {
                    /*corpBid表示是企业信息id*/
                    String lxwmNewUrl;
                    if (i == 0) {
                        lxwmNewUrl = lxwmUrl + "index.html"; // 当 i 为0时使用这个连接
                    } else {
                        lxwmNewUrl = lxwmUrl + "index_" + i + ".html";
                    }
                    /*currentid用来及数字*/
                    extractInformation(lxwmUrl,dataList,lxwmNewUrl, "联系我们", lxwmBid);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        zzjgService.saveBatch(dataList);
        System.out.println("数据插入成功！");
    }
    private int extractInformation(String baseUrl, List<ZzjgDao> dataList, String NewUrl, String category, int bid) throws IOException {
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
                Elements ccontentElements = articleDoc.select("div.view.TRS_UEDITOR.trs_paper_default.trs_web p, div.view.TRS_UEDITOR.trs_paper_default.trs_word p, div.zwxl-article div.view TRS_UEDITOR.trs_paper_default.trs_word p,div.view.TRS_UEDITOR.trs_paper_default.trs_external p,div.view.TRS_UEDITOR.trs_paper_default.trs_web table");
                // 使用 StringBuilder 构建文本
                StringBuilder ccontentBuilder = new StringBuilder();
                for (Element contentElement : ccontentElements) {
                    ccontentBuilder.append(contentElement.text()).append("\n");
                }
                // 最后添加一个换行符，以确保最后一行后也有一个换行符
                ccontentBuilder.append("\n");
                // 获取提取的正文内容
                articleContent = ccontentBuilder.toString().trim();
            }

            // 将数据添加到进去  并加入dataList
            ZzjgDao zzjgDao = new ZzjgDao();
            zzjgDao.setId(5);
            zzjgDao.setName("组织机构");
            zzjgDao.setBid(bid);
            zzjgDao.setBname(category);
            zzjgDao.setCid(Cid);
            zzjgDao.setCname(title);
            zzjgDao.setCurl(fullLink);
            zzjgDao.setDtime(date);
            zzjgDao.setDname(title);
            zzjgDao.setDyaun(csource);
            zzjgDao.setNeirong(articleContent);
            dataList.add(zzjgDao);
        }

        return bid;
    }
}
