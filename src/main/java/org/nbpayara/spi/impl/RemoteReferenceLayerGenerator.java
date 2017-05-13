/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbpayara.spi.impl;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.StringUtils;
import org.nbpayara.spi.RemoteLookup;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes({"org.thespheres.betula.beans.services.RemoteLookup.Registration",
    "org.thespheres.betula.beans.services.RemoteLookup.Registrations"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RemoteReferenceLayerGenerator extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(RemoteLookup.Registration.class)) {
            RemoteLookup.Registration r = e.getAnnotation(RemoteLookup.Registration.class);
            writeOne(r, e);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(RemoteLookup.Registrations.class)) {
            RemoteLookup.Registrations rs = e.getAnnotation(RemoteLookup.Registrations.class);
            for (RemoteLookup.Registration r : rs.value()) {
                writeOne(r, e);
            }
        }
        return true;
    }

    protected void writeOne(RemoteLookup.Registration r, Element e) throws LayerGenerationException, IllegalArgumentException {
        String name = r.name();
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        String module = r.module();
        if (StringUtils.isBlank(module)) {
            module = null;
        }
        TypeMirror type = null;
        try {
            r.beanInterface(); // this should throw
        } catch (MirroredTypeException mte) {
            if (mte != null) {
                type = mte.getTypeMirror();
            }
        }
        if (type == null) {
            throw new LayerGenerationException(null, e);
        }
        Element beanType = processingEnv.getTypeUtils().asElement(type);
        String beanClazz = processingEnv.getElementUtils().getBinaryName((TypeElement) beanType).toString();
        final String path = "RemoteLookup";
        String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
        String basename = clazz.replace('.', '-');
        String file = path + "/" + basename + ".instance";
        layer(e).file(file)
                .methodvalue("instanceCreate", "org.thespheres.betula.beans.services.layergen.RemoteReference", "create")
                .stringvalue("ejbName", name)
                .stringvalue("beanInterface", beanClazz) //type.toString())
                .stringvalue("module", module)
                .write();
//        layer(e).instanceFile("RemoteLookup", null, null, null)
//                .methodvalue("instanceCreate", "org.thespheres.betula.beans.services.layergen.RemoteReference", "create")
//                .stringvalue("ejbName", name)
//                .stringvalue("type", type.toString())
//                .stringvalue("module", module)
//                .write();
    }

}
