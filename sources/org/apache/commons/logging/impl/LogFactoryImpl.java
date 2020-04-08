package org.apache.commons.logging.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

public class LogFactoryImpl extends LogFactory {
    public static final String ALLOW_FLAWED_CONTEXT_PROPERTY = "org.apache.commons.logging.Log.allowFlawedContext";
    public static final String ALLOW_FLAWED_DISCOVERY_PROPERTY = "org.apache.commons.logging.Log.allowFlawedDiscovery";
    public static final String ALLOW_FLAWED_HIERARCHY_PROPERTY = "org.apache.commons.logging.Log.allowFlawedHierarchy";
    private static final String LOGGING_IMPL_JDK14_LOGGER = "org.apache.commons.logging.impl.Jdk14Logger";
    private static final String LOGGING_IMPL_LOG4J_LOGGER = "org.apache.commons.logging.impl.Log4JLogger";
    private static final String LOGGING_IMPL_LUMBERJACK_LOGGER = "org.apache.commons.logging.impl.Jdk13LumberjackLogger";
    private static final String LOGGING_IMPL_SIMPLE_LOGGER = "org.apache.commons.logging.impl.SimpleLog";
    public static final String LOG_PROPERTY = "org.apache.commons.logging.Log";
    protected static final String LOG_PROPERTY_OLD = "org.apache.commons.logging.log";
    private static final String PKG_IMPL = "org.apache.commons.logging.impl.";
    private static final int PKG_LEN = 32;
    static Class class$java$lang$String = null;
    static Class class$org$apache$commons$logging$Log = null;
    static Class class$org$apache$commons$logging$LogFactory = null;
    static Class class$org$apache$commons$logging$impl$LogFactoryImpl = null;
    private static final String[] classesToDiscover = {LOGGING_IMPL_LOG4J_LOGGER, LOGGING_IMPL_JDK14_LOGGER, LOGGING_IMPL_LUMBERJACK_LOGGER, LOGGING_IMPL_SIMPLE_LOGGER};
    private boolean allowFlawedContext;
    private boolean allowFlawedDiscovery;
    private boolean allowFlawedHierarchy;
    protected Hashtable attributes = new Hashtable();
    private String diagnosticPrefix;
    protected Hashtable instances = new Hashtable();
    private String logClassName;
    protected Constructor logConstructor = null;
    protected Class[] logConstructorSignature;
    protected Method logMethod;
    protected Class[] logMethodSignature;
    private boolean useTCCL = true;

    static ClassLoader access$000() {
        return LogFactory.directGetContextClassLoader();
    }

    static {
    }

