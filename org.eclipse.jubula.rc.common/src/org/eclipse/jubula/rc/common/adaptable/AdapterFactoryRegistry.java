/*******************************************************************************
 * Copyright (c) 2004, 2012 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.rc.common.adaptable;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.jubula.rc.common.classloader.DefaultUrlLocator;
import org.eclipse.jubula.rc.common.classloader.IUrlLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton to register adapter factories
 */
public class AdapterFactoryRegistry {
    /** the name of the package to search for adapters */
    public static final String ADAPTER_PACKAGE_NAME = "org.eclipse.jubula.rc.common.adapter"; //$NON-NLS-1$

    /** the name of the package to search for extension adapters */
    private static final String EXT_ADAPTER_PACKAGE_NAME = "org.eclipse.jubula.ext.rc.common.adapter"; //$NON-NLS-1$
    
    /** the logger */
    private static Logger log = LoggerFactory
            .getLogger(AdapterFactoryRegistry.class);

    /**
     * Singleton instance of this class
     */
    private static AdapterFactoryRegistry instance = 
        new AdapterFactoryRegistry();

    /**
     * Map that manages the registration. Key is always a class Value is a
     * collection of IAdapterFactory
     */
    private Map<Class, Collection<IAdapterFactory>> m_registrationMap = 
        new HashMap<Class, Collection<IAdapterFactory>>();

    /**
     * Call Constructor only by using getInstance
     */
    private AdapterFactoryRegistry() {
    }

    /**
     * Return the singleton of this class
     * 
     * @return singleton
     */
    public static AdapterFactoryRegistry getInstance() {
        return instance;
    }

    /**
     * Register adapter factory with all its supported classes
     * 
     * @param factory
     *            adapter factory that should be registered
     */
    public void registerFactory(IAdapterFactory factory) {
        Class[] supportedClasses = factory.getSupportedClasses();
        for (int i = 0; i < supportedClasses.length; i++) {
            Collection<IAdapterFactory> registeredFactories = m_registrationMap
                    .get(supportedClasses[i]);
            if (registeredFactories == null) {
                registeredFactories = new ArrayList<IAdapterFactory>();
            }
            registeredFactories.add(factory);
            m_registrationMap.put(supportedClasses[i], registeredFactories);
        }
    }

    /**
     * Sign off adapter factory from all its supported classes
     * 
     * @param factory
     *            adapter factory that should be signed off
     */
    public void signOffFactory(IAdapterFactory factory) {
        Class[] supportedClasses = factory.getSupportedClasses();
        for (int i = 0; i < supportedClasses.length; i++) {
            final Class supportedClass = supportedClasses[i];
            Collection<IAdapterFactory> registeredFactories = 
                m_registrationMap.get(supportedClass);
            if (registeredFactories == null) {
                return;
            }
            registeredFactories.remove(factory);
            m_registrationMap.remove(supportedClass);
        }
    }

    /**
     * @param targetAdapterClass
     *            Type of the adapter
     * @param objectToAdapt
     *            object that should be adapted
     * @return Returns an adapter for the objectToAdapt of type
     *         targetAdapterClass. The collection of all supported adapter
     *         factories is iterated. The first value that is not null will be
     *         returned. <code>Null</code> will only be returned if no adapter
     *         can be found for the targetAdapterClass, none of the given
     *         factories can handle the objectToAdapt or the objectToAdapt
     *         itself is <code>null</code>.
     */
    public Object getAdapter(Class targetAdapterClass, Object objectToAdapt) {
        if (objectToAdapt == null) {
            return null;
        }
        Collection<IAdapterFactory> registeredFactories = null;
        Class superClass = objectToAdapt.getClass();
        while (registeredFactories == null && superClass != Object.class) {
            registeredFactories = m_registrationMap.get(superClass);
            superClass = superClass.getSuperclass();
        }
        if (registeredFactories == null) {
            return null;
        }
        for (Iterator<IAdapterFactory> iterator = registeredFactories
            .iterator(); iterator.hasNext();) {
            IAdapterFactory adapterFactory = iterator.next();
            Object object = adapterFactory.getAdapter(targetAdapterClass,
                objectToAdapt);

            if (object != null) {
                return object;
            }
        }
        return null;
    }

