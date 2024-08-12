package pl.kdrozd.examples.annotationprocessor;

import com.google.auto.service.AutoService;
import org.bukkit.Bukkit;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@SupportedAnnotationTypes({
        "pl.kdrozd.examples.annotationprocessor.Handler"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class DescriptorProcessor extends AbstractProcessor {

    private Messager what;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        what = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> annotatedHandlers = roundEnvironment.getElementsAnnotatedWith(Handler.class);

        annotatedHandlers.forEach(element -> {
            System.out.println("Processing " + element);
            try {
                element.getClass().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            what.printMessage(Diagnostic.Kind.NOTE, "Found handler: " + element);
        });

        return false;
    }
}