///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVAC_OPTIONS --enable-preview -source 16
//JAVA_OPTIONS --enable-preview
//DEPS com.github.lalyos:jfiglet:0.0.8

package cloud.caravana.anonymouse.tdc;
//import com.github.lalyos.jfiglet.FigletFont;

public class NPEUtil {
    static class World{
        String user = "";
        public String hello(){
            return "hello " + user.toLowerCase();
        }
    }

    public static void main(String[] args) throws Exception{
        var world = new World();
        var msg = world.hello();
        //System.out.println(FigletFont.convertOneLine(msg));
    }
}
