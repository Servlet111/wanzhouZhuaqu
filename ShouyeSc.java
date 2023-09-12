package com.zhua.scraper;

import com.zhua.dao.ShouyeDao;

import com.zhua.mapper.ShouyeMapper;
import com.zhua.service.ShouyeService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
    @Component
    public class ShouyeSc {

        @Autowired

        private ShouyeService shouyeService;

        public void scrapeShouye() {
            /*dataList 用于存储抓取到的首页信息，每个首页信息都表示为一个 ShouyeDao 对象，
            然后将这些对象添加到 dataList 中。最后，通过调用 shouyeService.saveBatch(dataList);
            方法，将 dataList 中的数据批量插入到数据库中，以完成数据持久化操作。*/
            List<ShouyeDao> dataList = new ArrayList<>();

            try {
                String baseUrl = "http://www.wz.gov.cn/jkq/wzjkq/";
                Document doc = Jsoup.parse(new URL(baseUrl), 30000);
                System.out.println(doc);
                // 抓取企业信息板块的信息

                Elements enterpriseInfoElements = doc.select(".main_tab.lf.g-main_tab .tab-item > li");
                System.out.println("企业信息板块信息：");
                int enterpriseBid = 1;
                enterpriseBid = printInfo(enterpriseInfoElements, dataList, 1, "企业信息板块", enterpriseBid);

                // 抓取政务信息板块的信息
                Elements governmentInfoElements = doc.select(".main_tab.rt.g-main_tab .tab-item > li");
                System.out.println("\n政务信息板块信息：");
                int governmentBid = 1;
                governmentBid = printInfo(governmentInfoElements, dataList, 2, "政务信息板块", governmentBid);

                // 抓取信息公开板块的信息
                Elements publicInfoElements = doc.select(".mp1-left-part1 .tab-item > li");
                System.out.println("\n信息公开板块信息：");
                int publicBid = 1;
                publicBid = printInfo(publicInfoElements, dataList, 3, "信息公开板块", publicBid);

                // 抓取政策文件板块的信息
                Elements policyInfoElements = doc.select(".mp1-right .tab-item > li");
                System.out.println("\n政策文件板块信息：");
                int policyBid = 1;
                policyBid = printInfo(policyInfoElements, dataList, 4, "政策文件板块", policyBid);

            } catch (IOException e) {
                e.printStackTrace();
            }

            // 插入数据到数据库
            //shouyeService.saveBatch(dataList);
            System.out.println("数据插入成功！");
        }

        private int printInfo(Elements elements, List<ShouyeDao> dataList, int mid, String mname, int currentBid) {
            for (Element element : elements) {
                String title = element.select("a").attr("title");
                String date = element.select("span").text();
                String url = element.select("a").attr("href");

                ShouyeDao shouyeDao = new ShouyeDao();
                shouyeDao.setId(1);
                shouyeDao.setName("首页");
                shouyeDao.setBname(title);
                shouyeDao.setBtime(date);
                shouyeDao.setBurl(url);
                shouyeDao.setMid(mid);
                shouyeDao.setMname(mname);
                shouyeDao.setBid(currentBid);

                dataList.add(shouyeDao);

                currentBid++;
            }
            return currentBid;
        }
    }


















