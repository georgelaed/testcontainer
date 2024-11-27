package com.test.util;

import java.util.Locale;
import java.util.ResourceBundle;

/***
 * "You do not have to restrict yourself to using a single family of ResourceBundles.
 * For example, you could have a set of bundles for exception messages,
 * ExceptionResources (ExceptionResources_fr, ExceptionResources_de, ...), and one for widgets,
 * WidgetResource (WidgetResources_fr, WidgetResources_de, ...);
 * breaking up the resources however you like."
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.html">Java ResourceBundle API</a>
 *
 */
public enum Messenger {

    ALREADY_ACTIVE("already-active"),
    ALREADY_BASE("already-base"),
    ALREADY_DESTROYED("already-destroyed"),
    DEACTIVATING("deactivating"),
    ACTIVE_STATE("active-state"),
    EXECUTOR_SLEEPING("executor-sleeping"),
    EXECUTOR_LATCHED("executor-latched"),
    BASE_PRIORITY("base-priority"),
    DESTROYED_STATE("destroyed-state"),
    ALREADY_FALLBACK("already-fallback"),
    FALLBACK_PRIORITY("fallback-priority"),
    DEACTIVATED("deactivated"),
    ACTIVATING("activating"),
    ACTIVATED_OBJ("activated-obj"),
    INACTIVE_STATE("inactive-state"),
    INSTANTIATING_URI_OBJ_OBJ("instantiating-uri-obj-obj"),
    INSTANTIATED_OBJ("instantiated-obj")

    ;

    private static final ResourceBundle resourceBundle;
    private static final ResourceBundle resourceBundleHI;
    private static final ResourceBundle resourceBundleNL;
    private static final ResourceBundle resourceBundleTL;


    static {
        resourceBundle   = ResourceBundle.getBundle( "Messages");
        resourceBundleHI = ResourceBundle.getBundle( "Messages", Locale.forLanguageTag("hi"));
        resourceBundleNL = ResourceBundle.getBundle( "Messages", Locale.forLanguageTag("nl"));
        resourceBundleTL = ResourceBundle.getBundle( "Messages", Locale.forLanguageTag("tl"));
    }

    private final String key;

    Messenger(String key) {
        this.key = key;
    }

    public String translate() {
        return resourceBundle.getString(this.key);
    }

    public String toDutch() {
        return resourceBundleNL.getString(this.key);
    }

    public String toHindi() {
        return resourceBundleHI.getString(this.key);
    }

    public String toTagalog() {
        return resourceBundleTL.getString(this.key);
    }
}