package ext.opensource.netty.server.example.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

/**
 * @author ben
 * @Title: basic
 * @Description:
 **/

public class ThymeleafTest {
	 
    /**
     * 使用 Thymeleaf 渲染 HTML
     * @param template  HTML模板
     * @param params 参数
     * @return  渲染后的HTML
     */
    public static String render(String template,Map<String,Object> params){
        Context context = new Context();
        context.setVariables(params);
        TemplateEngine engine=new TemplateEngine();
        
        StringTemplateResolver  resolver = new StringTemplateResolver();
        engine.setTemplateResolver(resolver);
        return engine.process(template,context);
    }
    
    public static void testA() {
    	String template = "<p th:text='${title}'></p>";
        HashMap<String, Object> map = new HashMap<>(16);
        map.put("title","hello world");
        String render = render(template, map);
        System.out.println("渲染之后的字符串是:"+render);
    }
    
    public static void testB() {
    	 ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
         //模板所在目录，相对于当前classloader的classpath。
         resolver.setPrefix("static/");
        
         ///模板文件后缀
         // resolver.setSuffix(".html");
         // resolver.setCacheable(false);//设置不缓存
         // resolver.setTemplateMode("HTML5");
         
         TemplateEngine engine = new TemplateEngine();
         engine.setTemplateResolver(resolver);
         Context context = new Context();
         context.setVariable("socketurl", "ws");
         System.out.println(engine.process("websocket.html", context)); 
    }
    
    public static void testc() {
    	HttpResourceThymeleaf aa = new HttpResourceThymeleaf();
    	aa.setRootDir("static/");
    	aa.buildWebSocketRes("websocket.html", null);
    }
    
    public static void main(String[] args) throws IOException {   
    	testA();
    	//testB();
    	//testc();
    }
}