    public LogFactoryImpl() {
        Class cls;
        Class cls2;
        Class[] clsArr = new Class[1];
        if (class$java$lang$String == null) {
            cls = class$("java.lang.String");
            class$java$lang$String = cls;
        } else {
            cls = class$java$lang$String;
        }
        clsArr[0] = cls;
        this.logConstructorSignature = clsArr;
        this.logMethod = null;
        Class[] clsArr2 = new Class[1];
        if (class$org$apache$commons$logging$LogFactory == null) {
            cls2 = class$(LogFactory.FACTORY_PROPERTY);
            class$org$apache$commons$logging$LogFactory = cls2;
        } else {
            cls2 = class$org$apache$commons$logging$LogFactory;
        }
        clsArr2[0] = cls2;
        this.logMethodSignature = clsArr2;
        initDiagnostics();
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Instance created.");
        }
    }

    static Class class$(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }

    public Object getAttribute(String str) {
        return this.attributes.get(str);
    }

    public String[] getAttributeNames() {
        Vector vector = new Vector();
        Enumeration keys = this.attributes.keys();
        while (keys.hasMoreElements()) {
            vector.addElement((String) keys.nextElement());
        }
        String[] strArr = new String[vector.size()];
        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = (String) vector.elementAt(i);
        }
        return strArr;
    }

    public Log getInstance(Class cls) {
        return getInstance(cls.getName());
    }

    public Log getInstance(String str) {
        Log log = (Log) this.instances.get(str);
        if (log != null) {
            return log;
        }
        Log newInstance = newInstance(str);
        this.instances.put(str, newInstance);
        return newInstance;
    }

    public void release() {
        logDiagnostic("Releasing all known loggers");
        this.instances.clear();
    }

    public void removeAttribute(String str) {
        this.attributes.remove(str);
    }

    public void setAttribute(String str, Object obj) {
        if (this.logConstructor != null) {
            logDiagnostic("setAttribute: call too late; configuration already performed.");
        }
        if (obj == null) {
            this.attributes.remove(str);
        } else {
            this.attributes.put(str, obj);
        }
        if (str.equals(LogFactory.TCCL_KEY)) {
            this.useTCCL = Boolean.valueOf(obj.toString()).booleanValue();
        }
    }

    protected static ClassLoader getContextClassLoader() {
        return LogFactory.getContextClassLoader();
    }

    protected static boolean isDiagnosticsEnabled() {
        return LogFactory.isDiagnosticsEnabled();
    }

    protected static ClassLoader getClassLoader(Class cls) {
        return LogFactory.getClassLoader(cls);
    }

    private void initDiagnostics() {
        String str;
        ClassLoader classLoader = getClassLoader(getClass());
        if (classLoader == null) {
            str = "BOOTLOADER";
        } else {
            try {
                str = LogFactory.objectId(classLoader);
            } catch (SecurityException unused) {
                str = "UNKNOWN";
            }
        }
        this.diagnosticPrefix = new StringBuffer("[LogFactoryImpl@").append(System.identityHashCode(this)).append(" from ").append(str).append("] ").toString();
    }

    /* access modifiers changed from: protected */
    public void logDiagnostic(String str) {
        if (isDiagnosticsEnabled()) {
            LogFactory.logRawDiagnostic(new StringBuffer().append(this.diagnosticPrefix).append(str).toString());
        }
    }

    /* access modifiers changed from: protected */
    public String getLogClassName() {
        if (this.logClassName == null) {
            discoverLogImplementation(getClass().getName());
        }
        return this.logClassName;
    }

    /* access modifiers changed from: protected */
    public Constructor getLogConstructor() {
        if (this.logConstructor == null) {
            discoverLogImplementation(getClass().getName());
        }
        return this.logConstructor;
    }

    /* access modifiers changed from: protected */
    public boolean isJdk13LumberjackAvailable() {
        return isLogLibraryAvailable("Jdk13Lumberjack", LOGGING_IMPL_LUMBERJACK_LOGGER);
    }

    /* access modifiers changed from: protected */
    public boolean isJdk14Available() {
        return isLogLibraryAvailable("Jdk14", LOGGING_IMPL_JDK14_LOGGER);
    }

    /* access modifiers changed from: protected */
    public boolean isLog4JAvailable() {
        return isLogLibraryAvailable("Log4J", LOGGING_IMPL_LOG4J_LOGGER);
    }

    /* access modifiers changed from: protected */
    public Log newInstance(String str) {
        Log log;
        try {
            if (this.logConstructor == null) {
                log = discoverLogImplementation(str);
            } else {
                log = (Log) this.logConstructor.newInstance(new Object[]{str});
            }
            if (this.logMethod != null) {
                this.logMethod.invoke(log, new Object[]{this});
            }
            return log;
        } catch (LogConfigurationException e) {
            throw e;
        } catch (InvocationTargetException e2) {
            Throwable targetException = e2.getTargetException();
            if (targetException != null) {
                throw new LogConfigurationException(targetException);
            }
            throw new LogConfigurationException((Throwable) e2);
        } catch (Throwable th) {
            throw new LogConfigurationException(th);
        }
    }

    private static ClassLoader getContextClassLoaderInternal() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return LogFactoryImpl.access$000();
            }
        });
    }

    private static String getSystemProperty(String str, String str2) {
        return (String) AccessController.doPrivileged(new PrivilegedAction(str, str2) {
            private final String val$def;
            private final String val$key;

            {
                this.val$key = r1;
                this.val$def = r2;
            }

            public Object run() {
                return System.getProperty(this.val$key, this.val$def);
            }
        });
    }

    private ClassLoader getParentClassLoader(ClassLoader classLoader) {
        try {
            return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction(this, classLoader) {
                private final LogFactoryImpl this$0;
                private final ClassLoader val$cl;

                {
                    this.this$0 = r1;
                    this.val$cl = r2;
                }

                public Object run() {
                    return this.val$cl.getParent();
                }
            });
        } catch (SecurityException unused) {
            logDiagnostic("[SECURITY] Unable to obtain parent classloader");
            return null;
        }
    }

    private boolean isLogLibraryAvailable(String str, String str2) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic(new StringBuffer("Checking for '").append(str).append("'.").toString());
        }
        try {
            if (createLogFromClass(str2, getClass().getName(), false) == null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(new StringBuffer("Did not find '").append(str).append("'.").toString());
                }
                return false;
            }
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer("Found '").append(str).append("'.").toString());
            }
            return true;
        } catch (LogConfigurationException unused) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer("Logging system '").append(str).append("' is available but not useable.").toString());
            }
            return false;
        }
    }

    private String getConfigurationValue(String str) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic(new StringBuffer("[ENV] Trying to get configuration for item ").append(str).toString());
        }
        Object attribute = getAttribute(str);
        if (attribute != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer("[ENV] Found LogFactory attribute [").append(attribute).append("] for ").append(str).toString());
            }
            return attribute.toString();
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic(new StringBuffer("[ENV] No LogFactory attribute found for ").append(str).toString());
        }
        try {
            String systemProperty = getSystemProperty(str, null);
            if (systemProperty != null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(new StringBuffer("[ENV] Found system property [").append(systemProperty).append("] for ").append(str).toString());
                }
                return systemProperty;
            }
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer("[ENV] No system property found for property ").append(str).toString());
            }
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer("[ENV] No configuration defined for item ").append(str).toString());
            }
            return null;
        } catch (SecurityException unused) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer("[ENV] Security prevented reading system property ").append(str).toString());
            }
        }
    }

    private boolean getBooleanConfiguration(String str, boolean z) {
        String configurationValue = getConfigurationValue(str);
        if (configurationValue == null) {
            return z;
        }
        return Boolean.valueOf(configurationValue).booleanValue();
    }

    private void initConfiguration() {
        this.allowFlawedContext = getBooleanConfiguration(ALLOW_FLAWED_CONTEXT_PROPERTY, true);
        this.allowFlawedDiscovery = getBooleanConfiguration(ALLOW_FLAWED_DISCOVERY_PROPERTY, true);
        this.allowFlawedHierarchy = getBooleanConfiguration(ALLOW_FLAWED_HIERARCHY_PROPERTY, true);
    }

    private Log discoverLogImplementation(String str) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Discovering a Log implementation...");
        }
        initConfiguration();
        Log log = null;
        String findUserSpecifiedLogClassName = findUserSpecifiedLogClassName();
        if (findUserSpecifiedLogClassName != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic(new StringBuffer("Attempting to load user-specified log class '").append(findUserSpecifiedLogClassName).append("'...").toString());
            }
            Log createLogFromClass = createLogFromClass(findUserSpecifiedLogClassName, str, true);
            if (createLogFromClass != null) {
                return createLogFromClass;
            }
            StringBuffer stringBuffer = new StringBuffer("User-specified log class '");
            stringBuffer.append(findUserSpecifiedLogClassName);
            stringBuffer.append("' cannot be found or is not useable.");
            if (findUserSpecifiedLogClassName != null) {
                informUponSimilarName(stringBuffer, findUserSpecifiedLogClassName, LOGGING_IMPL_LOG4J_LOGGER);
                informUponSimilarName(stringBuffer, findUserSpecifiedLogClassName, LOGGING_IMPL_JDK14_LOGGER);
                informUponSimilarName(stringBuffer, findUserSpecifiedLogClassName, LOGGING_IMPL_LUMBERJACK_LOGGER);
                informUponSimilarName(stringBuffer, findUserSpecifiedLogClassName, LOGGING_IMPL_SIMPLE_LOGGER);
            }
            throw new LogConfigurationException(stringBuffer.toString());
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("No user-specified Log implementation; performing discovery using the standard supported logging implementations...");
        }
        for (int i = 0; i < classesToDiscover.length && log == null; i++) {
            log = createLogFromClass(classesToDiscover[i], str, true);
        }
        if (log != null) {
            return log;
        }
        throw new LogConfigurationException("No suitable Log implementation");
    }

    private void informUponSimilarName(StringBuffer stringBuffer, String str, String str2) {
        if (!str.equals(str2) && str.regionMatches(true, 0, str2, 0, PKG_LEN + 5)) {
            stringBuffer.append(" Did you mean '");
            stringBuffer.append(str2);
            stringBuffer.append("'?");
        }
    }

    private String findUserSpecifiedLogClassName() {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Trying to get log class from attribute 'org.apache.commons.logging.Log'");
        }
        String str = (String) getAttribute(LOG_PROPERTY);
        if (str == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from attribute 'org.apache.commons.logging.log'");
            }
            str = (String) getAttribute(LOG_PROPERTY_OLD);
        }
        if (str == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property 'org.apache.commons.logging.Log'");
            }
            try {
                str = getSystemProperty(LOG_PROPERTY, null);
            } catch (SecurityException e) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(new StringBuffer("No access allowed to system property 'org.apache.commons.logging.Log' - ").append(e.getMessage()).toString());
                }
            }
        }
        if (str == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property 'org.apache.commons.logging.log'");
            }
            try {
                str = getSystemProperty(LOG_PROPERTY_OLD, null);
            } catch (SecurityException e2) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(new StringBuffer("No access allowed to system property 'org.apache.commons.logging.log' - ").append(e2.getMessage()).toString());
                }
            }
        }
        if (str != null) {
            return str.trim();
        }
        return str;
    }

    private Log createLogFromClass(String str, String str2, boolean z) {
        Class cls;
        URL url;
        if (isDiagnosticsEnabled()) {
            logDiagnostic(new StringBuffer("Attempting to instantiate '").append(str).append("'").toString());
        }
        Object[] objArr = {str2};
        Log log = null;
        Constructor constructor = null;
        Class cls2 = null;
        ClassLoader baseClassLoader = getBaseClassLoader();
        while (true) {
            logDiagnostic(new StringBuffer("Trying to load '").append(str).append("' from classloader ").append(LogFactory.objectId(baseClassLoader)).toString());
            try {
                if (isDiagnosticsEnabled()) {
                    String stringBuffer = new StringBuffer().append(str.replace('.', '/')).append(".class").toString();
                    if (baseClassLoader != null) {
                        url = baseClassLoader.getResource(stringBuffer);
                    } else {
                        url = ClassLoader.getSystemResource(new StringBuffer().append(stringBuffer).append(".class").toString());
                    }
                    if (url == null) {
                        logDiagnostic(new StringBuffer("Class '").append(str).append("' [").append(stringBuffer).append("] cannot be found.").toString());
                    } else {
                        logDiagnostic(new StringBuffer("Class '").append(str).append("' was found at '").append(url).append("'").toString());
                    }
                }
                try {
                    cls = Class.forName(str, true, baseClassLoader);
                } catch (ClassNotFoundException e) {
                    logDiagnostic(new StringBuffer("The log adapter '").append(str).append("' is not available via classloader ").append(LogFactory.objectId(baseClassLoader)).append(": ").append(new StringBuffer().append(e.getMessage()).toString().trim()).toString());
                    try {
                        cls = Class.forName(str);
                    } catch (ClassNotFoundException e2) {
                        logDiagnostic(new StringBuffer("The log adapter '").append(str).append("' is not available via the LogFactoryImpl class classloader: ").append(new StringBuffer().append(e2.getMessage()).toString().trim()).toString());
                        break;
                    }
                }
                Constructor constructor2 = cls.getConstructor(this.logConstructorSignature);
                constructor = constructor2;
                Object newInstance = constructor2.newInstance(objArr);
                if (!(newInstance instanceof Log)) {
                    handleFlawedHierarchy(baseClassLoader, cls);
                    if (baseClassLoader == null) {
                        break;
                    }
                    baseClassLoader = getParentClassLoader(baseClassLoader);
                } else {
                    cls2 = cls;
                    log = (Log) newInstance;
                    break;
                }
            } catch (NoClassDefFoundError e3) {
                logDiagnostic(new StringBuffer("The log adapter '").append(str).append("' is missing dependencies when loaded via classloader ").append(LogFactory.objectId(baseClassLoader)).append(": ").append(new StringBuffer().append(e3.getMessage()).toString().trim()).toString());
            } catch (ExceptionInInitializerError e4) {
                logDiagnostic(new StringBuffer("The log adapter '").append(str).append("' is unable to initialize itself when loaded via classloader ").append(LogFactory.objectId(baseClassLoader)).append(": ").append(new StringBuffer().append(e4.getMessage()).toString().trim()).toString());
            } catch (LogConfigurationException e5) {
                throw e5;
            } catch (Throwable th) {
                handleFlawedDiscovery(str, baseClassLoader, th);
            }
        }
        if (log != null && z) {
            this.logClassName = str;
            this.logConstructor = constructor;
            try {
                this.logMethod = cls2.getMethod("setLogFactory", this.logMethodSignature);
                logDiagnostic(new StringBuffer("Found method setLogFactory(LogFactory) in '").append(str).append("'").toString());
            } catch (Throwable unused) {
                this.logMethod = null;
                logDiagnostic(new StringBuffer("[INFO] '").append(str).append("' from classloader ").append(LogFactory.objectId(baseClassLoader)).append(" does not declare optional method setLogFactory(LogFactory)").toString());
            }
            logDiagnostic(new StringBuffer("Log adapter '").append(str).append("' from classloader ").append(LogFactory.objectId(cls2.getClassLoader())).append(" has been selected for use.").toString());
        }
        return log;
    }

    private ClassLoader getBaseClassLoader() {
        Class cls;
        if (class$org$apache$commons$logging$impl$LogFactoryImpl == null) {
            cls = class$(LogFactory.FACTORY_DEFAULT);
            class$org$apache$commons$logging$impl$LogFactoryImpl = cls;
        } else {
            cls = class$org$apache$commons$logging$impl$LogFactoryImpl;
        }
        ClassLoader classLoader = getClassLoader(cls);
        if (!this.useTCCL) {
            return classLoader;
        }
        ClassLoader contextClassLoaderInternal = getContextClassLoaderInternal();
        ClassLoader lowestClassLoader = getLowestClassLoader(contextClassLoaderInternal, classLoader);
        if (lowestClassLoader != null) {
            if (lowestClassLoader != contextClassLoaderInternal) {
                if (!this.allowFlawedContext) {
                    throw new LogConfigurationException("Bad classloader hierarchy; LogFactoryImpl was loaded via a classloader that is not related to the current context classloader.");
                } else if (isDiagnosticsEnabled()) {
                    logDiagnostic("Warning: the context classloader is an ancestor of the classloader that loaded LogFactoryImpl; it should be the same or a descendant. The application using commons-logging should ensure the context classloader is used correctly.");
                }
            }
            return lowestClassLoader;
        } else if (this.allowFlawedContext) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[WARNING] the context classloader is not part of a parent-child relationship with the classloader that loaded LogFactoryImpl.");
            }
            return contextClassLoaderInternal;
        } else {
            throw new LogConfigurationException("Bad classloader hierarchy; LogFactoryImpl was loaded via a classloader that is not related to the current context classloader.");
        }
    }

    private ClassLoader getLowestClassLoader(ClassLoader classLoader, ClassLoader classLoader2) {
        if (classLoader == null) {
            return classLoader2;
        }
        if (classLoader2 == null) {
            return classLoader;
        }
        for (ClassLoader classLoader3 = classLoader; classLoader3 != null; classLoader3 = classLoader3.getParent()) {
            if (classLoader3 == classLoader2) {
                return classLoader;
            }
        }
        for (ClassLoader classLoader4 = classLoader2; classLoader4 != null; classLoader4 = classLoader4.getParent()) {
            if (classLoader4 == classLoader) {
                return classLoader2;
            }
        }
        return null;
    }

    private void handleFlawedDiscovery(String str, ClassLoader classLoader, Throwable th) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic(new StringBuffer("Could not instantiate Log '").append(str).append("' -- ").append(th.getClass().getName()).append(": ").append(th.getLocalizedMessage()).toString());
            if (th instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) th).getTargetException();
                if (targetException != null) {
                    logDiagnostic(new StringBuffer("... InvocationTargetException: ").append(targetException.getClass().getName()).append(": ").append(targetException.getLocalizedMessage()).toString());
                    if (targetException instanceof ExceptionInInitializerError) {
                        Throwable exception = ((ExceptionInInitializerError) targetException).getException();
                        if (exception != null) {
                            logDiagnostic(new StringBuffer("... ExceptionInInitializerError: ").append(exception.getClass().getName()).append(": ").append(exception.getLocalizedMessage()).toString());
                        }
                    }
                }
            }
        }
        if (!this.allowFlawedDiscovery) {
            throw new LogConfigurationException(th);
        }
    }

    private void handleFlawedHierarchy(ClassLoader classLoader, Class cls) {
        Class cls2;
        Class cls3;
        Class cls4;
        Class cls5;
        boolean z = false;
        if (class$org$apache$commons$logging$Log == null) {
            cls2 = class$(LOG_PROPERTY);
            class$org$apache$commons$logging$Log = cls2;
        } else {
            cls2 = class$org$apache$commons$logging$Log;
        }
        String name = cls2.getName();
        Class[] interfaces = cls.getInterfaces();
        int i = 0;
        while (true) {
            if (i >= interfaces.length) {
                break;
            } else if (name.equals(interfaces[i].getName())) {
                z = true;
                break;
            } else {
                i++;
            }
        }
        if (z) {
            if (isDiagnosticsEnabled()) {
                try {
                    if (class$org$apache$commons$logging$Log == null) {
                        cls5 = class$(LOG_PROPERTY);
                        class$org$apache$commons$logging$Log = cls5;
                    } else {
                        cls5 = class$org$apache$commons$logging$Log;
                    }
                    logDiagnostic(new StringBuffer("Class '").append(cls.getName()).append("' was found in classloader ").append(LogFactory.objectId(classLoader)).append(". It is bound to a Log interface which is not the one loaded from classloader ").append(LogFactory.objectId(getClassLoader(cls5))).toString());
                } catch (Throwable unused) {
                    logDiagnostic(new StringBuffer("Error while trying to output diagnostics about bad class '").append(cls).append("'").toString());
                }
            }
            if (!this.allowFlawedHierarchy) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Terminating logging for this context ");
                stringBuffer.append("due to bad log hierarchy. ");
                stringBuffer.append("You have more than one version of '");
                if (class$org$apache$commons$logging$Log == null) {
                    cls4 = class$(LOG_PROPERTY);
                    class$org$apache$commons$logging$Log = cls4;
                } else {
                    cls4 = class$org$apache$commons$logging$Log;
                }
                stringBuffer.append(cls4.getName());
                stringBuffer.append("' visible.");
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(stringBuffer.toString());
                }
                throw new LogConfigurationException(stringBuffer.toString());
            } else if (isDiagnosticsEnabled()) {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("Warning: bad log hierarchy. ");
                stringBuffer2.append("You have more than one version of '");
                if (class$org$apache$commons$logging$Log == null) {
                    cls3 = class$(LOG_PROPERTY);
                    class$org$apache$commons$logging$Log = cls3;
                } else {
                    cls3 = class$org$apache$commons$logging$Log;
                }
                stringBuffer2.append(cls3.getName());
                stringBuffer2.append("' visible.");
                logDiagnostic(stringBuffer2.toString());
            }
        } else if (!this.allowFlawedDiscovery) {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("Terminating logging for this context. ");
            stringBuffer3.append("Log class '");
            stringBuffer3.append(cls.getName());
            stringBuffer3.append("' does not implement the Log interface.");
            if (isDiagnosticsEnabled()) {
                logDiagnostic(stringBuffer3.toString());
            }
            throw new LogConfigurationException(stringBuffer3.toString());
        } else if (isDiagnosticsEnabled()) {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("[WARNING] Log class '");
            stringBuffer4.append(cls.getName());
            stringBuffer4.append("' does not implement the Log interface.");
            logDiagnostic(stringBuffer4.toString());
        }
    }
}
