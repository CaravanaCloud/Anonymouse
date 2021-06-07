package cloud.caravana.anonymouse.tdc;


import java.lang.annotation.Documented;

@Documented
public @interface StolenFrom {
    String value() default "";
    String link() default "";

}
