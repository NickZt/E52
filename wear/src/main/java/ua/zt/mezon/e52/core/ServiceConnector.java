package ua.zt.mezon.e52.core;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by MezM on 23.11.2016.
 */
public class ServiceConnector<T extends Service> implements ServiceConnection {

    private T service;

    private ServiceListener<T> listener;

    public ServiceConnector(final ServiceListener<T> listener) {
        this.listener = listener;
    }

    public ServiceListener getServiceListener(){
        return listener;
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        this.service = ((ServiceBinder<T>) service).getService();
        listener.onServiceConnected(this.service);
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        listener.onServiceDisconnected(this.service);
    }

    public static class ServiceBinder<T extends Service> extends Binder {

        private T service;

        public ServiceBinder(final T service) {
            this.service = service;
        }

        public T getService() {
            return service;
        }

    }

    public interface ServiceListener<T extends Service> {

        void onServiceConnected(final T service);

        void onServiceDisconnected(final T service);
        void callAlarmfromService(String tmp);

    }

}
