package com.norconex.jefmon.instances;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;

import com.norconex.commons.wicket.WicketClass;
import com.norconex.commons.wicket.resource.loader.StringResourceLoaderUtil;
import com.norconex.jef4.status.JobState;
import com.norconex.jef4.status.JobSuiteStatusSnapshot;
import com.norconex.jefmon.JEFMonApplication;
import com.norconex.jefmon.JEFMonConfig;
import com.norconex.jefmon.instance.JobSuitesStatusesMonitor;
import com.norconex.jefmon.model.ConfigurationDAO;

public final class InstancesManager extends WicketClass 
        implements Serializable {

    private static final long serialVersionUID = 830935350229721170L;
    private static final Logger LOG =
            LogManager.getLogger(InstancesManager.class);

    private InstancesManager() {
        super();
    }

    public static void addInstance(String url) {
        JEFMonConfig config = getConfig();
        synchronized (config) {
            config.setRemoteInstanceUrls(
                    ArrayUtils.add(config.getRemoteInstanceUrls(), url));
            try {
                ConfigurationDAO.saveConfig(config);
            } catch (IOException e) {
                throw new WicketRuntimeException("Config file not found: "
                        + ConfigurationDAO.CONFIG_FILE, e);
            }
        }
    }

    public static void removeInstance(String url) {
        JEFMonConfig config = getConfig();
        synchronized (config) {
            config.setRemoteInstanceUrls(ArrayUtils.removeElement(
                    config.getRemoteInstanceUrls(), url));
            try {
                ConfigurationDAO.saveConfig(config);
            } catch (IOException e) {
                throw new WicketRuntimeException("Config file not found: "
                        + ConfigurationDAO.CONFIG_FILE, e);
            }
        }
    }

    public static List<JEFMonInstance> loadInstances() {

        List<JEFMonInstance> freshInstances = new ArrayList<JEFMonInstance>();
        freshInstances.add(createThisJefMonInstance());

        JEFMonConfig cfg = JEFMonApplication.get().getConfig();
        String[] remoteUrls = cfg.getRemoteInstanceUrls();
        if (remoteUrls == null) {
            return freshInstances;
        }

        HttpClient httpclient = HttpClientBuilder.create().build();
        
        for (String url : remoteUrls) {
            JEFMonInstance instance = fetchJEFMonInstance(httpclient, url);
            if (instance != null) {
                freshInstances.add(instance);
            }
        }

        return freshInstances;
    }

    public static JEFMonInstance createThisJefMonInstance() {
        JEFMonConfig config = JEFMonApplication.get().getConfig();
        JobSuitesStatusesMonitor suitesStatusesMonitor = 
                JEFMonApplication.get().getJobSuitesStatusesMonitor();
        JEFMonInstance thisInstance = new JEFMonInstance(null);
        thisInstance.setName(config.getInstanceName());
        Collection<JobSuiteStatusSnapshot> suitesStatuses = 
                suitesStatusesMonitor.getJobSuitesStatuses();
        int totalRoot = 0;
        for (JobSuiteStatusSnapshot suiteStatuses : suitesStatuses) {
            JobState status = suiteStatuses.getRoot().getState();
            MutableInt count = thisInstance.getStatuses().get(status);
            if (count == null) {
                count = new MutableInt();
            }
            count.increment();
            thisInstance.getStatuses().put(status, count);
            totalRoot++;
        }
        thisInstance.setTotalRoots(totalRoot);
        return thisInstance;
    }
    
    private static JEFMonConfig getConfig() {
        return JEFMonApplication.get().getConfig();
    }

    private static JEFMonInstance fetchJEFMonInstance(
            HttpClient httpClient, final String url) {
        StringBuilder fullURL = new StringBuilder(url);
        if (!url.endsWith("/")) {
            fullURL.append("/");
        }
        fullURL.append("/suites/json");
        InputStream instream = null;
        JEFMonInstance instance = new JEFMonInstance(url);
        try {
            HttpGet httpget = new HttpGet(fullURL.toString());
            HttpResponse response = httpClient.execute(httpget);
            //int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                instream = entity.getContent();
                String content = IOUtils.toString(instream);
                JSONObject json = new JSONObject(content);
                
                instance.setName(json.getString("name"));
                instance.setTotalRoots(json.getInt("total"));
                JSONObject jsonStatuses = json.getJSONObject("statuses");
                @SuppressWarnings("unchecked")
                Iterator<String> it = jsonStatuses.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    int count = jsonStatuses.getInt(key);
                    JobState state = null;
                    if (StringUtils.isNotBlank(key)) {
                        state = JobState.valueOf(key);
                    }
                    instance.getStatuses().put(state, new MutableInt(count));
                }
                return instance;
            }
            ;
            instance.setRemoteError(StringResourceLoaderUtil.getString(
                    InstancesManager.class, "error.nocontent"));
        } catch (JSONException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error with " + instance.getUrl() + " ", e);
            }
            instance.setRemoteError(StringResourceLoaderUtil.getString(
                    InstancesManager.class, "error.badformat"));
        } catch (Exception e) {
            instance.setRemoteError("[Non-translated] " + e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.error("Could not fetch JEF Monitor instance.", e);
            } else {
                LOG.error("Could not fetch JEF Monitor instance: "
                        + e.getMessage());
            }
        } finally {
            IOUtils.closeQuietly(instream);
        }
        instance.setName(StringResourceLoaderUtil.getString(
                InstancesManager.class, "error"));
        return instance;
    }

}