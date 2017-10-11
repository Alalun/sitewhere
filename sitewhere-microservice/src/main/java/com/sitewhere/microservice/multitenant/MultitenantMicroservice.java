package com.sitewhere.microservice.multitenant;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;
import com.sitewhere.grpc.model.client.TenantManagementApiChannel;
import com.sitewhere.grpc.model.client.TenantManagementGrpcChannel;
import com.sitewhere.grpc.model.spi.client.ITenantManagementApiChannel;
import com.sitewhere.microservice.MicroserviceEnvironment;
import com.sitewhere.microservice.configuration.ConfigurableMicroservice;
import com.sitewhere.microservice.spi.multitenant.IMicroserviceTenantEngine;
import com.sitewhere.microservice.spi.multitenant.IMultitenantMicroservice;
import com.sitewhere.server.lifecycle.CompositeLifecycleStep;
import com.sitewhere.server.lifecycle.InitializeComponentLifecycleStep;
import com.sitewhere.server.lifecycle.StartComponentLifecycleStep;
import com.sitewhere.server.lifecycle.StopComponentLifecycleStep;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.server.lifecycle.ICompositeLifecycleStep;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;

/**
 * Microservice that contains engines for multiple tenants.
 * 
 * @author Derek
 */
public abstract class MultitenantMicroservice extends ConfigurableMicroservice implements IMultitenantMicroservice {

    /** Tenant management GRPC channel */
    private TenantManagementGrpcChannel tenantManagementGrpcChannel;

    /** Tenant management API channel */
    private ITenantManagementApiChannel tenantManagementApiChannel;

    /** Map of tenant engines indexed by tenant id */
    private ConcurrentMap<String, IMicroserviceTenantEngine> tenantEnginesByTenantId = new MapMaker()
	    .concurrencyLevel(4).makeMap();

    public MultitenantMicroservice() {
	createGrpcComponents();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.configuration.ConfigurableMicroservice#
     * initialize(com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void initialize(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.initialize(monitor);

	// Create step that will start components.
	ICompositeLifecycleStep init = new CompositeLifecycleStep("Initialize " + getName());

	// Initialize tenant management GRPC channel.
	init.addStep(new InitializeComponentLifecycleStep(this, getTenantManagementGrpcChannel(),
		"Tenant management GRPC channel", "Unable to initialize tenant management GRPC channel", true));

	// Execute initialization steps.
	init.execute(monitor);

	// Wait for microservice to be configured.
	waitForConfigurationReady();

	// Call logic for initializing microservice subclass.
	microserviceInitialize(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#start(com.sitewhere.spi
     * .server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.start(monitor);

	// Create step that will start components.
	ICompositeLifecycleStep start = new CompositeLifecycleStep("Start " + getName());

	// Start tenant mangement GRPC channel.
	start.addStep(new StartComponentLifecycleStep(this, getTenantManagementGrpcChannel(),
		"Tenant management GRPC channel", "Unable to start tenant management GRPC channel.", true));

	// Execute startup steps.
	start.execute(monitor);

	// Call logic for starting microservice subclass.
	microserviceStart(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#stop(com.sitewhere.spi.
     * server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void stop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.stop(monitor);

	// Call logic for stopping microservice subclass.
	microserviceStop(monitor);

	// Create step that will stop components.
	ICompositeLifecycleStep stop = new CompositeLifecycleStep("Stop " + getName());

	// Stop tenant management GRPC channel.
	stop.addStep(new StopComponentLifecycleStep(this, getTenantManagementGrpcChannel(),
		"Tenant Managment GRPC Channel"));

	// Execute shutdown steps.
	stop.execute(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.configuration.ConfigurableMicroservice#
     * terminate(com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void terminate(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	getTenantManagementGrpcChannel().terminate(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.spi.multitenant.IMultitenantMicroservice#
     * getTenantEngineByTenantId(java.lang.String)
     */
    @Override
    public IMicroserviceTenantEngine getTenantEngineByTenantId(String id) throws SiteWhereException {
	return getTenantEnginesByTenantId().get(id);
    }

    /**
     * Create tenant engines based on subfolders in Zk configuration.
     * 
     * @throws SiteWhereException
     */
    protected void createTenantEngines() throws SiteWhereException {
    }

    /**
     * Create components that interact via GRPC.
     */
    protected void createGrpcComponents() {
	this.tenantManagementGrpcChannel = new TenantManagementGrpcChannel(
		MicroserviceEnvironment.HOST_TENANT_MANAGEMENT, MicroserviceEnvironment.DEFAULT_GRPC_PORT);
	this.tenantManagementApiChannel = new TenantManagementApiChannel(getTenantManagementGrpcChannel());
    }

    public TenantManagementGrpcChannel getTenantManagementGrpcChannel() {
	return tenantManagementGrpcChannel;
    }

    public void setTenantManagementGrpcChannel(TenantManagementGrpcChannel tenantManagementGrpcChannel) {
	this.tenantManagementGrpcChannel = tenantManagementGrpcChannel;
    }

    public ITenantManagementApiChannel getTenantManagementApiChannel() {
	return tenantManagementApiChannel;
    }

    public void setTenantManagementApiChannel(ITenantManagementApiChannel tenantManagementApiChannel) {
	this.tenantManagementApiChannel = tenantManagementApiChannel;
    }

    public ConcurrentMap<String, IMicroserviceTenantEngine> getTenantEnginesByTenantId() {
	return tenantEnginesByTenantId;
    }

    public void setTenantEnginesByTenantId(ConcurrentMap<String, IMicroserviceTenantEngine> tenantEnginesByTenantId) {
	this.tenantEnginesByTenantId = tenantEnginesByTenantId;
    }
}