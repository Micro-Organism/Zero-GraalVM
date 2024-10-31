package com.zero.graalvm.js;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.script.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ZeroGraalvmJsApplicationTests {

    @Test
    public void print() throws Exception{
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        engine.eval("print('hello word!!')");
    }

    @Test
    public void obj() throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        StringBuffer script = new StringBuffer();
        script.append("var obj = new Object();");
        script.append("obj.hello = function(name){print('hello, '+name);}");
        //执行这段script脚本
        engine.eval(script.toString());
        // javax.script.Invocable 是一个可选的接口
        // 检查你的script engine 接口是否已实现!
        // 注意：JavaScript engine实现了Invocable接口
        Invocable inv = (Invocable) engine;
        // 获取我们想调用那个方法所属的js对象
        Object obj = engine.get("obj");
        // 执行obj对象的名为hello的方法
        inv.invokeMethod(obj, "hello", "Script Method !!" );
    }

    @Test
    public void file() throws Exception{
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        engine.eval(new java.io.FileReader(new File("D:\\IdeaProjects\\ETFramework\\graalvm-js\\src\\main\\resources\\static\\test.js")));
        Invocable inv = (Invocable) engine;
        Object obj = engine.get("obj");
        inv.invokeMethod(obj, "name", "知道了" );
    }

    /**
     * 脚本变量
     * @throws Exception
     */
    @Test
    public void scriptVar() throws Exception{

        File file = new File("D:\\IdeaProjects\\ETFramework\\graalvm-js\\src\\main\\resources\\static\\test.txt");
        //将File对象f直接注入到js脚本中并可以作为全局变量使用
        Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .allowIO(true)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                .build();

        context.getPolyglotBindings().putMember("name", file);
        context.eval("js", "var name = Polyglot.import('name');");
        context.eval("js", "console.log(name.getName())");

    }

    /**
     *  使用Script实现java接口
     * @throws Exception
     */
    public void runnableImpl() throws Exception{
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        // String里定义一段JavaScript代码脚本
        String script = "function run() { print('run called'); }";
        // 执行这个脚本
        engine.eval(script);

        // 从脚本引擎中获取Runnable接口对象（实例）. 该接口方法由具有相匹配名称的脚本函数实现。
        Invocable inv = (Invocable) engine;
        // 在上面的脚本中，我们已经实现了Runnable接口的run()方法
        Runnable runnable = inv.getInterface(Runnable.class);

        // 启动一个线程运行上面的实现了runnable接口的script脚本
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * 脚本引擎的多个scope
     * @throws Exception
     */
    @Test
    public void multiScopes() throws Exception{
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        // 打印全局变量 "x"
        engine.put("x", "hello word!!");
        engine.eval("print(x);");
        // 上面的代码会打印"hello word！！"

        // 现在，传入另一个不同的script context
        ScriptContext context = new SimpleScriptContext();
        //新的Script context绑定ScriptContext的ENGINE_SCOPE
        Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);

        // 增加一个新变脸到新的范围 engineScope 中
        bindings.put("x", "word hello!!");
        // 执行同一个脚本 - 但这次传入一个不同的script context
        engine.eval("print(x);", bindings);
        engine.eval("print(x);");
    }

    @Test
    void testJavaScriptFunction() throws Exception {
        runnableImpl();
        List list = new ArrayList();
        list.add("1");
        list.add("1");
        list.add("1");
        for (Object object : list) {
            System.out.println(object);
        }
    }

    @Test
    public  void test10(){
        // 创建JavaScript引擎
        ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("js");
//        ScriptEngine jsEngine = new ScriptEngineManager().getEngineByExtension("js");
//        ScriptEngine jsEngine = new ScriptEngineManager().getEngineByMimeType("text/javascript");
        // 方式一，默认设置变量
        jsEngine.put("hello", "jack");
        // 方式二，使用binding设置变量
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("hello","world");
        jsEngine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        // 方式三，使用Context设置变量
        ScriptContext scriptContext = jsEngine.getContext();
        scriptContext.setAttribute("hello", "polo", ScriptContext.ENGINE_SCOPE);
        try {
            // 方式一，直接调用
            jsEngine.eval("print(hello)");
            // 方式二，编译调用(需要重复调用，建议先编译后调用)
            if (jsEngine instanceof Compilable){
                CompiledScript compileScript = ((Compilable) jsEngine).compile("print(hello)");
                if (compileScript != null){
                    for (int i = 0; i < 10; i++) {
                        compileScript.eval();
                    }
                }
            }
            Invocable invocable = ((Invocable)jsEngine);
            // 方式三，使用JavaScript中的顶层方法
            jsEngine.eval("function greet(name) { print('Hello, ' + name); } ");
            invocable.invokeFunction("greet", "tom");
            // 方式四，使用某个对象的方法
            jsEngine.eval("var obj = { getGreeting: function(name){ return 'hello, ' + name; } } ");
            Object obj = jsEngine.get("obj");
            Object result = invocable.invokeMethod(obj, "getGreeting", "kitty");
            System.out.println(result);

        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testRemoteJS() throws IOException, ScriptException {
        URL jsUrl = new URL("http://127.0.0.1:8088/test.js"); // js文件的URL
        URLConnection connection = jsUrl.openConnection();
        InputStream inputStream = connection.getInputStream(); // 获取js文件的流

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n"); // 将js文件的内容存入StringBuilder
        }
        reader.close();
        inputStream.close();

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("nashorn"); // 获取Nashorn引擎
        String script = sb.toString(); // js文件的内容
        engine.eval(script); // 运行js文件

        Object result = engine.eval("hello()"); // 调用js文件中名为"hello"的函数
        System.out.println(result); // 输出结果
    }
}
