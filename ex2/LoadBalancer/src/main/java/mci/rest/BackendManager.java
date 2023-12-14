package mci.rest;

import java.util.ArrayList;
import java.util.List;

public final class BackendManager {

    private static BackendManager instance;
    private static Object monitor = new Object();
    private List<String> serviceRegistry = new ArrayList<>();

    public List<String> getServiceRegistry() {
        return serviceRegistry;
    }

    public List<String> getBackendServices() {
        // Since this function could be called several times that the same time,
        // we need to synchronize it
        // Java uses the monitor concept for synchronisation
        synchronized (monitor) {
            // We return a copy of the backend service list because otherwise we
            // would need to synchronize any access to that list
            return new ArrayList<String>(serviceRegistry);
        }
    }

    public boolean addBackendService(String serviceUrl) {
        // Since this function could be called several times that the same time,
        // we need to synchronize it
        // Java uses the monitor concept for synchronisation
        synchronized (monitor) {
            if (!serviceRegistry.contains(serviceUrl)) {
                serviceRegistry.add(serviceUrl);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean removeBackendService(String serviceUrl) {
        // Since this function could be called several times that the same time,
        // we need to synchronize it
        // Java uses the monitor concept for synchronisation
        synchronized (monitor) {
            return serviceRegistry.remove(serviceUrl);
        }
    }

    public static BackendManager getInstance() {
        // Since this function could be called several times that the same time,
        // we need to synchronize it
        // Java uses the monitor concept for synchronisation
        synchronized (monitor) {
            if (instance == null) {
                instance = new BackendManager();
            }
        }
        return instance;
    }

    public void cleanUserSession() {
        serviceRegistry = null;
    }
}
