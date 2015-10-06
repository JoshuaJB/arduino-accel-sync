package me.jbakita.arduinodatalogging;

import android.util.Log;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;

import java.util.ArrayList;

/**
 * Find LightBlue Beans and maintain a list of all devices found during this scan.
 */
public class BeanDiscoverer implements com.punchthrough.bean.sdk.BeanDiscoveryListener {
    private ArrayList<Bean> beans = new ArrayList();
    private boolean done = false;
    private BeanDiscovererListener listener;

    @Override
    public void onBeanDiscovered(Bean bean, int i) {
        beans.add(bean);
        Log.i("BeanDiscover", "bean discovered");
    }

    @Override
    public void onDiscoveryComplete() {
        done = true;
        listener.onDiscoveryComplete();
    }

    public boolean isScanComplete() {
        return done;
    }

    public Bean[] getDiscovered() {
        return beans.toArray(new Bean[]{});
    }

    public void addListenter(BeanDiscovererListener listener){
        this.listener = listener;
    }
}
