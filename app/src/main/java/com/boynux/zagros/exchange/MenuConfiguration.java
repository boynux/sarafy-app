package com.boynux.zagros.exchange;

import android.support.v4.app.Fragment;

import com.boynux.zagros.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MenuConfiguration {

    private static final List<DemoFeature> demoFeatures = new ArrayList<DemoFeature>();

    static {
        addDemoFeature("zagros_exchanges", R.mipmap.zagros, R.string.exchange_rates_title,
                new DemoItem(R.string.exchange_rates_title, R.mipmap.zagros,
                        R.string.exchange_rates_title, ExchangeRateFragment.class));
        addDemoFeature("zagros_copyright", R.mipmap.zagros, R.string.title_activity_copyright,
                new DemoItem(R.string.title_activity_copyright, R.mipmap.zagros,
                        R.string.title_activity_copyright, CopyrightFragment.class));
        addDemoFeature("zagros_disclaimer", R.mipmap.zagros, R.string.title_activity_disclaimer,
                new DemoItem(R.string.title_activity_disclaimer, R.mipmap.zagros,
                        R.string.title_activity_disclaimer, DisclaimerFragment.class));
    }

    public static List<DemoFeature> getDemoFeatureList() {
        return Collections.unmodifiableList(demoFeatures);
    }

    public static DemoFeature getDemoFeatureByName(final String name) {
        for (DemoFeature demoFeature : demoFeatures) {
            if (demoFeature.name.equals(name)) {
                return demoFeature;
            }
        }
        return null;
    }

    private static void addDemoFeature(final String name, final int iconResId, final int titleResId,
                                       final DemoItem... demoItems) {
        DemoFeature demoFeature = new DemoFeature(name, iconResId, titleResId, demoItems);
        demoFeatures.add(demoFeature);
    }

    public static class DemoFeature {
        public String name;
        public int iconResId;
        public int titleResId;
        public List<DemoItem> demos;

        public DemoFeature() {

        }

        public DemoFeature(final String name, final int iconResId, final int titleResId,
                           final DemoItem... demoItems) {
            this.name = name;
            this.iconResId = iconResId;
            this.titleResId = titleResId;
            this.demos = Arrays.asList(demoItems);
        }
    }

    public static class DemoItem {
        public int titleResId;
        public int iconResId;
        public int buttonTextResId;
        public String fragmentClassName;

        public DemoItem(final int titleResId, final int iconResId, final int buttonTextResId,
                        final Class<? extends Fragment> fragmentClass) {
            this.titleResId = titleResId;
            this.iconResId = iconResId;
            this.buttonTextResId = buttonTextResId;
            this.fragmentClassName = fragmentClass.getName();
        }
    }
}
