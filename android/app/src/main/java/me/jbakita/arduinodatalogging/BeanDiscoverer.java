package me.jbakita.arduinodatalogging;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;

import java.util.ArrayList;

/**
 * Find LightBlue Beans and maintain a list of all devices found during this scan.
 */
public class BeanDiscoverer implements BeanDiscoveryListener {
    private ArrayList<Bean> beans = new ArrayList();
    private boolean done = false;

    @Override
    public void onBeanDiscovered(Bean bean, int i) {
        beans.add(bean);
    }

    @Override
    public void onDiscoveryComplete() {
        done = true;
    }

    public boolean isScanComplete() {
        return done;
    }

    public Bean[] getDiscovered() {
        return beans.toArray(new Bean[]{});
    }
}
