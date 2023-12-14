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
        synchronized (monitor) {
            return new ArrayList<String>(serviceRegistry);
        }
    }

    public boolean addBackendService(String serviceUrl) {
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
        synchronized (monitor) {
            return serviceRegistry.remove(serviceUrl);
        }
    }

    public static BackendManager getInstance() {
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
