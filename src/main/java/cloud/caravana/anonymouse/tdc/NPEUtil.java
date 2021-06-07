package cloud.caravana.anonymouse.tdc;

public class NPEUtil {
    static class World{
        String user;
        public String hello(){
            return "hello " + user.toLowerCase();
        }
    }

    public static void main(String[] args) {
        World world = new World();
        System.out.println(world.hello());
    }
}