    /**
     * Use this method in eclipse environments.
     * Must be called to initialize the registration of adapters.
     * @param urlLocator The URL location converter needed in eclipse environments.
     */
    public static void initRegistration(IUrlLocator urlLocator) {
        Class[] adapterFactories = findClassesOfType(urlLocator,
                ADAPTER_PACKAGE_NAME, IAdapterFactory.class);
        Class[] externalAdapterFactories = findClassesOfType(urlLocator,
                EXT_ADAPTER_PACKAGE_NAME, IAdapterFactory.class);
        
        List<Class> allFactories = new ArrayList<Class>(
                Arrays.asList(adapterFactories));
        allFactories.addAll(Arrays.asList(externalAdapterFactories));
        
        // Register all found factories
        for (Class c : allFactories) {
            try {
                IAdapterFactory factory = (IAdapterFactory) c.newInstance();
                getInstance().registerFactory(factory);
            } catch (IllegalAccessException e) {
                log.error(e.getLocalizedMessage(), e);
            } catch (InstantiationException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Use this method outside of eclipse environments. Must be called to
     * initialize the registration of adapters. This method directly
     * calls {@link AdapterFactoryRegistry#initRegistration(IUrlLocator)} with
     * the {@link DefaultUrlLocator}.
     */
    public static void initRegistration() {
        initRegistration(new DefaultUrlLocator());
    }
    
    /**
     * Investigate a package of subclasses of a specific superclass
     * @param urlLocator
     *            The URL location converter needed in eclipse environments.
     * @param packageName
     *            name of the package
     * @param superclass
     *            parent class for found classes
     * @return found classes
     */
    private static Class[] findClassesOfType(IUrlLocator urlLocator,
            String packageName, Class<IAdapterFactory> superclass) {
        try {
            Class[] allClasses = getClasses(urlLocator, packageName);

            List<Class> assignableClasses = new ArrayList<Class>();
            for (int i = 0; i < allClasses.length; i++) {
                if (superclass.isAssignableFrom(allClasses[i]) 
                        && superclass != allClasses[i]) {
                    assignableClasses.add(allClasses[i]);
                }
            }
            return castListToClassArray(assignableClasses);
        } catch (ClassNotFoundException e) {
            return new Class[0];
        } catch (IOException e) {
            return new Class[0];
        }
    }

    /**
     * Cast a list of classes to an array of classes
     * 
     * @param classes
     *            List of classes
     * @return array of classes
     */
    private static Class[] castListToClassArray(List<Class> classes) {
        Class[] arrayClasses = new Class[classes.size()];
        for (int i = 0; i < arrayClasses.length; i++) {
            arrayClasses[i] = classes.get(i);
        }
        return arrayClasses;
    }
    
    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and sub packages.
     * @param urlLocator
     *            The URL location converter needed in eclipse environments.
     * @param packageName
     *            The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(IUrlLocator urlLocator,
            String packageName)
        throws ClassNotFoundException, IOException {
        ClassLoader classLoader = AdapterFactoryRegistry.class.getClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<URL> dirs = new ArrayList<URL>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            try {
                resource = urlLocator.convertUrl(resource);
                dirs.add(resource);
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        List<Class> classes = new ArrayList<Class>();
        for (int i = 0; i < dirs.size(); i++) {
            if (dirs.get(i).toString().startsWith("jar:")) { //$NON-NLS-1$
                classes.addAll(findClassesInJar(dirs.get(i), packageName));
            } else {
                classes.addAll(findClasses(dirs.get(i), packageName));
            }
        }
        return castListToClassArray(classes);
    }


    /**
     * Recursive method used to find all classes in a given directory and
     * subdirectories.
     * 
     * @param directoryUrl
     *            The base directory
     * @param packageName
     *            The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(URL directoryUrl, String packageName)
        throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        File directory = new File(directoryUrl.getFile());
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String fileName = file.getName();
            if (file.isDirectory()) {
                try {
                    classes.addAll(findClasses(file.toURI().toURL(),
                            packageName + '.' + fileName));
                } catch (MalformedURLException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            } else if (fileName.endsWith(".class")) { //$NON-NLS-1$
                classes.add(Class.forName(packageName + '.'
                        + fileName.substring(0, fileName.length() - 6)));
            }
        }
        return classes;
    }

    /**
     * method to find all classes in a given jar
     * 
     * @param resource
     *            The URL to the jar file
     * @param pkgname
     *            The package name for classes found inside the base directory
     * @return The classes
     */
    private static List<Class> findClassesInJar(URL resource, String pkgname) {
        String relPath = pkgname.replace('.', '/');
        String path = resource.getPath()
                .replaceFirst("[.]jar[!].*", ".jar") //$NON-NLS-1$ //$NON-NLS-2$
                .replaceFirst("file:", ""); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            path = URLDecoder.decode(path, "utf-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException uee) {
            log.error(uee.getLocalizedMessage(), uee);
        }
        List<Class> classes = new ArrayList<Class>();
        JarFile jarFile = null;
        try {            
            jarFile = new JarFile(path);        
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                String className = null;
                if (entryName.endsWith(".class")  //$NON-NLS-1$
                        && entryName.startsWith(relPath)) {
                    className = entryName.replace('/', '.').replace('\\', '.')
                            .replaceAll(".class", ""); //$NON-NLS-1$ //$NON-NLS-2$
                
                    if (className != null) {
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException cnfe) {
                            log.error(cnfe.getLocalizedMessage(), cnfe);
                        }
                    }
                }
            }
        } catch (IOException ioe) {            
            log.warn(ioe.getLocalizedMessage(), ioe);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return classes;
    }
}