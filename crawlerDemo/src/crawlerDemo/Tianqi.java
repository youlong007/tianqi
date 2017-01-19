package crawlerDemo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class Tianqi {

	static String url = "http://www.tianqi.com/air/beijing.html";
	public static DB db = new DB();
	
	public static void main(String[] args) throws Exception{
        processPage("����",processTime(),"http://www.tianqi.com/",url);
	}
	//��ѯ����������ֵ
	public static void processPage(String mon,long updatetime,String source,String url) throws Exception{
        //���һ���Ƿ������URL�Ѿ������ݿ���
		Timestamp time=new Timestamp(updatetime);  //����ת��ʱ��
        String sql = "select * from outdoor_data_records AS da INNER JOIN outdoor_stations_info AS st ON da.station_id = st.id "+
                     "WHERE st.`name` = '"+mon+"' and da.update_time='"+time+"' and da.source='"+source+"'";
        ResultSet rs = db.runSql(sql);
        if(rs.next()){
             System.out.println("�����Ѿ�����");
        }else{
            //�õ����õ���Ϣ           
            Document document = Jsoup.parse(new URL(url), 20000);     
            Elements html0 = document.select("script");                   //��ȡAQI����
            String data = html0.html();
            int index =data.indexOf("flashvalue");
            int index2=data.indexOf("var", index);
            String data2=data.substring(index,index2);
            String a[] =data2.split(";"); 
            int aqis =a[24].trim().indexOf("value='");
            int aqie=a[24].trim().indexOf("'", aqis+7);
            String aqi=a[24].trim().substring(aqis+7,aqie);
            Elements html = document.select("div[class=txt01] ul li");    //��ȡ��������
            String  mon1 =html.text();
            //�ַ�����֣���ȡĿ������
            String a1[]=mon1.split(" ");
            int p25 =a1[0].indexOf("��");              //��ȡpm25��ֵ
            String pm25=a1[0].substring(p25+1);
            int p10b =a1[4].indexOf("��");
            int p10e =a1[4].indexOf("��g/m3");
            String pm10=a1[4].substring(p10b+1,p10e); //��ȡpm10��ֵ
            int so2b =a1[1].indexOf("��");
            int so2e =a1[1].indexOf("��g/m3");
            String so2=a1[1].substring(so2b+1,so2e);  //��ȡso2��ֵ
            int no2b =a1[5].indexOf("��");
            int no2e =a1[5].indexOf("��g/m3");
            String no2=a1[5].substring(no2b+1,no2e);  //��ȡno2��ֵ
//            int o =a1[6].indexOf("��");
//            String o3 =a1[6].substring(o+1);
            processPage(mon,aqi,pm25,pm10,so2,no2,"","","",updatetime,source,url);
            System.out.println("close");
        }
     }
	//��ҳ��ȡ��ϸ����
	public static void processPage(String mon,String aqi,String pm25,String pm10,String so2,String no2,String o3,
			String co,String major,long updatetime,String source,String url) throws Exception{
        //���һ���Ƿ������URL�Ѿ������ݿ���
		Timestamp time=new Timestamp(updatetime);  //����ת��ʱ��
        String sql = "select * from outdoor_data_records AS da INNER JOIN outdoor_stations_info AS st ON da.station_id = st.id "+
                     "WHERE st.`name` ='"+mon+"' and da.update_time='"+time+"' and da.source='"+source+"'";
        ResultSet rs = db.runSql(sql);
        String mon1=null;
        if(rs.next()){
        	System.out.println("2");
        }else{
        	//��outdoor_stations_info�е�nameת��id��ֵ
        	sql ="SELECT id FROM outdoor_stations_info WHERE name='"+mon+"'";
        	ResultSet rs1 = db.runSql(sql);
        	rs1.next();
        	int stationId=rs1.getInt(1);
            //��uRL�洢�����ݿ��б����´��ظ�
            sql = "INSERT INTO outdoor_data_records (station_id,aqi_china,pm25,pm10,so2,no2,o3,co,major_plt,update_time,source)"+
                  "VALUE (?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            stmt.setInt(1,stationId);
            stmt.setFloat(2, Float.parseFloat(aqi));
            stmt.setFloat(3, Float.parseFloat(pm25));
            stmt.setFloat(4, Float.parseFloat(pm10));
            stmt.setFloat(5, Float.parseFloat(so2));
            stmt.setFloat(6, Float.parseFloat(no2));
            if("".equals(o3)||"��������".equals(o3)){           //��o3����Ϊ�������������ݡ�ʱת��-1
            	stmt.setFloat(7,-1);
            }else{
            	stmt.setFloat(7, Float.parseFloat(o3));
            }
            if("".equals(co)||"��������".equals(co)){           //��co����Ϊ�������������ݡ�ʱת��-1
            	stmt.setFloat(8,-1);
            }else{
            	stmt.setFloat(8, Float.parseFloat(co));
            }
            stmt.setString(9,major);
            stmt.setTimestamp(10,time);
            stmt.setString(11,source);
            stmt.execute();
            //�õ����õ���Ϣ����ҳѭ��
            Document document = Jsoup.parse(new URL(url), 20000);     
            Elements html1 = document.select("table[class=air_tab01] tr");
            for(Element link: html1){
            	Elements html2=link.select("td a");
            	url=html2.attr("abs:href");
            	if(link.text().contains("���վ��")){
            		continue;
            	}else{
            		mon1 =link.text(); 
                    //�ַ�����֣���ȡĿ������
                	String a1[]=mon1.split(" ");
                	processPage(a1[0],a1[5],source,url); 
            	}                   	
            }           	
        }       
     }
	//��ҳ��ȡ��ϸ����
	public static void processPage(String mon,String major,String source,String url) throws Exception{
		Document document = Jsoup.parse(new URL(url), 20000);
		
		//��ȡʱ������
		Elements html0 = document.select("div[class=toptxt] h4");
		String date=html0.text();
		String a[]=date.split("��");
		String y =a[1].substring(0,4);
		String m =a[1].substring(5,7);
		String d =a[1].substring(8,10);
		String h =a[1].substring(11,13);
		String time =y+m+d+h+"0000";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm"); 
        long mSec = sdf.parse(time).getTime();
        //���һ���Ƿ������URL�Ѿ������ݿ���
		Timestamp datatime=new Timestamp(mSec);  //����ת��ʱ��
        String sql = "select * from outdoor_data_records AS da INNER JOIN outdoor_stations_info AS st ON da.station_id = st.id "+
                     "WHERE st.`name` ='"+mon+"' and da.update_time='"+time+"' and da.source='"+source+"'";
        ResultSet rs = db.runSql(sql);
        String mon1=null;
        if(rs.next()){
        	
        }else{
        	//��outdoor_stations_info�е�nameת��id��ֵ
        	sql ="SELECT id FROM outdoor_stations_info WHERE name='"+mon+"'";
        	ResultSet rs1 = db.runSql(sql);
        	rs1.next();
        	int stationId=rs1.getInt(1);       	
        	//�õ����õ���Ϣ  
            Elements html = document.select("script");
            String data =html.html();
            String b[]=data.split("function");
            String c1[] =b[7].split("flashvalue");                //��ȡscript��aqi������
            int ds1=c1[24].trim().indexOf("value='");
            int de1=c1[24].trim().indexOf("'",ds1+8);
            String aqi=c1[24].substring(ds1+8, de1+1);
            String c2[] =b[8].split("flashvalue");                //��ȡscript��pm25������
            int ds2=c2[24].trim().indexOf("value='");
            int de2=c2[24].trim().indexOf("'",ds2+8);
            String pm25=c2[24].substring(ds2+8, de2+1);
            String c3[] =b[9].split("flashvalue");                //��ȡscript��pm10������
            int ds3=c3[24].trim().indexOf("value='");
            int de3=c3[24].trim().indexOf("'",ds3+8);
            String pm10=c3[24].substring(ds3+8, de3+1);
            String c4[] =b[10].split("flashvalue");                //��ȡscript��co������
            int ds4=c4[24].trim().indexOf("value='");
            int de4=c4[24].trim().indexOf("'",ds4+8);
            String co=c4[24].substring(ds4+8, de4+1);
            String c5[] =b[11].split("flashvalue");                //��ȡscript��so2������
            int ds5=c5[24].trim().indexOf("value='");
            int de5=c5[24].trim().indexOf("'",ds5+8);
            String so2=c5[24].substring(ds5+8, de5+1);
            String c6[] =b[12].split("flashvalue");                //��ȡscript��no2������
            int ds6=c6[24].trim().indexOf("value='");
            int de6=c6[24].trim().indexOf("'",ds6+8);
            String no2=c6[24].substring(ds6+8, de6+1);
            String c7[] =b[13].split("flashvalue");                //��ȡscript��o3������
            int ds7=c7[24].trim().indexOf("value='");
            int de7=c7[24].trim().indexOf("'",ds7+8);
            String o3=c7[24].substring(ds7+8, de7+1);
//            System.out.println(aqi+","+pm25+","+pm10+","+co+","+so2+","+no2+","+o3);
            //��uRL�洢�����ݿ���
            sql = "INSERT INTO outdoor_data_records (station_id,aqi_china,pm25,pm10,so2,no2,o3,co,major_plt,update_time,source)"+
                  "VALUE (?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement stmt = db.conn.prepareStatement(sql);
            stmt.setInt(1,stationId);
            stmt.setFloat(2, Float.parseFloat(aqi));
            stmt.setFloat(3, Float.parseFloat(pm25));
            stmt.setFloat(4, Float.parseFloat(pm10));
            stmt.setFloat(5, Float.parseFloat(so2));
            stmt.setFloat(6, Float.parseFloat(no2));
            stmt.setFloat(7, Float.parseFloat(o3));
            stmt.setFloat(8, Float.parseFloat(co));
            stmt.setString(9,major);
            stmt.setTimestamp(10,datatime);
            stmt.setString(11,source);
            stmt.execute();          	
        }       
     }
	//������ڣ���ת���ɺ���(������ҳ)
	public static long processTime() throws MalformedURLException, IOException, ParseException{
            Document document = Jsoup.parse(new URL(url), 20000);
            Elements html = document.select("script");
            String data = html.html();
            int index =data.indexOf("flashvalue +=");
            int index2=data.indexOf("var", index);
            String data2=data.substring(index,index2);
            String a[] =data2.split(";");                //��ȡscript�е�����
            int d1=a[23].trim().indexOf("��");
            int h1=a[23].trim().indexOf("ʱ");
            String d=a[23].trim().substring(26,d1);      //��ȡ���ա�����
            String h=a[23].trim().substring(d1+1,h1);    //��ȡ��ʱ������

            Elements html1 = document.select("ul[class=news01]");    //��ȡ�ꡢ������
            String data3 =html1.last().text();
            int y1 =data3.indexOf("��");
            int m1 =data3.indexOf("��");
            String y =data3.substring(3, y1);
            String m =data3.substring(y1+1, m1);
            if(m.length()<2){                            //1��ӦΪ01��
            	m="0"+m;
            }
            String time=y+m+d+h+"00";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm"); 
            long millionSeconds = sdf.parse(time).getTime();    
            return millionSeconds;           
     }
}
